package api;

import bd.ConexionException;
import bd.ConexionMySQL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controlador.OrdenVentaController;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import modelo.Cliente;
import modelo.DetalleOrdenVenta;
import modelo.Empleado;
import modelo.OrdenVenta;
import modelo.Producto;

/**
 * API REST que usa la app Android para tomar pedidos. Es un programa aparte del
 * escritorio (otro main) y usa la misma base de datos y los mismos controladores.
 *
 * Solo escucha en 127.0.0.1: nadie de la red puede llegar a ella. El teléfono la
 * alcanza por el cable USB con "adb reverse" (ver android/README.md).
 *
 * Endpoints (todos devuelven JSON; los errores son {"error": "mensaje"}):
 *   GET  /api/salud           comprobar que la API está viva
 *   GET  /api/clientes        catálogo de clientes
 *   GET  /api/empleados       catálogo de vendedores
 *   GET  /api/productos       catálogo con precio y existencia
 *   GET  /api/pedidos         historial, del más nuevo al más viejo
 *   GET  /api/pedidos/{id}    un pedido con sus productos
 *   POST /api/pedidos         registrar un pedido (queda Pendiente, listo para Despacho)
 */
public class ServidorApi {

    private static final int PUERTO_POR_DEFECTO = 8090;
    private static final int MAX_CUERPO = 64 * 1024;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final OrdenVentaController controlador = new OrdenVentaController();

    // ---- Lo que recibe la API ----
    record LineaEntrada(Integer idProducto, Integer cantidad) { }

    record PedidoEntrada(Integer idCliente, Integer idEmpleado, String metodoPago, String observaciones,
            List<LineaEntrada> productos) { }

    // ---- Lo que devuelve la API ----
    record PersonaSalida(int id, String nombre, String apellido) { }

    record ProductoSalida(int id, String nombre, double precio, int existencia) { }

    record PedidoResumen(int id, String fecha, double total, String estado, String metodoPago,
            String observaciones) { }

    record LineaSalida(int idProducto, String nombre, int cantidad, double precioUnitario, double subtotal) { }

    record PedidoDetalle(int id, String fecha, int idCliente, int idEmpleado, double total, String estado,
            String metodoPago, String observaciones, List<LineaSalida> productos) { }

    record Salud(String estado) { }

    /** Los catálogos salen ordenados por nombre, como los mostraba la app antes de usar la API. */
    private static final class PorNombre {

        static final Comparator<PersonaSalida> PERSONAS = Comparator.comparing(PersonaSalida::nombre,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        static final Comparator<ProductoSalida> PRODUCTOS = Comparator.comparing(ProductoSalida::nombre,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    record ErrorSalida(String error) { }

    /** Error que se le devuelve tal cual a quien llamó a la API. */
    private static class ErrorApi extends RuntimeException {

        final int estado;

        ErrorApi(int estado, String mensaje) {
            super(mensaje);
            this.estado = estado;
        }
    }

    @FunctionalInterface
    private interface Ruta {

        void atender(HttpExchange ex) throws Exception;
    }

    public static void main(String[] args) throws Exception {
        try {
            // Igual que el escritorio: crea la base y las tablas si faltan (y agrega
            // las columnas nuevas), así la API funciona aunque el escritorio no se
            // haya abierto nunca en esta máquina.
            ConexionMySQL.inicializar();
        } catch (ConexionException ex) {
            Throwable causa = ex;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.err.println("No se pudo preparar la base de datos MySQL: " + causa.getMessage());
            System.err.println("Revise que MySQL esté corriendo (usuario root sin contraseña por defecto;"
                    + " si tiene contraseña defina DB_PASSWORD).");
            System.exit(1);
        }

        int puerto = puertoConfigurado();
        HttpServer servidor = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), puerto), 0);
        new ServidorApi().registrarRutas(servidor);
        servidor.setExecutor(Executors.newFixedThreadPool(4));
        servidor.start();

        System.out.println("API de Sistema de Ventas escuchando en http://127.0.0.1:" + puerto + "/api/");
        System.out.println("Con el telefono por cable USB:  adb reverse tcp:8090 tcp:" + puerto);
        System.out.println("Para detenerla: Ctrl+C");
    }

    /** Igual que DB_USER/DB_PASSWORD: variable de entorno o propiedad -D API_PORT. */
    private static int puertoConfigurado() {
        String valor = System.getProperty("API_PORT");
        if (valor == null) {
            valor = System.getenv("API_PORT");
        }
        if (valor == null) {
            return PUERTO_POR_DEFECTO;
        }
        try {
            int puerto = Integer.parseInt(valor.trim());
            if (puerto < 1 || puerto > 65535) {
                throw new NumberFormatException();
            }
            return puerto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("API_PORT debe ser un número entre 1 y 65535 (recibido: " + valor + ")");
        }
    }

    private void registrarRutas(HttpServer servidor) {
        ruta(servidor, "/api/salud", ex -> {
            soloMetodo(ex, "GET");
            soloRuta(ex, "/api/salud");
            responder(ex, 200, new Salud("ok"));
        });
        ruta(servidor, "/api/clientes", ex -> {
            soloMetodo(ex, "GET");
            soloRuta(ex, "/api/clientes");
            List<PersonaSalida> salida = new ArrayList<>();
            for (Cliente c : controlador.obtenerClientes()) {
                salida.add(new PersonaSalida(c.getIdCliente(), c.getNombre(), c.getApellido()));
            }
            salida.sort(PorNombre.PERSONAS);
            responder(ex, 200, salida);
        });
        ruta(servidor, "/api/empleados", ex -> {
            soloMetodo(ex, "GET");
            soloRuta(ex, "/api/empleados");
            List<PersonaSalida> salida = new ArrayList<>();
            for (Empleado e : controlador.obtenerEmpleados()) {
                salida.add(new PersonaSalida(e.getIdEmpleado(), e.getNombre(), e.getApellido()));
            }
            salida.sort(PorNombre.PERSONAS);
            responder(ex, 200, salida);
        });
        ruta(servidor, "/api/productos", ex -> {
            soloMetodo(ex, "GET");
            soloRuta(ex, "/api/productos");
            List<ProductoSalida> salida = new ArrayList<>();
            for (Producto p : controlador.obtenerProductos()) {
                salida.add(new ProductoSalida(p.getIdProducto(), p.getNombre(), p.getPrecio(), p.getExistencia()));
            }
            salida.sort(PorNombre.PRODUCTOS);
            responder(ex, 200, salida);
        });
        ruta(servidor, "/api/pedidos", this::pedidos);
        // Cualquier otra ruta: JSON y no la página HTML que mandaría HttpServer por su cuenta.
        ruta(servidor, "/", ex -> {
            throw new ErrorApi(404, "No existe esa ruta.");
        });
    }

    private void pedidos(HttpExchange ex) throws Exception {
        String path = ex.getRequestURI().getPath();
        String metodo = ex.getRequestMethod();

        if (path.equals("/api/pedidos")) {
            if (metodo.equals("GET")) {
                List<OrdenVenta> ordenes = new ArrayList<>(controlador.listarTodas());
                ordenes.sort(Comparator.comparingInt(OrdenVenta::getIdOrdenVenta).reversed());
                List<PedidoResumen> salida = new ArrayList<>();
                for (OrdenVenta o : ordenes) {
                    salida.add(new PedidoResumen(o.getIdOrdenVenta(), String.valueOf(o.getFecha()),
                            redondear(o.getTotal()), o.getEstado(), o.getMetodoPago(), o.getObservaciones()));
                }
                responder(ex, 200, salida);
            } else if (metodo.equals("POST")) {
                crearPedido(ex);
            } else {
                throw metodoNoPermitido(ex, "GET, POST");
            }
            return;
        }

        if (path.startsWith("/api/pedidos/")) {
            soloMetodo(ex, "GET");
            int id;
            try {
                id = Integer.parseInt(path.substring("/api/pedidos/".length()));
            } catch (NumberFormatException e) {
                throw new ErrorApi(404, "No existe esa ruta.");
            }
            OrdenVenta o = controlador.obtenerOrden(id);
            if (o == null) {
                throw new ErrorApi(404, "El pedido " + id + " no existe.");
            }
            List<LineaSalida> lineas = new ArrayList<>();
            if (o.getDetalles() != null) {
                for (DetalleOrdenVenta d : o.getDetalles()) {
                    lineas.add(new LineaSalida(d.getProducto().getIdProducto(), d.getProducto().getNombre(),
                            d.getCantidad(), d.getPrecioUnitario(), redondear(d.getSubtotal())));
                }
            }
            responder(ex, 200, new PedidoDetalle(o.getIdOrdenVenta(), String.valueOf(o.getFecha()),
                    o.getCliente().getId(), o.getEmpleado().getId(), redondear(o.getTotal()), o.getEstado(),
                    o.getMetodoPago(), o.getObservaciones(), lineas));
            return;
        }

        throw new ErrorApi(404, "No existe esa ruta.");
    }

    private void crearPedido(HttpExchange ex) throws Exception {
        PedidoEntrada entrada;
        try {
            entrada = GSON.fromJson(leerCuerpo(ex), PedidoEntrada.class);
        } catch (JsonParseException e) {
            throw new ErrorApi(400, "El cuerpo no es un JSON válido.");
        }
        if (entrada == null || entrada.idCliente() == null || entrada.idEmpleado() == null
                || entrada.productos() == null || entrada.productos().isEmpty()) {
            throw new ErrorApi(400, "Faltan datos: idCliente, idEmpleado y al menos un producto.");
        }

        // Si el mismo producto viene en dos líneas se suman, para validar la existencia una sola vez.
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (LineaEntrada linea : entrada.productos()) {
            if (linea == null || linea.idProducto() == null || linea.cantidad() == null) {
                throw new ErrorApi(400, "Cada producto necesita idProducto y cantidad.");
            }
            cantidades.merge(linea.idProducto(), linea.cantidad(), Integer::sum);
        }

        OrdenVenta orden;
        try {
            orden = controlador.crearPedidoMovil(entrada.idCliente(), entrada.idEmpleado(), cantidades,
                    entrada.metodoPago(), entrada.observaciones());
        } catch (IllegalStateException e) {
            throw new ErrorApi(500, e.getMessage()); // falló la base de datos
        } catch (Exception e) {
            throw new ErrorApi(400, e.getMessage()); // regla de negocio: cliente inexistente, sin existencia, etc.
        }

        ex.getResponseHeaders().set("Location", "/api/pedidos/" + orden.getIdOrdenVenta());
        responder(ex, 201, new PedidoResumen(orden.getIdOrdenVenta(), String.valueOf(orden.getFecha()),
                redondear(orden.getTotal()), orden.getEstado(), orden.getMetodoPago(), orden.getObservaciones()));
    }

    // ---- Utilidades HTTP ----

    private void ruta(HttpServer servidor, String path, Ruta ruta) {
        servidor.createContext(path, ex -> {
            try {
                ruta.atender(ex);
            } catch (ErrorApi e) {
                responder(ex, e.estado, new ErrorSalida(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                responder(ex, 500, new ErrorSalida("Error interno del servidor."));
            } finally {
                ex.close();
            }
        });
    }

    private static void soloMetodo(HttpExchange ex, String permitido) {
        if (!ex.getRequestMethod().equals(permitido)) {
            throw metodoNoPermitido(ex, permitido);
        }
    }

    /** El contexto de HttpServer coincide por prefijo: "/api/clientesXYZ" no debe contestar. */
    private static void soloRuta(HttpExchange ex, String path) {
        if (!ex.getRequestURI().getPath().equals(path)) {
            throw new ErrorApi(404, "No existe esa ruta.");
        }
    }

    private static ErrorApi metodoNoPermitido(HttpExchange ex, String permitidos) {
        ex.getResponseHeaders().set("Allow", permitidos);
        return new ErrorApi(405, "Método no permitido. Use: " + permitidos + ".");
    }

    private static String leerCuerpo(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] datos = in.readNBytes(MAX_CUERPO + 1);
            if (datos.length > MAX_CUERPO) {
                throw new ErrorApi(413, "El cuerpo es demasiado grande.");
            }
            return new String(datos, StandardCharsets.UTF_8);
        }
    }

    private static void responder(HttpExchange ex, int estado, Object cuerpo) throws IOException {
        byte[] datos = GSON.toJson(cuerpo).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(estado, datos.length);
        ex.getResponseBody().write(datos);
        System.out.println(ex.getRequestMethod() + " " + ex.getRequestURI() + " -> " + estado);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
