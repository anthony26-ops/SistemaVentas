/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

public class Cliente extends Persona {
    private int idCliente;
    private String nit;

    public Cliente() {
        super();
    }

    public Cliente(int idPersona, String nombre, String apellido, String telefono, String email, int idCliente, String nit) {
        super(idPersona, nombre, apellido, telefono, email);
        this.idCliente = idCliente;
        this.nit = nit;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
}
