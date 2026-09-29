/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import modelo.Producto;
import modelo.Categoria;
import modelo.Proveedor;
import bd.ConexionMySQL;
import bd.ConexionException;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 *
 * @author garci
 */
public class ProductoDAOImpl implements ProductoDAO {
    @Override
    public void guardar(Producto p) {
        String sql = "INSERT INTO ventas_productos (id_producto, nombre, descripcion, precio, existencia, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdProducto());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getExistencia());
            ps.setInt(6, p.getCategoria().getIdCategoria());
            ps.setInt(7, p.getProveedor().getIdProveedor());            
            ps.executeUpdate();
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean actualizar(Producto p) {
        String sql = "UPDATE ventas_productos SET nombre=?, descripcion=?, precio=?, existencia=?, categoria_id=?, proveedor_id=? WHERE id_producto=?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getExistencia());
            ps.setInt(5, p.getCategoria().getIdCategoria());
            ps.setInt(6, p.getProveedor().getIdProveedor());
            ps.setInt(7, p.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        String sql = "DELETE FROM ventas_productos WHERE id_producto=?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Producto> listar() {
    List<Producto> lista = new ArrayList<>();
    String sql = "SELECT * FROM ventas_productos";
    try (Connection con = ConexionMySQL.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            Categoria cat = new Categoria();
            cat.setIdCategoria(rs.getInt("categoria_id"));
            Proveedor prov = new Proveedor();
            prov.setIdProveedor(rs.getInt("proveedor_id"));
            Producto p = new Producto();
            p.setIdProducto(rs.getInt("id_producto"));
            p.setNombre(rs.getString("nombre"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setPrecio(rs.getDouble("precio"));
            p.setExistencia(rs.getInt("existencia"));
            p.setCategoria(cat);
            p.setProveedor(prov);
            lista.add(p);
        }
    } catch (SQLException | ConexionException e) {
            e.printStackTrace();
    }
    return lista;
}
    
    @Override
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM ventas_productos WHERE id_producto = ?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                        Categoria cat = new Categoria();
                        cat.setIdCategoria(rs.getInt("categoria_id"));
                        Proveedor prov = new Proveedor();
                        prov.setIdProveedor(rs.getInt("proveedor_id"));
                        Producto p = new Producto();
                        p.setIdProducto(rs.getInt("id_producto"));
                        p.setNombre(rs.getString("nombre"));
                        p.setDescripcion(rs.getString("descripcion"));
                        p.setPrecio(rs.getDouble("precio"));
                        p.setExistencia(rs.getInt("existencia"));
                        p.setCategoria(cat);
                        p.setProveedor(prov);
                        return p;
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public void actualizarExistencia(int idProducto, int nuevaExistencia) {
        String sql = "UPDATE ventas_productos SET existencia = ? WHERE id_producto = ?";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevaExistencia);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
            } catch (SQLException | ConexionException e) {
                e.printStackTrace();
            }
    }
    
    public List<Categoria> listarCategorias() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas_categorias";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));

                lista.add(c);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }
    /** Inserta una categoría nueva y devuelve el registro con el id autogenerado, o null si falló. */
    public Categoria insertarCategoria(String nombre, String descripcion) {
        String sql = "INSERT INTO ventas_categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    return null;
                }
                return new Categoria(keys.getInt(1), nombre, descripcion);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Inserta un proveedor nuevo y devuelve el registro con el id autogenerado, o null si falló. */
    public Proveedor insertarProveedor(String nombreEmpresa, String telefono, String direccion) {
        String sql = "INSERT INTO ventas_proveedores (nombre_empresa, telefono, direccion) VALUES (?, ?, ?)";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreEmpresa);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    return null;
                }
                return new Proveedor(keys.getInt(1), nombreEmpresa, telefono, direccion);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas_proveedores";
        try (Connection con = ConexionMySQL.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("id_proveedor"));
                p.setNombreEmpresa(rs.getString("nombre_empresa"));
                p.setTelefono(rs.getString("telefono"));
                p.setDireccion(rs.getString("direccion"));
                
                // setters aquí: id_proveedor, nombre_empresa, telefono, direccion
                lista.add(p);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }
}