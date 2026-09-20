/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import modelo.Bodega;
import bd.ConexionException;
import bd.ConexionMySQL;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 *
 * @author garci
 */
public class BodegaDAOImpl implements BodegaDAO{
    @Override
    public boolean guardar(Bodega b) {
        String sql = "INSERT INTO ventas_bodegas (nombre, direccion, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getNombre());
            ps.setString(2, b.getDireccion());
            ps.setString(3, b.getTelefono());
            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setIdBodega(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public List <Bodega> listar(){
        List<Bodega> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas_bodegas";
        try (Connection con = ConexionMySQL.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bodega b = new Bodega();
                b.setIdBodega(rs.getInt("id_bodega"));
                b.setNombre(rs.getString("nombre"));
                b.setDireccion(rs.getString("direccion"));
                b.setTelefono(rs.getString("telefono"));
                lista.add(b);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        } 
        return lista;
    }
}