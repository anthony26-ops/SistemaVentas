package dao;

import bd.ConexionException;
import modelo.Usuario;

/**
 * @author anthonyjolon
 */
public interface UsuarioDAO {

    void guardar(Usuario u) throws ConexionException;

    Usuario buscarPorUsuario(String nombreUsuario) throws ConexionException;
}
