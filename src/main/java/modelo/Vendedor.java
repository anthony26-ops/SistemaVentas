package modelo;

/**
 * Especialización de Empleado para el personal de ventas. Se guarda en la
 * misma tabla "empleados" (patrón "tabla por jerarquía", tal como indica el
 * documento de organización), diferenciada por la columna tipo_empleado.
 *
 * @author anthonyjolon
 */
public class Vendedor extends Empleado {

    private String zonaVenta;
    private double metaVentasMensual;

    public Vendedor(int idPersona, String nombre, String apellido, String telefono, String email,
            int idEmpleado, String cargo, double salario, String zonaVenta, double metaVentasMensual) {
        super(idPersona, nombre, apellido, telefono, email, idEmpleado, cargo, salario);
        this.zonaVenta = zonaVenta;
        this.metaVentasMensual = metaVentasMensual;
    }

    public String getZonaVenta() {
        return zonaVenta;
    }

    public void setZonaVenta(String zonaVenta) {
        this.zonaVenta = zonaVenta;
    }

    public double getMetaVentasMensual() {
        return metaVentasMensual;
    }

    public void setMetaVentasMensual(double metaVentasMensual) {
        this.metaVentasMensual = metaVentasMensual;
    }

    @Override
    public String toString() {
        return getNombre() + " " + getApellido() + " (Vendedor - " + zonaVenta + ")";
    }
}
