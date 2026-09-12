package dao.impl;

import bd.ConexionPostgreSQL;
import dao.OrdenVentaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.OrdenVenta;

public class OrdenVentaDAOImpl implements OrdenVentaDAO {

    @Override
    public boolean registrarOrden(OrdenVenta orden) {
        String sql = "INSERT INTO ordenes_venta (fecha, id_cliente, id_empleado, total) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = ConexionPostgreSQL.getConnection();
            ps = con.prepareStatement(sql);
            
            // ps.executeUpdate();
            return true;
        } catch (Exception e) { // <--- ¡Este es el cambio clave!
            System.err.println("Error al registrar orden de venta: " + e.getMessage());
            return false;
        } finally {
            // Cierre de conexiones
        }
    }

    @Override
    public List<OrdenVenta> listarOrdenes() {
        List<OrdenVenta> lista = new ArrayList<>();
        return lista;
    }

    @Override
    public OrdenVenta obtenerOrdenPorId(int idOrden) {
        return null;
    }
}