/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ClienteDAO;
import dao.impl.ClienteDAOImpl;
import modelo.Cliente;
import modelo.Telefono;
import modelo.TipoTelefono;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controlador de Clientes. En la arquitectura de este proyecto (MVC + DAO) NO hay
 * una capa "Servicio" separada: la lógica de negocio vive en los controladores
 * (igual que PagoController o OrdenVentaController), y este controlador cumple
 * ese papel para los clientes y sus teléfonos: valida los teléfonos y orquesta
 * guardar/modificar el cliente junto con su lista. No importa nada de Swing.
 *
 * Un cliente puede tener uno o varios teléfonos (número + tipo).
 */
public class ClienteController {

    /** Cantidad mínima y máxima de dígitos de un número de teléfono. */
    private static final int MIN_DIGITOS = 7;
    private static final int MAX_DIGITOS = 15;
    /** Largo máximo del texto del número (coincide con telefonos_cliente.numero VARCHAR(20)). */
    private static final int MAX_LARGO = 20;

    private final ClienteDAO clienteDAO;

    public ClienteController() {
        this.clienteDAO = new ClienteDAOImpl();
    }

    /**
     * Guarda un cliente nuevo junto con sus teléfonos.
     *
     * @throws Exception con un mensaje claro si falta un dato o algún teléfono no es válido.
     * @return false si la base de datos no pudo guardar (no queda nada a medias).
     */
    public boolean guardarCliente(String nombre, String apellido, String email, String nit,
            List<Telefono> telefonos) throws Exception {
        Cliente cliente = armarCliente(0, nombre, apellido, email, nit, telefonos);
        return clienteDAO.insertar(cliente);
    }

    /** Modifica los datos del cliente y reemplaza su lista de teléfonos por la recibida. */
    public boolean modificarCliente(int idCliente, String nombre, String apellido, String email, String nit,
            List<Telefono> telefonos) throws Exception {
        Cliente cliente = armarCliente(idCliente, nombre, apellido, email, nit, telefonos);
        return clienteDAO.actualizar(cliente);
    }

    public boolean borrarCliente(int idCliente) {
        return clienteDAO.eliminar(idCliente);
    }

    /** El cliente con sus teléfonos leídos de la base de datos (o null si no existe). */
    public Cliente obtenerCliente(int idCliente) {
        return clienteDAO.obtenerPorId(idCliente);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }

    /**
     * Valida UN teléfono (número y tipo) frente a los que el cliente ya tiene, y devuelve
     * el Telefono listo para agregar (con el número sin espacios sobrantes).
     *
     * @param existentes teléfonos que ya están en la lista (sin incluir el que se está
     *                   modificando, si se está modificando uno) para detectar repetidos.
     */
    public Telefono crearTelefonoValidado(String numero, TipoTelefono tipo, List<Telefono> existentes)
            throws Exception {
        if (tipo == null) {
            throw new Exception("Seleccione el tipo de teléfono (Personal, Trabajo, Casa u Otro).");
        }
        String limpio = numero == null ? "" : numero.trim();
        validarFormato(limpio);
        String clave = soloDigitos(limpio);
        if (existentes != null) {
            for (Telefono t : existentes) {
                if (soloDigitos(t.getNumero()).equals(clave)) {
                    throw new Exception("El número " + limpio + " ya está en la lista de este cliente.");
                }
            }
        }
        return new Telefono(limpio, tipo);
    }

    /**
     * Validación de la lista completa antes de guardar: al menos un teléfono (el
     * requerimiento dice "uno o varios"), cada uno válido, y sin números repetidos.
     */
    public void validarTelefonos(List<Telefono> telefonos) throws Exception {
        if (telefonos == null || telefonos.isEmpty()) {
            throw new Exception("El cliente debe tener al menos un teléfono.");
        }
        Set<String> vistos = new HashSet<>();
        for (Telefono t : telefonos) {
            if (t.getTipo() == null) {
                throw new Exception("El teléfono " + t.getNumero() + " no tiene tipo.");
            }
            String numero = t.getNumero() == null ? "" : t.getNumero().trim();
            validarFormato(numero);
            if (!vistos.add(soloDigitos(numero))) {
                throw new Exception("El número " + numero + " está repetido.");
            }
        }
    }

    private Cliente armarCliente(int idCliente, String nombre, String apellido, String email, String nit,
            List<Telefono> telefonos) throws Exception {
        if (nombre == null || nombre.trim().isEmpty() || apellido == null || apellido.trim().isEmpty()) {
            throw new Exception("Complete al menos nombre y apellido.");
        }
        validarTelefonos(telefonos);

        List<Telefono> limpios = new ArrayList<>();
        for (Telefono t : telefonos) {
            limpios.add(new Telefono(t.getNumero().trim(), t.getTipo()));
        }
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setEmail(email);
        cliente.setNit(nit);
        cliente.setTelefonos(limpios);
        return cliente;
    }

    /**
     * Formato aceptado: dígitos, espacios y guiones, con un "+" opcional al inicio
     * (por ejemplo 5555-1234, 55551234 o +502 5555-1234), entre 7 y 15 dígitos.
     * Para cambiar la regla (más estricta o más flexible) basta editar este método.
     */
    private void validarFormato(String numero) throws Exception {
        if (numero.isEmpty()) {
            throw new Exception("Escriba el número de teléfono.");
        }
        if (numero.length() > MAX_LARGO) {
            throw new Exception("El número de teléfono es demasiado largo (máximo " + MAX_LARGO + " caracteres).");
        }
        if (!numero.matches("\\+?[0-9][0-9 \\-]*")) {
            throw new Exception("El teléfono solo puede tener dígitos, espacios y guiones (con un + opcional al inicio): "
                    + numero);
        }
        int digitos = soloDigitos(numero).length();
        if (digitos < MIN_DIGITOS || digitos > MAX_DIGITOS) {
            throw new Exception("El teléfono debe tener entre " + MIN_DIGITOS + " y " + MAX_DIGITOS
                    + " dígitos: " + numero);
        }
    }

    /** "5555-1234" y "55551234" son el mismo número: se comparan solo por sus dígitos. */
    private static String soloDigitos(String numero) {
        return numero == null ? "" : numero.replaceAll("[^0-9]", "");
    }
}
