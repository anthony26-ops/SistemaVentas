package dao.impl;

import bd.ConexionException;
import bd.ConexionMySQL;
import dao.OrdenDespachoDAO;
import dao.OrdenVentaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.DetalleOrdenDespacho;
import modelo.OrdenDespacho;
import modelo.Producto;

public class OrdenDespachoDAOImpl implements OrdenDespachoDAO {

    /**
     * Guarda la orden de despacho junto con todas sus lineas de detalle
     * dentro de una misma transaccion: si falla una linea, no se guarda nada.
     */
    @Override
    public boolean insertar(OrdenDespacho orden) {
        String sqlOrden = "INSERT INTO ordenes_despacho "
                + "(id_orden_venta, fecha_despacho, direccion_entrega, transportista, estado) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_orden_despacho "
                + "(id_orden_despacho, id_producto, nombre_producto, cantidad) "
                + "VALUES (?, ?, ?, ?)";

        Connection cn = null;
        try {
            cn = ConexionMySQL.getConnection();
            cn.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement ps = cn.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orden.getIdOrdenVenta());
                ps.setTimestamp(2, new Timestamp(orden.getFechaDespacho().getTime()));
                ps.setString(3, orden.getDireccionEntrega());
                ps.setString(4, orden.getTransportista());
                ps.setString(5, orden.getEstado());

                if (ps.executeUpdate() == 0) {
                    cn.rollback();
                    return false;
                }
                try (ResultSet generado = ps.getGeneratedKeys()) {
                    if (generado.next()) {
                        idGenerado = generado.getInt(1);
                        orden.setIdOrdenDespacho(idGenerado);
                    } else {
                        cn.rollback();
                        return false;
                    }
                }
            }

            if (orden.getDetalles() != null && !orden.getDetalles().isEmpty()) {
                try (PreparedStatement ps = cn.prepareStatement(sqlDetalle)) {
                    for (DetalleOrdenDespacho detalle : orden.getDetalles()) {
                        ps.setInt(1, idGenerado);
                        ps.setInt(2, detalle.getIdProducto());
                        ps.setString(3, detalle.getNombreProducto());
                        ps.setInt(4, detalle.getCantidad());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            cn.commit();

            // Al confirmar el despacho: descontar existencia de cada
            // producto despachado y marcar la orden de venta como
            // "Despachada".
            descontarExistencias(orden.getDetalles());
            OrdenVentaDAO ordenVentaDAO = new OrdenVentaDAOImpl();
            ordenVentaDAO.actualizarEstado(orden.getIdOrdenVenta(), "Despachada");

            return true;

        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void descontarExistencias(List<DetalleOrdenDespacho> detalles) {
        if (detalles == null) {
            return;
        }
        ProductoDAO productoDAO = new ProductoDAOImpl();
        for (DetalleOrdenDespacho detalle : detalles) {
            Producto producto = productoDAO.buscarPorId(detalle.getIdProducto());
            if (producto != null) {
                int nuevaExistencia = Math.max(producto.getExistencia() - detalle.getCantidad(), 0);
                productoDAO.actualizarExistencia(producto.getIdProducto(), nuevaExistencia);
            }
        }
    }

    @Override
    public boolean actualizarEstado(int idOrdenDespacho, String estado) {
        String sql = "UPDATE ordenes_despacho SET estado=? WHERE id_orden_despacho=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idOrdenDespacho);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idOrdenDespacho) {
        String sql = "DELETE FROM ordenes_despacho WHERE id_orden_despacho=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenDespacho);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public OrdenDespacho obtenerPorId(int idOrdenDespacho) {
        String sql = "SELECT * FROM ordenes_despacho WHERE id_orden_despacho=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenDespacho);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrdenDespacho orden = mapear(rs);
                    orden.setDetalles(listarDetalles(orden.getIdOrdenDespacho()));
                    return orden;
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<OrdenDespacho> listarTodos() {
        List<OrdenDespacho> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_despacho ORDER BY id_orden_despacho DESC";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<DetalleOrdenDespacho> listarDetalles(int idOrdenDespacho) {
        List<DetalleOrdenDespacho> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_orden_despacho WHERE id_orden_despacho=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenDespacho);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleOrdenDespacho detalle = new DetalleOrdenDespacho();
                    detalle.setIdDetalleDespacho(rs.getInt("id_detalle_despacho"));
                    detalle.setIdOrdenDespacho(rs.getInt("id_orden_despacho"));
                    detalle.setIdProducto(rs.getInt("id_producto"));
                    detalle.setNombreProducto(rs.getString("nombre_producto"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    lista.add(detalle);
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private OrdenDespacho mapear(ResultSet rs) throws SQLException {
        OrdenDespacho orden = new OrdenDespacho();
        orden.setIdOrdenDespacho(rs.getInt("id_orden_despacho"));
        orden.setIdOrdenVenta(rs.getInt("id_orden_venta"));
        orden.setFechaDespacho(rs.getTimestamp("fecha_despacho"));
        orden.setDireccionEntrega(rs.getString("direccion_entrega"));
        orden.setTransportista(rs.getString("transportista"));
        orden.setEstado(rs.getString("estado"));
        return orden;
    }
}
