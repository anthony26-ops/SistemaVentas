package dao.impl;

import bd.ConexionPostgreSQL;
import dao.FacturaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import modelo.Factura;
import modelo.Cliente;
import modelo.Empleado;

public class FacturaDAOImpl implements FacturaDAO {

    @Override
    public boolean registrarFactura(Factura factura) {
        String sql = "INSERT INTO facturas (fecha, id_cliente, id_empleado, total, estado) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDate(1, new java.sql.Date(factura.getFecha().getTime()));
            ps.setInt(2, factura.getCliente() != null ? factura.getCliente().getId() : 0);
            ps.setInt(3, factura.getEmpleado() != null ? factura.getEmpleado().getId() : 0);
            ps.setDouble(4, factura.getTotal());
            ps.setString(5, factura.getEstado());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (Exception e) {
            System.err.println("Error al registrar factura: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Factura> listarFacturas() {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT * FROM facturas";
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    
                    lista.add(factura);
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
        
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    factura = new Factura();
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
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener factura por ID: " + e.getMessage());
        }
        return factura;
    }
}