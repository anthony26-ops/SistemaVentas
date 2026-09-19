package modelo;

/**
 * Pago realizado en efectivo. Valida que el monto recibido cubra el total
 * y permite calcular el cambio que se le devuelve al cliente.
 */
public class PagoEfectivo implements IMetodoPago {

    private double montoRecibido;
    private String mensajeValidacion;

    public PagoEfectivo() {
    }

    public PagoEfectivo(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    @Override
    public String getTipo() {
        return "EFECTIVO";
    }

    @Override
    public boolean validar(double monto) {
        if (montoRecibido <= 0) {
            mensajeValidacion = "Debe ingresar el monto recibido en efectivo.";
            return false;
        }
        if (montoRecibido < monto) {
            mensajeValidacion = "El monto recibido es menor al total a pagar.";
            return false;
        }
        mensajeValidacion = "";
        return true;
    }

    @Override
    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    @Override
    public String generarReferencia() {
        return "EFECTIVO-" + System.currentTimeMillis();
    }

    /** Calcula el cambio a devolver al cliente. */
    public double calcularCambio(double monto) {
        return montoRecibido - monto;
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }
}
