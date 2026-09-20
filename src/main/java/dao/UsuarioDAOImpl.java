package dao;

import bd.ConexionException;
import bd.ConexionMySQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Empleado;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/**
 * @author anthonyjolon
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public void guardar(Usuario u) throws ConexionException {
        String sql = "INSERT INTO ventas_usuarios "
                + "(id_usuario, empleado_id, nombre_usuario, contrasena_hash, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "empleado_id = VALUES(empleado_id), "
                + "nombre_usuario = VALUES(nombre_usuario), "
                + "contrasena_hash = VALUES(contrasena_hash), "
                + "rol = VALUES(rol), "
                + "activo = VALUES(activo)";

        try (Connection conexion = ConexionMySQL.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, u.getIdUsuario());
            ps.setInt(2, u.getEmpleado().getIdEmpleado());
            ps.setString(3, u.getNombreUsuario());
            ps.setString(4, hashSiNoLoEsta(u.getContrasenaHash()));
            ps.setString(5, u.getRol());
            ps.setBoolean(6, u.isActivo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ConexionException("No se pudo guardar el usuario " + u.getNombreUsuario(), e);
        }
    }

    @Override
    public Usuario buscarPorUsuario(String nombreUsuario) throws ConexionException {
        String sql = "SELECT id_usuario, empleado_id, nombre_usuario, contrasena_hash, rol, activo "
                + "FROM ventas_usuarios WHERE nombre_usuario = ?";

        try (Connection conexion = ConexionMySQL.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                // Solo se enlaza el id del empleado; los datos completos
                // (nombre, puesto, etc.) los carga EmpleadoDAO por separado.
                Empleado empleado = new Empleado();
                empleado.setIdEmpleado(rs.getInt("empleado_id"));

                return new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("contrasena_hash"),
                        rs.getString("rol"),
                        rs.getBoolean("activo"),
                        empleado);
            }
        } catch (SQLException e) {
            throw new ConexionException("No se pudo buscar el usuario " + nombreUsuario, e);
        }
    }

    /**
     * Nunca se guarda una contraseña en texto plano: si el valor recibido no
     * trae ya el formato de hash de BCrypt, se hashea antes de insertarlo.
     */
    private String hashSiNoLoEsta(String contrasena) {
        if (contrasena != null && (contrasena.startsWith("$2a$") || contrasena.startsWith("$2b$"))) {
            return contrasena;
        }
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }
}
