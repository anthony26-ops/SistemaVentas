package modelo;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Cuenta de acceso al sistema (login). Un Usuario está ligado a un Empleado
 * y define con qué rol entra ('ADMIN' | 'VENDEDOR' | 'BODEGA' | 'CAJA').
 *
 * @author anthonyjolon
 */
public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String contrasenaHash;
    private String rol;
    private boolean activo;
    private Empleado empleado;

    public Usuario(int idUsuario, String nombreUsuario, String contrasenaHash, String rol,
            boolean activo, Empleado empleado) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
        this.empleado = empleado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    /**
     * Verifica la contraseña en texto plano contra el hash guardado.
     * La contraseña nunca se compara ni se guarda en texto plano.
     */
    public boolean autenticar(String pwd) {
        return activo && contrasenaHash != null && pwd != null
                && BCrypt.checkpw(pwd, contrasenaHash);
    }
}
