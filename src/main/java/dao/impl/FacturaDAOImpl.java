package dao.impl;

import bd.ConexionPostgreSQL;
import dao.FacturaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Factura;

public class FacturaDAOImpl implements FacturaDAO {

    @Override
    public boolean registrarFactura(Factura factura) {
        String sql = "INSERT INTO facturas (fecha, id_cliente, id_empleado, total) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = ConexionPostgreSQL.getConnection(); 
            ps = con.prepareStatement(sql);
            // Aquí luego mapearemos los datos: ps.setDate(1, ...), etc.
            
            // ps.executeUpdate();
            return true;
        } catch (Exception e) { // <--- ¡Este es el cambio clave!
            System.err.println("Error al registrar factura: " + e.getMessage());
            return false;
        } finally {
            // Aquí cerramos conexiones
        }
    }

    @Override
    public List<Factura> listarFacturas() {
        List<Factura> lista = new ArrayList<>();
        // Lógica SQL para SELECT * FROM facturas
        return lista;
    }

    @Override
    public Factura obtenerFacturaPorId(int idFactura) {
        // Lógica SQL para buscar una factura
        return null;
    }
}