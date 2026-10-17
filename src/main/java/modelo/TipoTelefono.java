package modelo;

/**
 * Tipos de teléfono permitidos para un cliente. En la base de datos se guarda
 * la etiqueta ("Personal", "Trabajo", "Casa", "Otro").
 */
public enum TipoTelefono {
    PERSONAL("Personal"),
    TRABAJO("Trabajo"),
    CASA("Casa"),
    OTRO("Otro");

    private final String etiqueta;

    TipoTelefono(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Convierte el texto guardado en la base de datos; un valor desconocido se trata como OTRO. */
    public static TipoTelefono desdeTexto(String texto) {
        if (texto != null) {
            for (TipoTelefono t : values()) {
                if (t.etiqueta.equalsIgnoreCase(texto.trim())) {
                    return t;
                }
            }
        }
        return OTRO;
    }

    /** Se muestra la etiqueta (así el combo de la pantalla ya sale con los textos correctos). */
    @Override
    public String toString() {
        return etiqueta;
    }
}
