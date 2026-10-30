package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.OrdenVentaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Cliente;
import modelo.DetalleOrdenVenta;
import modelo.Empleado;
import modelo.OrdenVenta;
import modelo.Producto;

public class OrdenVentaController {

    private OrdenVentaDAO ordenDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ProductoDAO productoDAO;

    public OrdenVentaController() {
        this.ordenDAO = new OrdenVentaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }
    
    public List<Cliente> obtenerClientes() {
        return clienteDAO.listarTodos();
    }
    
    public List<Empleado> obtenerEmpleados() {
        return empleadoDAO.listarTodos();
    }
    
    /**
     * PUNTO 7: Método puro de negocio, independiente de Swing.
     * Ideal para integrarse con apps móviles o web.
     */
    public boolean crearPedido(int idCliente, int idEmpleado, double total, String estado) throws Exception {
        if (total <= 0) {
            throw new Exception("El total de la orden debe ser mayor a cero.");
        }
        
        OrdenVenta orden = new OrdenVenta();
        orden.setFecha(new java.sql.Date(System.currentTimeMillis()));
        
        Cliente cliente = new Cliente();
        cliente.setId(idCliente); // Asumiendo que obtienes el ID de la vista
        orden.setCliente(cliente);
        
        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        orden.setEmpleado(empleado);
        
        orden.setTotal(total);
        orden.setEstado(estado); // Cumpliendo el Punto 1
        
        return ordenDAO.registrarOrden(orden);
    }

    /**
     * Igual que crearPedido, pero guardando también el detalle de productos
     * (List&lt;DetalleOrdenVenta&gt;) para que la factura y el despacho puedan
     * generarse a partir de la orden real.
     */
    public boolean crearPedido(int idCliente, int idEmpleado, List<DetalleOrdenVenta> detalles, String estado) throws Exception {
        return crearPedido(idCliente, idEmpleado, detalles, estado, new java.util.Date());
    }

    /**
     * Igual que crearPedido, pero recibiendo la fecha elegida en el
     * selector de calendario de la pantalla en vez de asumir siempre
     * "ahora mismo".
     */
    public boolean crearPedido(int idCliente, int idEmpleado, List<DetalleOrdenVenta> detalles, String estado,
            java.util.Date fecha) throws Exception {
        if (detalles == null || detalles.isEmpty()) {
            throw new Exception("El pedido debe tener al menos un producto.");
        }

        validarExistencias(detalles);
        return ordenDAO.registrarOrden(nuevaOrden(idCliente, idEmpleado, detalles, estado, fecha));
    }

    /**
     * Pedido que llega de la app Android a través de la API REST. Recibe solo ids y
     * cantidades: el precio sale SIEMPRE del catálogo (nunca de lo que mande el
     * teléfono) y el total lo calcula este método. Devuelve la orden ya guardada,
     * con su id, en estado Pendiente (queda lista para el Despacho del escritorio).
     */
    public OrdenVenta crearPedidoMovil(int idCliente, int idEmpleado, Map<Integer, Integer> cantidades,
            String metodoPago, String observaciones) throws Exception {
        if (cantidades == null || cantidades.isEmpty()) {
            throw new Exception("El pedido debe tener al menos un producto.");
        }
        if (clienteDAO.obtenerPorId(idCliente) == null) {
            throw new Exception("El cliente " + idCliente + " no existe.");
        }
        if (empleadoDAO.obtenerPorId(idEmpleado) == null) {
            throw new Exception("El vendedor " + idEmpleado + " no existe.");
        }
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            throw new Exception("Indique el método de pago.");
        }
        if (metodoPago.length() > 30) {
            throw new Exception("El método de pago no puede pasar de 30 caracteres.");
        }
        if (observaciones != null && observaciones.length() > 255) {
            throw new Exception("Las observaciones no pueden pasar de 255 caracteres.");
        }

        List<DetalleOrdenVenta> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> linea : cantidades.entrySet()) {
            int cantidad = linea.getValue() == null ? 0 : linea.getValue();
            if (cantidad <= 0) {
                throw new Exception("La cantidad del producto " + linea.getKey() + " debe ser mayor a cero.");
            }
            Producto producto = productoDAO.buscarPorId(linea.getKey());
            if (producto == null) {
                throw new Exception("El producto " + linea.getKey() + " no existe en el catálogo.");
            }
            detalles.add(new DetalleOrdenVenta(0, null, producto, cantidad, producto.getPrecio()));
        }
        validarExistencias(detalles);

        OrdenVenta orden = nuevaOrden(idCliente, idEmpleado, detalles, "Pendiente", new java.util.Date());
        orden.setMetodoPago(metodoPago.trim());
        orden.setObservaciones(observaciones == null || observaciones.trim().isEmpty() ? null : observaciones.trim());
        if (!ordenDAO.registrarOrden(orden)) {
            throw new IllegalStateException("No se pudo guardar el pedido en la base de datos.");
        }
        return orden;
    }

    /**
     * Paso 4 del flujo obligatorio: validar que exista inventario suficiente
     * para los productos solicitados, ANTES de crear la orden.
     */
    private void validarExistencias(List<DetalleOrdenVenta> detalles) throws Exception {
        for (DetalleOrdenVenta d : detalles) {
            Producto producto = productoDAO.buscarPorId(d.getProducto().getIdProducto());
            if (producto == null) {
                throw new Exception("El producto '" + d.getProducto().getNombre() + "' ya no existe en el catálogo.");
            }
            if (producto.getExistencia() < d.getCantidad()) {
                throw new Exception("No hay existencia suficiente de '" + producto.getNombre() + "'. "
                        + "Disponible: " + producto.getExistencia() + ", solicitado: " + d.getCantidad() + ".");
            }
        }
    }

    private OrdenVenta nuevaOrden(int idCliente, int idEmpleado, List<DetalleOrdenVenta> detalles, String estado,
            java.util.Date fecha) {
        double total = 0;
        for (DetalleOrdenVenta d : detalles) {
            total += d.getSubtotal();
        }

        OrdenVenta orden = new OrdenVenta();
        orden.setFecha(new java.sql.Date((fecha != null ? fecha : new java.util.Date()).getTime()));

        Cliente cliente = new Cliente();
        cliente.setId(idCliente);
        orden.setCliente(cliente);

        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        orden.setEmpleado(empleado);

        orden.setTotal(total);
        orden.setEstado(estado);
        orden.setDetalles(detalles);
        return orden;
    }

    public List<OrdenVenta> listarPendientes() {
        return ordenDAO.listarPendientes();
    }

    public List<OrdenVenta> listarTodas() {
        return ordenDAO.listarOrdenes();
    }

    /** Orden completa (con su detalle de productos) para pantallas de consulta. */
    public OrdenVenta obtenerOrden(int idOrdenVenta) {
        return ordenDAO.obtenerOrdenPorId(idOrdenVenta);
    }

    /**
     * PUNTO 6: Precio dinámico, tomado del catálogo real de Producto (André).
     */
    public double obtenerPrecioProducto(String nombreProducto) {
        for (Producto p : productoDAO.listar()) {
            if (p.getNombre() != null && p.getNombre().equalsIgnoreCase(nombreProducto)) {
                return p.getPrecio();
            }
        }
        return 0.0;
    }

    /**
     * Productos disponibles para el combo de la vista (André).
     */
    public List<Producto> obtenerProductos() {
        return productoDAO.listar();
    }
}