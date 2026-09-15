package dao.impl;

import bd.ConexionPostgreSQL;
import dao.OrdenVentaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import modelo.OrdenVenta;
import modelo.Cliente;
import modelo.Empleado;

public class OrdenVentaDAOImpl implements OrdenVentaDAO {

    @Override
    public boolean registrarOrden(OrdenVenta orden) {
        String sql = "INSERT INTO ordenes_venta (fecha, id_cliente, id_empleado, total, estado) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDate(1, new java.sql.Date(orden.getFecha().getTime()));
            ps.setInt(2, orden.getCliente() != null ? orden.getCliente().getId() : 0);
            ps.setInt(3, orden.getEmpleado() != null ? orden.getEmpleado().getId() : 0);
            ps.setDouble(4, orden.getTotal());
            ps.setString(5, orden.getEstado());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (Exception e) { // <--- Atrapa SQLException y ConexionException
            System.err.println("Error al registrar orden de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<OrdenVenta> listarOrdenes() {
        List<OrdenVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_venta";
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    
                    lista.add(orden);
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
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orden = new OrdenVenta();
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
                }
            }
        } catch (Exception e) { // <--- Corregido aquí
            System.err.println("Error al obtener orden por ID: " + e.getMessage());
        }
        return orden;
    }
}