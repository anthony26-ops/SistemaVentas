package dao.impl;

import bd.ConexionException;
import bd.ConexionMySQL;
import dao.TelefonoDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Telefono;
import modelo.TipoTelefono;

public class TelefonoDAOImpl implements TelefonoDAO {

    @Override
    public void insertarTodos(Connection cn, int idCliente, List<Telefono> telefonos) throws SQLException {
        if (telefonos == null || telefonos.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO telefonos_cliente (id_cliente, numero, tipo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Telefono t : telefonos) {
                ps.setInt(1, idCliente);
                ps.setString(2, t.getNumero());
                ps.setString(3, t.getTipo().getEtiqueta());
                ps.addBatch();
                t.setIdCliente(idCliente);
            }
            ps.executeBatch();
        }
    }

    @Override
    public void eliminarPorCliente(Connection cn, int idCliente) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM telefonos_cliente WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Telefono> listarPorCliente(int idCliente) {
        List<Telefono> lista = new ArrayList<>();
        String sql = "SELECT id_telefono, id_cliente, numero, tipo FROM telefonos_cliente "
                + "WHERE id_cliente = ? ORDER BY id_telefono";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Map<Integer, List<Telefono>> listarTodosPorCliente() {
        Map<Integer, List<Telefono>> mapa = new LinkedHashMap<>();
        String sql = "SELECT id_telefono, id_cliente, numero, tipo FROM telefonos_cliente "
                + "ORDER BY id_cliente, id_telefono";
        try (Connection cn = ConexionMySQL.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Telefono t = mapear(rs);
                mapa.computeIfAbsent(t.getIdCliente(), k -> new ArrayList<>()).add(t);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return mapa;
    }

    private Telefono mapear(ResultSet rs) throws SQLException {
        return new Telefono(
                rs.getInt("id_telefono"),
                rs.getInt("id_cliente"),
                rs.getString("numero"),
                TipoTelefono.desdeTexto(rs.getString("tipo")));
    }
}
