/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

public class Empleado extends Persona {
    private int idEmpleado;
    private String cargo;
    private double salario;

    public Empleado() {
        super();
    }

    public Empleado(int idPersona, String nombre, String apellido, String telefono, String email, int idEmpleado, String cargo, double salario) {
        super(idPersona, nombre, apellido, telefono, email);
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.salario = salario;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getPuesto() { return cargo; }
    public void setPuesto(String cargo) { this.cargo = cargo; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
}