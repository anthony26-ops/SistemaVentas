package bd;

/**
 * Excepción simple que envuelve los errores de conexión/SQL de la capa bd.
 *
 * @author anthonyjolon
 */
public class ConexionException extends Exception {

    public ConexionException(String mensaje) {
        super(mensaje);
    }

    public ConexionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
