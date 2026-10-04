package modelo;

/**
 * Contrato que deben cumplir todos los metodos de pago del sistema.
 * Permite aplicar polimorfismo: el controlador trabaja con IMetodoPago sin
 * conocer la implementación concreta. Por ahora la única implementación es
 * {@link PagoEfectivo} (se quitaron Tarjeta y Transferencia porque no
 * validaban nada real), pero la interfaz queda lista para agregar otro
 * método de pago el día que haga falta.
 *
 * Modulo de Pagos y Orden de Despacho.
 */
public interface IMetodoPago {

    /** Nombre corto del metodo de pago (por ahora, solo EFECTIVO). */
    String getTipo();

    /** Valida que el pago se pueda realizar por el monto indicado. */
    boolean validar(double monto);

    /** Devuelve el mensaje de error de la ultima validacion fallida. */
    String getMensajeValidacion();

    /** Genera la referencia que quedara guardada en la base de datos. */
    String generarReferencia();
}
