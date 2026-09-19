package modelo;

/**
 * Contrato que deben cumplir todos los metodos de pago del sistema.
 * Permite aplicar polimorfismo: el controlador trabaja con IMetodoPago
 * sin conocer si el pago es en efectivo, con tarjeta o por transferencia.
 *
 * Modulo de Pagos y Orden de Despacho.
 */
public interface IMetodoPago {

    /** Nombre corto del metodo de pago (EFECTIVO, TARJETA, TRANSFERENCIA). */
    String getTipo();

    /** Valida que el pago se pueda realizar por el monto indicado. */
    boolean validar(double monto);

    /** Devuelve el mensaje de error de la ultima validacion fallida. */
    String getMensajeValidacion();

    /** Genera la referencia que quedara guardada en la base de datos. */
    String generarReferencia();
}
