package dao.impl;

import bd.ConexionMySQL;
import dao.FacturaDAO;
import dao.OrdenVentaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.DetalleFactura;
import modelo.DetalleOrdenVenta;
import modelo.Empleado;
import modelo.Factura;
import modelo.OrdenVenta;
import modelo.Producto;

public class FacturaDAOImpl implements FacturaDAO {

    /**
     * Inserta la factura y, si trae detalles (factura.getDetalles()), los
     * guarda en factura_detalles y descuenta la existencia de cada
     * producto, todo dentro de la misma transacción.
     */
    @Override
    public boolean registrarFactura(Factura factura) {
        String sql = "INSERT INTO facturas (fecha, id_cliente, id_empleado, total, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionMySQL.getConnection()) {
            con.setAutoCommit(false);

            int idFactura;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
                ps.setInt(2, factura.getCliente() != null ? factura.getCliente().getId() : 0);
                ps.setInt(3, factura.getEmpleado() != null ? factura.getEmpleado().getId() : 0);
                ps.setDouble(4, factura.getTotal());
                ps.setString(5, factura.getEstado());
                if (ps.executeUpdate() == 0) {
                    return false;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        return false;
                    }
                    idFactura = keys.getInt(1);
                }
            }
            factura.setIdFactura(idFactura);

            if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
                guardarDetalles(con, idFactura, factura.getDetalles());
            }

            // Se hace commit de la factura + detalle ANTES de tocar el
            // inventario: ProductoDAO.actualizarExistencia() abre su propia
            // conexión, y si la transacción de esta seguía abierta, el
            // UPDATE de ventas_productos quedaba bloqueado por el INSERT en
            // factura_detalles (que toma un lock por su FK a ese producto)
            // hasta hacer timeout.
            con.commit();

            if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
                descontarExistencias(factura.getDetalles());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar factura: " + e.getMessage());
            return false;
        }
    }

    private void guardarDetalles(Connection con, int idFactura, List<DetalleFactura> detalles)
            throws java.sql.SQLException {
        String sqlDetalle = "INSERT INTO factura_detalles (id_factura, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
            for (DetalleFactura d : detalles) {
                psDet.setInt(1, idFactura);
                psDet.setInt(2, d.getProducto().getIdProducto());
                psDet.setInt(3, d.getCantidad());
                psDet.setDouble(4, d.getPrecioUnitario());
                psDet.addBatch();
            }
            psDet.executeBatch();
        }
    }

    private void descontarExistencias(List<DetalleFactura> detalles) {
        ProductoDAO productoDAO = new ProductoDAOImpl();
        for (DetalleFactura d : detalles) {
            Producto producto = productoDAO.buscarPorId(d.getProducto().getIdProducto());
            if (producto != null) {
                int nuevaExistencia = Math.max(producto.getExistencia() - d.getCantidad(), 0);
                productoDAO.actualizarExistencia(producto.getIdProducto(), nuevaExistencia);
            }
        }
    }

    @Override
    public Factura generarFacturaDesdeOrden(OrdenVenta orden) {
        OrdenVentaDAO ordenVentaDAO = new OrdenVentaDAOImpl();

        OrdenVenta ordenCompleta = ordenVentaDAO.obtenerOrdenPorId(orden.getIdOrdenVenta());
        if (ordenCompleta == null || ordenCompleta.getDetalles() == null || ordenCompleta.getDetalles().isEmpty()) {
            return null;
        }

        List<DetalleFactura> detallesFactura = new ArrayList<>();
        double total = 0;
        for (DetalleOrdenVenta d : ordenCompleta.getDetalles()) {
            DetalleFactura df = new DetalleFactura();
            df.setProducto(d.getProducto());
            df.setPrecioUnitario(d.getPrecioUnitario());
            df.setCantidad(d.getCantidad());
            detallesFactura.add(df);
            total += df.getSubtotal();
        }

        Factura factura = new Factura();
        factura.setFecha(new java.sql.Date(System.currentTimeMillis()));
        factura.setCliente(ordenCompleta.getCliente());
        factura.setEmpleado(ordenCompleta.getEmpleado());
        factura.setDetalles(detallesFactura);
        factura.setTotal(total);
        factura.setEstado("Emitida");

        if (!registrarFactura(factura)) {
            return null;
        }

        ordenVentaDAO.actualizarEstado(ordenCompleta.getIdOrdenVenta(), "Facturada");
        return factura;
    }

    @Override
    public List<Factura> listarFacturas() {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT * FROM facturas";

        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFactura(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar facturas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Factura obtenerFacturaPorId(int idFactura) {
        Factura factura = null;
        String sql = "SELECT * FROM facturas WHERE id = ?";

        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    factura = mapearFactura(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener factura por ID: " + e.getMessage());
        }
        return factura;
    }

    private Factura mapearFactura(ResultSet rs) throws java.sql.SQLException {
        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("id"));
        factura.setFecha(rs.getDate("fecha"));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id_cliente"));
        factura.setCliente(cliente);

        Empleado empleado = new Empleado();
        empleado.setId(rs.getInt("id_empleado"));
        factura.setEmpleado(empleado);

        factura.setTotal(rs.getDouble("total"));
        factura.setEstado(rs.getString("estado"));
        return factura;
    }
}
