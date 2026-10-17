/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.impl;

import bd.ConexionMySQL;
import bd.ConexionException; // Importante
import dao.ClienteDAO;
import dao.TelefonoDAO;
import modelo.Cliente;
import modelo.Telefono;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClienteDAOImpl implements ClienteDAO {

    private final TelefonoDAO telefonoDAO = new TelefonoDAOImpl();

    /**
     * Guarda el cliente y TODOS sus teléfonos en una sola transacción: o se guarda
     * todo o no se guarda nada (si falla un teléfono no queda un cliente a medias).
     * El id que genera MySQL para el cliente se lee y se pone en el objeto, porque
     * los teléfonos se ligan a ese id.
     */
    @Override
    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, apellido, email, nit) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionMySQL.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idGenerado;
                try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cliente.getNombre());
                    ps.setString(2, cliente.getApellido());
                    ps.setString(3, cliente.getEmail());
                    ps.setString(4, cliente.getNit());
                    if (ps.executeUpdate() == 0) {
                        cn.rollback();
                        return false;
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            cn.rollback();
                            return false;
                        }
                        idGenerado = keys.getInt(1);
                    }
                }
                cliente.setIdCliente(idGenerado);
                telefonoDAO.insertarTodos(cn, idGenerado, cliente.getTelefonos());
                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                cliente.setIdCliente(0);
                throw e;
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza los datos del cliente y reemplaza su lista de teléfonos (se borran
     * los anteriores y se insertan los actuales), todo en una sola transacción.
     */
    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre=?, apellido=?, email=?, nit=? WHERE id_cliente=?";
        try (Connection cn = ConexionMySQL.getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, cliente.getNombre());
                    ps.setString(2, cliente.getApellido());
                    ps.setString(3, cliente.getEmail());
                    ps.setString(4, cliente.getNit());
                    ps.setInt(5, cliente.getIdCliente());
                    if (ps.executeUpdate() == 0) {
                        cn.rollback();
                        return false;
                    }
                }
                telefonoDAO.eliminarPorCliente(cn, cliente.getIdCliente());
                telefonoDAO.insertarTodos(cn, cliente.getIdCliente(), cliente.getTelefonos());
                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Los teléfonos del cliente se borran solos (ON DELETE CASCADE en telefonos_cliente). */
    @Override
    public boolean eliminar(int idCliente) {
        String sql = "DELETE FROM clientes WHERE id_cliente=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Devuelve el cliente CON sus teléfonos leídos de la base de datos. */
    @Override
    public Cliente obtenerPorId(int idCliente) {
        String sql = "SELECT * FROM clientes WHERE id_cliente=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente c = mapear(rs);
                    c.setTelefonos(telefonoDAO.listarPorCliente(idCliente));
                    return c;
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Lista todos los clientes; los teléfonos de todos se traen en UNA sola consulta adicional. */
    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try (Connection cn = ConexionMySQL.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        Map<Integer, List<Telefono>> telefonos = telefonoDAO.listarTodosPorCliente();
        for (Cliente c : lista) {
            c.setTelefonos(telefonos.get(c.getIdCliente()));
        }
        return lista;
    }

    /** La columna clientes.telefono ya no se lee: los teléfonos vienen de telefonos_cliente. */
    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_persona"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            null,
            rs.getString("email"),
            rs.getInt("id_cliente"),
            rs.getString("nit")
        );
    }
}
