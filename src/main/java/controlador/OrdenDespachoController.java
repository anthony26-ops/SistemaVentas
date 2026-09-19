package controlador;

import dao.OrdenDespachoDAO;
import dao.impl.OrdenDespachoDAOImpl;
import java.util.Date;
import java.util.List;
import modelo.DetalleOrdenDespacho;
import modelo.OrdenDespacho;

/**
 * Controlador del modulo de Orden de Despacho. Solo logica de negocio,
 * sin dependencias de la interfaz grafica.
 */
public class OrdenDespachoController {

    private final OrdenDespachoDAO ordenDespachoDAO;

    /** Estados validos por los que puede pasar una orden de despacho. */
    public static final String[] ESTADOS = {"PENDIENTE", "EN RUTA", "ENTREGADA", "CANCELADA"};

    public OrdenDespachoController() {
        this.ordenDespachoDAO = new OrdenDespachoDAOImpl();
    }

    /**
     * Crea una orden de despacho con sus lineas de producto.
     *
     * @throws Exception si falta informacion obligatoria.
     */
    public OrdenDespacho crearOrdenDespacho(int idOrdenVenta, String direccionEntrega,
                                            String transportista,
                                            List<DetalleOrdenDespacho> detalles) throws Exception {
        if (idOrdenVenta <= 0) {
            throw new Exception("Debe indicar la orden de venta que se va a despachar.");
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
        }

        OrdenDespacho orden = new OrdenDespacho();
        orden.setIdOrdenVenta(idOrdenVenta);
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
