package modelo;

import java.util.Date;
import java.util.List;

public class Factura {
    private int idFactura;
    private Date fecha;
    private Cliente cliente;
    private Empleado empleado; 
    private List<DetalleFactura> detalles;
    private double total;
    private String estado;

    public Factura() {
    }

    public Factura(int idFactura, Date fecha, Cliente cliente, Empleado empleado, List<DetalleFactura> detalles, double total) {
        this.idFactura = idFactura;
        this.fecha = fecha;
        this.cliente = cliente;
        this.empleado = empleado;
        this.detalles = detalles;
        this.total = total;
    }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
}
