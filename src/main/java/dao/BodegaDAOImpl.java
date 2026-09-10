/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import modelo.Bodega;
import bd.ConexionException;
import bd.ConexionPostgreSQL;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 *
 * @author garci
 */
public class BodegaDAOImpl implements BodegaDAO{
    @Override
    public void guardar(Bodega b){
        String sql = "INSERT INTO ventas_bodegas (id_bodega, nombre, direccion, telefono) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionPostgreSQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, b.getIdBodega());
            ps.setString(2, b.getNombre());
            ps.setString(3, b.getDireccion());
            ps.setString(4, b.getTelefono());
            ps.executeUpdate();
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List <Bodega> listar(){
        List<Bodega> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas_bodegas";
        try (Connection con = ConexionPostgreSQL.getConnection();
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