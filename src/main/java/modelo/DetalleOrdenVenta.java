package modelo;

public class DetalleOrdenVenta {
    private int idDetalleOrdenVenta;
    private OrdenVenta ordenVenta;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleOrdenVenta() {
    }

    public DetalleOrdenVenta(int idDetalleOrdenVenta, OrdenVenta ordenVenta, Producto producto, int cantidad, double precioUnitario) {
        this.idDetalleOrdenVenta = idDetalleOrdenVenta;
        this.ordenVenta = ordenVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    public int getIdDetalleOrdenVenta() { return idDetalleOrdenVenta; }
    public void setIdDetalleOrdenVenta(int idDetalleOrdenVenta) { this.idDetalleOrdenVenta = idDetalleOrdenVenta; }

    public OrdenVenta getOrdenVenta() { return ordenVenta; }
    public void setOrdenVenta(OrdenVenta ordenVenta) { this.ordenVenta = ordenVenta; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}