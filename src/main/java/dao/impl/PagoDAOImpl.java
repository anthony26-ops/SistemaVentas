package dao.impl;

import bd.ConexionException;
import bd.ConexionMySQL;
import dao.PagoDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Pago;

public class PagoDAOImpl implements PagoDAO {

    @Override
    public boolean insertar(Pago pago) {
        String sql = "INSERT INTO pagos (id_factura, fecha, monto, tipo_pago, referencia, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionMySQL.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pago.getIdFactura());
                ps.setTimestamp(2, new Timestamp(pago.getFecha().getTime()));
                ps.setDouble(3, pago.getMonto());
                ps.setString(4, pago.getTipoPago());
                ps.setString(5, pago.getReferencia());
                ps.setString(6, pago.getEstado());

                if (ps.executeUpdate() == 0) {
                    return false;
                }
                try (ResultSet generado = ps.getGeneratedKeys()) {
                    if (generado.next()) {
                        pago.setIdPago(generado.getInt(1));
                    }
                }
            }

            actualizarEstadoFacturaSiCompleta(cn, pago.getIdFactura());

            cn.commit();
            return true;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Si la suma de pagos (no anulados) de la factura alcanza o supera su
     * total, marca la factura como "Pagada".
     */
    private void actualizarEstadoFacturaSiCompleta(Connection cn, int idFactura) throws SQLException {
        double total = 0;
        double pagado = 0;

        try (PreparedStatement ps = cn.prepareStatement("SELECT total FROM facturas WHERE id = ?")) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return; // la factura no existe (o el catálogo usa otro id)
                }
                total = rs.getDouble("total");
            }
        }

        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COALESCE(SUM(monto), 0) AS total FROM pagos WHERE id_factura = ? AND estado <> 'ANULADO'")) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pagado = rs.getDouble("total");
                }
            }
        }

        if (pagado >= total) {
            try (PreparedStatement ps = cn.prepareStatement("UPDATE facturas SET estado = 'Pagada' WHERE id = ?")) {
                ps.setInt(1, idFactura);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public boolean actualizar(Pago pago) {
        String sql = "UPDATE pagos SET id_factura=?, monto=?, tipo_pago=?, referencia=?, estado=? "
                   + "WHERE id_pago=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdFactura());
            ps.setDouble(2, pago.getMonto());
            ps.setString(3, pago.getTipoPago());
            ps.setString(4, pago.getReferencia());
            ps.setString(5, pago.getEstado());
            ps.setInt(6, pago.getIdPago());
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idPago) {
        String sql = "DELETE FROM pagos WHERE id_pago=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Pago obtenerPorId(int idPago) {
        String sql = "SELECT * FROM pagos WHERE id_pago=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Pago> listarTodos() {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos ORDER BY id_pago DESC";
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
    public List<Pago> listarPorFactura(int idFactura) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_factura=? ORDER BY id_pago";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
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
    public double totalPagadoPorFactura(int idFactura) {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM pagos "
                   + "WHERE id_factura=? AND estado <> 'ANULADO'";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Convierte una fila del ResultSet en un objeto Pago. */
    private Pago mapear(ResultSet rs) throws SQLException {
        Pago pago = new Pago();
        pago.setIdPago(rs.getInt("id_pago"));
        pago.setIdFactura(rs.getInt("id_factura"));
        pago.setFecha(rs.getTimestamp("fecha"));
        pago.setMonto(rs.getDouble("monto"));
        pago.setTipoPago(rs.getString("tipo_pago"));
        pago.setReferencia(rs.getString("referencia"));
        pago.setEstado(rs.getString("estado"));
        return pago;
    }
}
