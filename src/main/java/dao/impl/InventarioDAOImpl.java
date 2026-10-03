package dao.impl;

import bd.ConexionException;
import bd.ConexionMySQL;
import dao.InventarioDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Bodega;
import modelo.Inventario;
import modelo.Producto;

public class InventarioDAOImpl implements InventarioDAO {

    @Override
    public int obtenerCantidad(int idProducto, int idBodega) {
        String sql = "SELECT cantidad_disponible FROM ventas_inventario WHERE producto_id=? AND bodega_id=?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idBodega);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cantidad_disponible") : 0;
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int totalDisponible(int idProducto) {
        String sql = "SELECT COALESCE(SUM(cantidad_disponible), 0) AS total FROM ventas_inventario WHERE producto_id=?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Inventario> listarPorProducto(int idProducto) {
        List<Inventario> lista = new ArrayList<>();
        String sql = "SELECT i.id_inventario, i.cantidad_disponible, b.id_bodega, b.nombre, b.direccion, b.telefono "
                + "FROM ventas_inventario i JOIN ventas_bodegas b ON b.id_bodega = i.bodega_id "
                + "WHERE i.producto_id = ? ORDER BY b.nombre";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bodega bodega = new Bodega(rs.getInt("id_bodega"), rs.getString("nombre"),
                            rs.getString("direccion"), rs.getString("telefono"));
                    Producto producto = new Producto();
                    producto.setIdProducto(idProducto);
                    Inventario inv = new Inventario(rs.getInt("id_inventario"), producto,
                            rs.getInt("cantidad_disponible"), bodega);
                    lista.add(inv);
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean acreditar(int idProducto, int idBodega, int cantidad) {
        String sql = "INSERT INTO ventas_inventario (producto_id, bodega_id, cantidad_disponible) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE cantidad_disponible = cantidad_disponible + VALUES(cantidad_disponible)";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idBodega);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean descontar(int idProducto, int idBodega, int cantidad) {
        String sql = "UPDATE ventas_inventario SET cantidad_disponible = cantidad_disponible - ? "
                + "WHERE producto_id = ? AND bodega_id = ? AND cantidad_disponible >= ?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, idBodega);
            ps.setInt(4, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
