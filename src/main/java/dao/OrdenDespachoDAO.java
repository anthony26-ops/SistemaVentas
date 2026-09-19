package dao;

import java.util.List;
import modelo.DetalleOrdenDespacho;
import modelo.OrdenDespacho;

public interface OrdenDespachoDAO {
    boolean insertar(OrdenDespacho orden);
    boolean actualizarEstado(int idOrdenDespacho, String estado);
    boolean eliminar(int idOrdenDespacho);
    OrdenDespacho obtenerPorId(int idOrdenDespacho);
    List<OrdenDespacho> listarTodos();
    List<DetalleOrdenDespacho> listarDetalles(int idOrdenDespacho);
}
