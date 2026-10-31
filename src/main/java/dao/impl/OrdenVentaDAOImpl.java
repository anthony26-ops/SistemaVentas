package dao.impl;

import bd.ConexionMySQL;
import dao.OrdenVentaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.DetalleOrdenVenta;
import modelo.Empleado;
import modelo.OrdenVenta;
import modelo.Producto;

public class OrdenVentaDAOImpl implements OrdenVentaDAO {

    /**
     * Inserta la orden y, si trae detalles (orden.getDetalles()), los
     * guarda también en orden_venta_detalles dentro de la misma
     * transacción.
     */
    @Override
    public boolean registrarOrden(OrdenVenta orden) {
        String sql = "INSERT INTO ordenes_venta (fecha, id_cliente, id_empleado, total, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionMySQL.getConnection()) {
            con.setAutoCommit(false);

            int idOrden;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, new java.sql.Date(orden.getFecha().getTime()));
                ps.setInt(2, orden.getCliente() != null ? orden.getCliente().getId() : 0);
                ps.setInt(3, orden.getEmpleado() != null ? orden.getEmpleado().getId() : 0);
                ps.setDouble(4, orden.getTotal());
                ps.setString(5, orden.getEstado());
                if (ps.executeUpdate() == 0) {
                    return false;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        return false;
                    }
                    idOrden = keys.getInt(1);
                }
            }
            orden.setIdOrdenVenta(idOrden);

            if (orden.getDetalles() != null && !orden.getDetalles().isEmpty()) {
                String sqlDetalle = "INSERT INTO orden_venta_detalles (id_orden_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                    for (DetalleOrdenVenta d : orden.getDetalles()) {
                        psDet.setInt(1, idOrden);
                        psDet.setInt(2, d.getProducto().getIdProducto());
                        psDet.setInt(3, d.getCantidad());
                        psDet.setDouble(4, d.getPrecioUnitario());
                        psDet.addBatch();
                    }
                    psDet.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (Exception e) { // <--- Atrapa SQLException y ConexionException
            System.err.println("Error al registrar orden de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<OrdenVenta> listarOrdenes() {
        return listarPorFiltro(null);
    }

    @Override
    public List<OrdenVenta> listarPendientes() {
        return listarPorFiltro("Pendiente");
    }

    @Override
    public List<OrdenVenta> listarDespachadas() {
        return listarPorFiltro("Despachada");
    }

    private List<OrdenVenta> listarPorFiltro(String estado) {
        List<OrdenVenta> lista = new ArrayList<>();
        String sql = estado == null ? "SELECT * FROM ordenes_venta" : "SELECT * FROM ordenes_venta WHERE estado = ?";

        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (estado != null) {
                ps.setString(1, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOrden(rs));
                }
            }
        } catch (Exception e) { // <--- Corregido aquí
            System.err.println("Error al listar órdenes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public OrdenVenta obtenerOrdenPorId(int idOrden) {
        OrdenVenta orden = null;
        String sql = "SELECT * FROM ordenes_venta WHERE id = ?";

        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orden = mapearOrden(rs);
                    orden.setDetalles(cargarDetalles(con, idOrden));
                }
            }
        } catch (Exception e) { // <--- Corregido aquí
            System.err.println("Error al obtener orden por ID: " + e.getMessage());
        }
        return orden;
    }

    @Override
    public boolean actualizarEstado(int idOrden, String estado) {
        String sql = "UPDATE ordenes_venta SET estado = ? WHERE id = ?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idOrden);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar estado de la orden: " + e.getMessage());
            return false;
        }
    }

    private OrdenVenta mapearOrden(ResultSet rs) throws java.sql.SQLException {
        OrdenVenta orden = new OrdenVenta();
        orden.setIdOrdenVenta(rs.getInt("id"));
        orden.setFecha(rs.getDate("fecha"));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id_cliente"));
        orden.setCliente(cliente);

        Empleado empleado = new Empleado();
        empleado.setId(rs.getInt("id_empleado"));
        orden.setEmpleado(empleado);

        orden.setTotal(rs.getDouble("total"));
        orden.setEstado(rs.getString("estado"));
        return orden;
    }

    private List<DetalleOrdenVenta> cargarDetalles(Connection con, int idOrden) throws java.sql.SQLException {
        List<DetalleOrdenVenta> detalles = new ArrayList<>();
        // Antes solo traía id_producto, cantidad y precio_unitario de
        // orden_venta_detalles: el Producto quedaba con el nombre en null,
        // así que cualquier pantalla que mostrara o usara ese nombre (como
        // Despacho, que lo guarda en detalle_orden_despacho.nombre_producto,
        // columna NOT NULL) fallaba. Se agrega el JOIN para traer el nombre
        // real del catálogo.
        String sql = "SELECT d.id_producto, d.cantidad, d.precio_unitario, p.nombre "
                + "FROM orden_venta_detalles d "
                + "LEFT JOIN ventas_productos p ON p.id_producto = d.id_producto "
                + "WHERE d.id_orden_venta = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("id_producto"));
                    String nombre = rs.getString("nombre");
                    producto.setNombre(nombre != null ? nombre : "(producto eliminado del catálogo)");
                    DetalleOrdenVenta detalle = new DetalleOrdenVenta(0, null, producto,
                            rs.getInt("cantidad"), rs.getDouble("precio_unitario"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }
}
