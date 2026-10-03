package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orden de despacho generada a partir de una orden de venta ya pagada.
 * Contiene los datos de entrega y el listado de productos a despachar.
 */
public class OrdenDespacho {

    private int idOrdenDespacho;
    private int idOrdenVenta;
    private Integer idBodega;
    private Date fechaDespacho;
    private String direccionEntrega;
    private String transportista;
    private String estado;
    private List<DetalleOrdenDespacho> detalles;

    public OrdenDespacho() {
        this.detalles = new ArrayList<>();
    }

    public OrdenDespacho(int idOrdenDespacho, int idOrdenVenta, Date fechaDespacho,
                         String direccionEntrega, String transportista, String estado) {
        this.idOrdenDespacho = idOrdenDespacho;
        this.idOrdenVenta = idOrdenVenta;
        this.fechaDespacho = fechaDespacho;
        this.direccionEntrega = direccionEntrega;
        this.transportista = transportista;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    public int getIdOrdenDespacho() { return idOrdenDespacho; }
    public void setIdOrdenDespacho(int idOrdenDespacho) { this.idOrdenDespacho = idOrdenDespacho; }

    public int getIdOrdenVenta() { return idOrdenVenta; }
    public void setIdOrdenVenta(int idOrdenVenta) { this.idOrdenVenta = idOrdenVenta; }

    public Integer getIdBodega() { return idBodega; }
    public void setIdBodega(Integer idBodega) { this.idBodega = idBodega; }

    public Date getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(Date fechaDespacho) { this.fechaDespacho = fechaDespacho; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getTransportista() { return transportista; }
    public void setTransportista(String transportista) { this.transportista = transportista; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetalleOrdenDespacho> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOrdenDespacho> detalles) { this.detalles = detalles; }

    /** Agrega una linea de producto a la orden de despacho. */
    public void agregarDetalle(DetalleOrdenDespacho detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
    }

    /** Suma la cantidad total de unidades que se van a despachar. */
    public int calcularTotalUnidades() {
        int total = 0;
        if (detalles != null) {
            for (DetalleOrdenDespacho d : detalles) {
                total += d.getCantidad();
            }
        }
        return total;
    }
}
