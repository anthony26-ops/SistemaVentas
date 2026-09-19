package modelo;

/**
 * Pago realizado por transferencia o deposito bancario. Requiere el banco
 * emisor y el numero de boleta que respalda la operacion.
 */
public class PagoTransferencia implements IMetodoPago {

    private String banco;
    private String numeroBoleta;
    private String mensajeValidacion;

    public PagoTransferencia() {
    }

    public PagoTransferencia(String banco, String numeroBoleta) {
        this.banco = banco;
        this.numeroBoleta = numeroBoleta;
    }

    @Override
    public String getTipo() {
        return "TRANSFERENCIA";
    }

    @Override
    public boolean validar(double monto) {
        if (banco == null || banco.trim().isEmpty()) {
            mensajeValidacion = "Debe seleccionar el banco emisor.";
            return false;
        }
        if (numeroBoleta == null || numeroBoleta.trim().isEmpty()) {
            mensajeValidacion = "Debe ingresar el numero de boleta de la transferencia.";
            return false;
        }
        if (monto <= 0) {
            mensajeValidacion = "El monto transferido debe ser mayor a cero.";
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
        return "TRANSF-" + banco.trim().toUpperCase() + "-" + numeroBoleta.trim();
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getNumeroBoleta() {
        return numeroBoleta;
    }

    public void setNumeroBoleta(String numeroBoleta) {
        this.numeroBoleta = numeroBoleta;
    }
}
