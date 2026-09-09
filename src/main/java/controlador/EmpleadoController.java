/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EmpleadoDAO;
import dao.impl.EmpleadoDAOImpl;
import modelo.Empleado;
import java.util.List;

public class EmpleadoController {
    private final EmpleadoDAO empleadoDAO;

    public EmpleadoController() {
        this.empleadoDAO = new EmpleadoDAOImpl();
    }

    public boolean guardarEmpleado(String nombre, String apellido, String telefono, String email, String cargo, double salario) {
        Empleado empleado = new Empleado(0, nombre, apellido, telefono, email, 0, cargo, salario);
        return empleadoDAO.insertar(empleado);
    }

    public boolean modificarEmpleado(int idEmpleado, String nombre, String apellido, String telefono, String email, String cargo, double salario) {
        Empleado empleado = new Empleado(0, nombre, apellido, telefono, email, idEmpleado, cargo, salario);
        return empleadoDAO.actualizar(empleado);
    }

    public boolean borrarEmpleado(int idEmpleado) {
        return empleadoDAO.eliminar(idEmpleado);
    }

    public List<Empleado> listarEmpleados() {
        return empleadoDAO.listarTodos();
    }
}
