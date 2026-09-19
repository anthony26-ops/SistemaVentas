package modelo;

/**
 * Pago realizado con tarjeta de credito o debito. Por seguridad solo se
 * almacenan los ultimos cuatro digitos dentro de la referencia.
 */
public class PagoTarjeta implements IMetodoPago {

    private String numeroTarjeta;
    private String titular;
    private String mensajeValidacion;

    public PagoTarjeta() {
    }

    public PagoTarjeta(String numeroTarjeta, String titular) {
        this.numeroTarjeta = numeroTarjeta;
        this.titular = titular;
    }

    @Override
    public String getTipo() {
        return "TARJETA";
    }

    @Override
    public boolean validar(double monto) {
        if (titular == null || titular.trim().isEmpty()) {
            mensajeValidacion = "Debe ingresar el nombre del titular de la tarjeta.";
            return false;
        }
        String numero = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("[\\s-]", "");
        if (!numero.matches("\\d{16}")) {
            mensajeValidacion = "El numero de tarjeta debe tener 16 digitos.";
            return false;
        }
        if (monto <= 0) {
            mensajeValidacion = "El monto a cobrar debe ser mayor a cero.";
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
        return "TARJETA-****" + obtenerUltimosCuatro();
    }

    /** Devuelve unicamente los ultimos cuatro digitos de la tarjeta. */
    public String obtenerUltimosCuatro() {
        String numero = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("[\\s-]", "");
        if (numero.length() < 4) {
            return "0000";
        }
        return numero.substring(numero.length() - 4);
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }
}
