package controlador;

import bd.ConexionException;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Usuario;

/**
 * @author anthonyjolon
 */
public class UsuarioController {

    private static final Logger logger = Logger.getLogger(UsuarioController.class.getName());

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    /**
     * Valida usuario y contraseña contra la base de datos.
     *
     * @return el Usuario autenticado, o null si el usuario no existe, está
     * inactivo o la contraseña es incorrecta.
     */
    public Usuario login(String nombreUsuario, String pwd) {
        try {
            Usuario usuario = usuarioDAO.buscarPorUsuario(nombreUsuario);
            if (usuario != null && usuario.autenticar(pwd)) {
                return usuario;
            }
        } catch (ConexionException ex) {
            logger.log(Level.SEVERE, "Error al validar el login de " + nombreUsuario, ex);
        }
        return null;
    }
}
