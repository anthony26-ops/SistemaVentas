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
     * producto, todo dentro de la misma transacción. Se usa para la
     * facturación manual (sin orden de venta detrás), donde nadie más
     * descontó inventario todavía.
     */
    @Override
    public boolean registrarFactura(Factura factura) {
        return registrarFacturaInterno(factura, true);
    }

    private boolean registrarFacturaInterno(Factura factura, boolean descontarInventario) {
        String sql = "INSERT INTO facturas (fecha, id_orden_venta, id_cliente, id_empleado, total, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionMySQL.getConnection()) {
            con.setAutoCommit(false);

            int idFactura;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
                if (factura.getIdOrdenVenta() != null) {
                    ps.setInt(2, factura.getIdOrdenVenta());
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                }
                ps.setInt(3, factura.getCliente() != null ? factura.getCliente().getId() : 0);
                ps.setInt(4, factura.getEmpleado() != null ? factura.getEmpleado().getId() : 0);
                ps.setDouble(5, factura.getTotal());
                ps.setString(6, factura.getEstado());
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

            if (descontarInventario && factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
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

    /**
     * Genera la factura a partir de una orden que YA fue despachada. El
     * flujo obligatorio del proyecto es Cliente -> OrdenVenta -> Despacho ->
     * Salida de inventario -> Facturación: el inventario se descuenta una
     * sola vez, al confirmar el despacho (OrdenDespachoDAOImpl), así que
     * aquí NO se vuelve a descontar.
     */
    @Override
    public Factura generarFacturaDesdeOrden(OrdenVenta orden) {
        OrdenVentaDAO ordenVentaDAO = new OrdenVentaDAOImpl();

        OrdenVenta ordenCompleta = ordenVentaDAO.obtenerOrdenPorId(orden.getIdOrdenVenta());
        if (ordenCompleta == null || ordenCompleta.getDetalles() == null || ordenCompleta.getDetalles().isEmpty()) {
            return null;
        }
        if (!"Despachada".equalsIgnoreCase(ordenCompleta.getEstado())) {
            System.err.println("No se puede facturar: la orden " + ordenCompleta.getIdOrdenVenta()
                    + " todavía no ha sido despachada (estado actual: " + ordenCompleta.getEstado() + ")");
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
        factura.setIdOrdenVenta(ordenCompleta.getIdOrdenVenta());
        factura.setCliente(ordenCompleta.getCliente());
        factura.setEmpleado(ordenCompleta.getEmpleado());
        factura.setDetalles(detallesFactura);
        factura.setTotal(total);
        factura.setEstado("Emitida");

        // false: el inventario ya se descontó al confirmar el despacho.
        if (!registrarFacturaInterno(factura, false)) {
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

    @Override
    public Factura obtenerFacturaPorOrden(int idOrdenVenta) {
        Factura factura = null;
        String sql = "SELECT * FROM facturas WHERE id_orden_venta = ?";

        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idOrdenVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    factura = mapearFactura(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener factura por orden: " + e.getMessage());
        }
        return factura;
    }

    private Factura mapearFactura(ResultSet rs) throws java.sql.SQLException {
        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("id"));
        int idOrden = rs.getInt("id_orden_venta");
        factura.setIdOrdenVenta(rs.wasNull() ? null : idOrden);
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
