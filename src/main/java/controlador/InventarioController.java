package controlador;

import dao.BodegaDAO;
import dao.BodegaDAOImpl;
import dao.InventarioDAO;
import dao.impl.InventarioDAOImpl;
import java.util.List;
import modelo.Bodega;
import modelo.Inventario;

/**
 * Controlador del inventario real por bodega. Es el punto único por el que
 * las demás pantallas (Productos, Despacho) deberían acreditar o descontar
 * existencia de una bodega concreta, en vez de tocar directamente
 * Producto.existencia (que sigue existiendo solo como total cacheado para
 * las validaciones rápidas y el resaltado de "poco stock").
 */
public class InventarioController {

    private final InventarioDAO inventarioDAO = new InventarioDAOImpl();
    private final BodegaDAO bodegaDAO = new BodegaDAOImpl();

    public List<Bodega> listarBodegas() {
        return bodegaDAO.listar();
    }

    public int obtenerCantidad(int idProducto, int idBodega) {
        return inventarioDAO.obtenerCantidad(idProducto, idBodega);
    }

    public int totalDisponible(int idProducto) {
        return inventarioDAO.totalDisponible(idProducto);
    }

    public List<Inventario> listarPorProducto(int idProducto) {
        return inventarioDAO.listarPorProducto(idProducto);
    }

    public boolean acreditar(int idProducto, int idBodega, int cantidad) throws Exception {
        if (cantidad <= 0) {
            throw new Exception("La cantidad a acreditar debe ser mayor a cero.");
        }
        return inventarioDAO.acreditar(idProducto, idBodega, cantidad);
    }

    public boolean descontar(int idProducto, int idBodega, int cantidad) throws Exception {
        if (cantidad <= 0) {
            throw new Exception("La cantidad a descontar debe ser mayor a cero.");
        }
        return inventarioDAO.descontar(idProducto, idBodega, cantidad);
    }
}
