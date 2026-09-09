/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;
import modelo.Empleado;

public interface EmpleadoDAO {
    boolean insertar(Empleado empleado);
    boolean actualizar(Empleado empleado);
    boolean eliminar(int idEmpleado);
    Empleado obtenerPorId(int idEmpleado);
    List<Empleado> listarTodos();
}