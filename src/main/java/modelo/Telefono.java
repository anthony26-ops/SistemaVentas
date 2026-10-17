package modelo;

/**
 * Un teléfono de un cliente. Un {@link Cliente} puede tener uno o varios
 * (relación 1 a N): cada fila de la tabla telefonos_cliente guarda el id del
 * cliente al que pertenece.
 */
public class Telefono {

    private int idTelefono;
    private int idCliente;
    private String numero;
    private TipoTelefono tipo;

    public Telefono() {
    }

    public Telefono(String numero, TipoTelefono tipo) {
        this.numero = numero;
        this.tipo = tipo;
    }

    public Telefono(int idTelefono, int idCliente, String numero, TipoTelefono tipo) {
        this.idTelefono = idTelefono;
        this.idCliente = idCliente;
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getIdTelefono() { return idTelefono; }
    public void setIdTelefono(int idTelefono) { this.idTelefono = idTelefono; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public TipoTelefono getTipo() { return tipo; }
    public void setTipo(TipoTelefono tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return numero + " (" + (tipo != null ? tipo.getEtiqueta() : "?") + ")";
    }
}
