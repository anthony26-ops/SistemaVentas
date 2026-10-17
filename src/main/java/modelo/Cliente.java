/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {
    private int idCliente;
    private String nit;

    // Un cliente tiene uno o varios teléfonos (relación 1 a N). Siempre
    // inicializada vacía para que nunca sea null.
    private List<Telefono> telefonos = new ArrayList<>();

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

    public List<Telefono> getTelefonos() { return telefonos; }

    public void setTelefonos(List<Telefono> telefonos) {
        this.telefonos = telefonos != null ? new ArrayList<>(telefonos) : new ArrayList<>();
        sincronizarTelefonoPrincipal();
    }

    public void agregarTelefono(Telefono telefono) {
        this.telefonos.add(telefono);
        sincronizarTelefonoPrincipal();
    }

    /** Texto con todos los teléfonos, por ejemplo "5555-1111 (Personal), 2222-3333 (Trabajo)". */
    public String getTelefonosResumen() {
        StringBuilder sb = new StringBuilder();
        for (Telefono t : telefonos) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(t);
        }
        return sb.toString();
    }

    /**
     * Persona.telefono (heredado, y usado por Empleado) sigue existiendo: en un
     * cliente refleja el primer teléfono de la lista, así getTelefono() sigue
     * devolviendo algo con sentido para el código que ya lo usaba.
     */
    private void sincronizarTelefonoPrincipal() {
        setTelefono(telefonos.isEmpty() ? null : telefonos.get(0).getNumero());
    }
}
