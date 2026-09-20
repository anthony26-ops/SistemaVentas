/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.impl;

import bd.ConexionMySQL;
import bd.ConexionException;
import dao.EmpleadoDAO;
import modelo.Empleado;
import modelo.Vendedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAOImpl implements EmpleadoDAO {

    @Override
    public boolean insertar(Empleado empleado) {
        String sql = "INSERT INTO empleados (nombre, apellido, telefono, email, puesto, salario, tipo_empleado, zona_venta, meta_ventas_mensual) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, empleado.getNombre());
            ps.setString(2, empleado.getApellido());
            ps.setString(3, empleado.getTelefono());
            ps.setString(4, empleado.getEmail());
            ps.setString(5, empleado.getPuesto());
            ps.setDouble(6, empleado.getSalario());
            if (empleado instanceof Vendedor vendedor) {
                ps.setString(7, "VENDEDOR");
                ps.setString(8, vendedor.getZonaVenta());
                ps.setDouble(9, vendedor.getMetaVentasMensual());
            } else {
                ps.setString(7, "GENERAL");
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.DECIMAL);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Empleado empleado) {
        String sql = "UPDATE empleados SET nombre=?, apellido=?, telefono=?, email=?, puesto=?, salario=?, "
                + "tipo_empleado=?, zona_venta=?, meta_ventas_mensual=? WHERE id_empleado=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, empleado.getNombre());
            ps.setString(2, empleado.getApellido());
            ps.setString(3, empleado.getTelefono());
            ps.setString(4, empleado.getEmail());
            ps.setString(5, empleado.getPuesto());
            ps.setDouble(6, empleado.getSalario());
            if (empleado instanceof Vendedor vendedor) {
                ps.setString(7, "VENDEDOR");
                ps.setString(8, vendedor.getZonaVenta());
                ps.setDouble(9, vendedor.getMetaVentasMensual());
            } else {
                ps.setString(7, "GENERAL");
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.DECIMAL);
            }
            ps.setInt(10, empleado.getIdEmpleado());
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idEmpleado) {
        String sql = "DELETE FROM empleados WHERE id_empleado=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Empleado obtenerPorId(int idEmpleado) {
        String sql = "SELECT * FROM empleados WHERE id_empleado=?";
        try (Connection cn = ConexionMySQL.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Empleado(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getInt("id_empleado"),
                        rs.getString("puesto"),
                        rs.getDouble("salario")
                    );
                }
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados";
        try (Connection cn = ConexionMySQL.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Empleado e = new Empleado(
                    rs.getInt("id_persona"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getInt("id_empleado"),
                    rs.getString("puesto"),
                    rs.getDouble("salario")
                );
                lista.add(e);
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Vendedor> listarVendedores() {
        List<Vendedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados WHERE tipo_empleado='VENDEDOR'";
        try (Connection cn = ConexionMySQL.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Vendedor(
                    rs.getInt("id_persona"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getInt("id_empleado"),
                    rs.getString("puesto"),
                    rs.getDouble("salario"),
                    rs.getString("zona_venta"),
                    rs.getDouble("meta_ventas_mensual")
                ));
            }
        } catch (SQLException | ConexionException e) {
            e.printStackTrace();
        }
        return lista;
    }
}