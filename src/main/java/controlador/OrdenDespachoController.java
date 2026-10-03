package controlador;

import dao.BodegaDAO;
import dao.BodegaDAOImpl;
import dao.InventarioDAO;
import dao.OrdenDespachoDAO;
import dao.OrdenVentaDAO;
import dao.impl.InventarioDAOImpl;
import dao.impl.OrdenDespachoDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.Date;
import java.util.List;
import modelo.Bodega;
import modelo.DetalleOrdenDespacho;
import modelo.OrdenDespacho;
import modelo.OrdenVenta;

/**
 * Controlador del modulo de Orden de Despacho. Solo logica de negocio,
 * sin dependencias de la interfaz grafica.
 */
public class OrdenDespachoController {

    private final OrdenDespachoDAO ordenDespachoDAO;
    private final OrdenVentaDAO ordenVentaDAO;
    private final InventarioDAO inventarioDAO;
    private final BodegaDAO bodegaDAO;

    /** Estados validos por los que puede pasar una orden de despacho. */
    public static final String[] ESTADOS = {"PENDIENTE", "EN RUTA", "ENTREGADA", "CANCELADA"};

    public OrdenDespachoController() {
        this.ordenDespachoDAO = new OrdenDespachoDAOImpl();
        this.ordenVentaDAO = new OrdenVentaDAOImpl();
        this.inventarioDAO = new InventarioDAOImpl();
        this.bodegaDAO = new BodegaDAOImpl();
    }

    /** Bodegas disponibles, para que la pantalla pregunte de cuál sale el despacho. */
    public List<Bodega> listarBodegas() {
        return bodegaDAO.listar();
    }

    /** Órdenes de venta listas para despachar (estado "Pendiente"). */
    public List<OrdenVenta> listarOrdenesPendientes() {
        return ordenVentaDAO.listarPendientes();
    }

    /** Carga una orden de venta completa, con su detalle real de productos. */
    public OrdenVenta obtenerOrdenVenta(int idOrdenVenta) {
        return ordenVentaDAO.obtenerOrdenPorId(idOrdenVenta);
    }

    /**
     * Crea una orden de despacho con sus lineas de producto.
     *
     * @throws Exception si falta informacion obligatoria.
     */
    public OrdenDespacho crearOrdenDespacho(int idOrdenVenta, int idBodega, String direccionEntrega,
                                            String transportista,
                                            List<DetalleOrdenDespacho> detalles) throws Exception {
        if (idOrdenVenta <= 0) {
            throw new Exception("Debe indicar la orden de venta que se va a despachar.");
        }

        OrdenVenta ordenVenta = ordenVentaDAO.obtenerOrdenPorId(idOrdenVenta);
        if (ordenVenta == null) {
            throw new Exception("La orden de venta No. " + idOrdenVenta + " no existe.");
        }
        if (!"Pendiente".equalsIgnoreCase(ordenVenta.getEstado())) {
            throw new Exception("La orden No. " + idOrdenVenta + " ya está en estado \""
                    + ordenVenta.getEstado() + "\" y no se puede volver a despachar.");
        }

        if (idBodega <= 0) {
            throw new Exception("Debe indicar de qué bodega sale el despacho.");
        }
        if (direccionEntrega == null || direccionEntrega.trim().isEmpty()) {
            throw new Exception("Debe ingresar la direccion de entrega.");
        }
        if (transportista == null || transportista.trim().isEmpty()) {
            throw new Exception("Debe indicar el transportista responsable.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new Exception("La orden debe llevar al menos un producto.");
        }
        for (DetalleOrdenDespacho detalle : detalles) {
            if (detalle.getCantidad() <= 0) {
                throw new Exception("La cantidad de cada producto debe ser mayor a cero.");
            }
            int disponibleEnBodega = inventarioDAO.obtenerCantidad(detalle.getIdProducto(), idBodega);
            if (disponibleEnBodega < detalle.getCantidad()) {
                throw new Exception("No hay existencia suficiente de \"" + detalle.getNombreProducto()
                        + "\" en esa bodega. Disponible: " + disponibleEnBodega
                        + ", solicitado: " + detalle.getCantidad() + ".");
            }
        }

        OrdenDespacho orden = new OrdenDespacho();
        orden.setIdOrdenVenta(idOrdenVenta);
        orden.setIdBodega(idBodega);
        orden.setFechaDespacho(new Date());
        orden.setDireccionEntrega(direccionEntrega.trim());
        orden.setTransportista(transportista.trim());
        orden.setEstado("PENDIENTE");
        orden.setDetalles(detalles);

        if (!ordenDespachoDAO.insertar(orden)) {
            throw new Exception("No se pudo guardar la orden de despacho.");
        }
        return orden;
    }

    /** Cambia el estado de la orden validando que el estado exista. */
    public boolean cambiarEstado(int idOrdenDespacho, String nuevoEstado) throws Exception {
        boolean valido = false;
        for (String estado : ESTADOS) {
            if (estado.equalsIgnoreCase(nuevoEstado)) {
                valido = true;
                break;
            }
        }
        if (!valido) {
            throw new Exception("El estado indicado no es valido.");
        }
        return ordenDespachoDAO.actualizarEstado(idOrdenDespacho, nuevoEstado.toUpperCase());
    }

    public boolean eliminarOrden(int idOrdenDespacho) {
        return ordenDespachoDAO.eliminar(idOrdenDespacho);
    }

    public OrdenDespacho obtenerOrden(int idOrdenDespacho) {
        return ordenDespachoDAO.obtenerPorId(idOrdenDespacho);
    }

    public List<OrdenDespacho> listarOrdenes() {
        return ordenDespachoDAO.listarTodos();
    }

    public List<DetalleOrdenDespacho> listarDetalles(int idOrdenDespacho) {
        return ordenDespachoDAO.listarDetalles(idOrdenDespacho);
    }
}
