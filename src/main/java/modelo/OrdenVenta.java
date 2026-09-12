package modelo;

import java.util.Date;
import java.util.List;

public class OrdenVenta {
    private int idOrdenVenta;
    private Date fecha;
    private Cliente cliente;
    private Empleado empleado;
    private List<DetalleOrdenVenta> detalles;
    private double total;

    public OrdenVenta() {
    }

    public OrdenVenta(int idOrdenVenta, Date fecha, Cliente cliente, Empleado empleado, List<DetalleOrdenVenta> detalles, double total) {
        this.idOrdenVenta = idOrdenVenta;
        this.fecha = fecha;
        this.cliente = cliente;
        this.empleado = empleado;
        this.detalles = detalles;
        this.total = total;
    }

    public int getIdOrdenVenta() { return idOrdenVenta; }
    public void setIdOrdenVenta(int idOrdenVenta) { this.idOrdenVenta = idOrdenVenta; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public List<DetalleOrdenVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOrdenVenta> detalles) { this.detalles = detalles; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}