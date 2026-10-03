package dao;

import java.util.List;
import modelo.Inventario;

/**
 * Acceso al stock real por bodega (tabla ventas_inventario). A diferencia de
 * Producto.existencia (un solo número global, usado como total cacheado
 * para validaciones rápidas), aquí se sabe exactamente cuánto hay de cada
 * producto en cada bodega.
 */
public interface InventarioDAO {

    /** Cantidad disponible de un producto en una bodega puntual (0 si no hay renglón todavía). */
    int obtenerCantidad(int idProducto, int idBodega);

    /** Suma de todas las bodegas para un producto. */
    int totalDisponible(int idProducto);

    /** Desglose por bodega de un producto, para mostrarlo en pantalla. */
    List<Inventario> listarPorProducto(int idProducto);

    /** Suma "cantidad" al stock del producto en esa bodega (lo crea si no existía el renglón). */
    boolean acreditar(int idProducto, int idBodega, int cantidad);

    /**
     * Resta "cantidad" del stock del producto en esa bodega, solo si hay
     * suficiente disponible ahí mismo (operación atómica a nivel de fila:
     * evita que el stock de una bodega quede en negativo).
     */
    boolean descontar(int idProducto, int idBodega, int cantidad);
}
