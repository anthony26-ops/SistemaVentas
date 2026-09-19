package modelo;

import java.util.Date;

/**
 * Representa el pago aplicado a una factura.
 *
 * La factura se referencia por su identificador (idFactura) y no por el
 * objeto Factura, para que el modulo de Pagos pueda compilarse y probarse
 * de forma independiente del modulo de Facturacion.
 */
public class Pago {

    private int idPago;
    private int idFactura;
    private Date fecha;
    private double monto;
    private String tipoPago;
    private String referencia;
    private String estado;

    public Pago() {
    }

    public Pago(int idPago, int idFactura, Date fecha, double monto,
                String tipoPago, String referencia, String estado) {
        this.idPago = idPago;
        this.idFactura = idFactura;
        this.fecha = fecha;
        this.monto = monto;
        this.tipoPago = tipoPago;
        this.referencia = referencia;
        this.estado = estado;
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String mostrarInformacion() {
        return "Pago #" + idPago
                + " | Factura: " + idFactura
                + " | Monto: Q" + String.format("%.2f", monto)
                + " | Metodo: " + tipoPago
                + " | Estado: " + estado;
    }
}
