package modelo;

/**
 * Linea de una orden de despacho: indica que producto se despacha y en
 * que cantidad. El producto se referencia por su identificador para no
 * depender del modulo de Inventario.
 */
public class DetalleOrdenDespacho {

    private int idDetalleDespacho;
    private int idOrdenDespacho;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;

    public DetalleOrdenDespacho() {
    }

    public DetalleOrdenDespacho(int idDetalleDespacho, int idOrdenDespacho,
                                int idProducto, String nombreProducto, int cantidad) {
        this.idDetalleDespacho = idDetalleDespacho;
        this.idOrdenDespacho = idOrdenDespacho;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
    }

    public int getIdDetalleDespacho() { return idDetalleDespacho; }
    public void setIdDetalleDespacho(int idDetalleDespacho) { this.idDetalleDespacho = idDetalleDespacho; }

    public int getIdOrdenDespacho() { return idOrdenDespacho; }
    public void setIdOrdenDespacho(int idOrdenDespacho) { this.idOrdenDespacho = idOrdenDespacho; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
