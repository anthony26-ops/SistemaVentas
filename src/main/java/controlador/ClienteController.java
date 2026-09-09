/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ClienteDAO;
import dao.impl.ClienteDAOImpl;
import modelo.Cliente;
import java.util.List;

public class ClienteController {
    private final ClienteDAO clienteDAO;

    public ClienteController() {
        this.clienteDAO = new ClienteDAOImpl();
    }

    public boolean guardarCliente(String nombre, String apellido, String telefono, String email, String nit) {
        Cliente cliente = new Cliente(0, nombre, apellido, telefono, email, 0, nit);
        return clienteDAO.insertar(cliente);
    }

    public boolean modificarCliente(int idCliente, String nombre, String apellido, String telefono, String email, String nit) {
        Cliente cliente = new Cliente(0, nombre, apellido, telefono, email, idCliente, nit);
        return clienteDAO.actualizar(cliente);
    }

    public boolean borrarCliente(int idCliente) {
        return clienteDAO.eliminar(idCliente);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }
}