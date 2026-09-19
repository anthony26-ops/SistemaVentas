package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.OrdenVentaDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.List;
import modelo.Cliente;
import modelo.Empleado;
import modelo.OrdenVenta;

public class OrdenVentaController {
    
    private OrdenVentaDAO ordenDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    
    public OrdenVentaController() {
        this.ordenDAO = new OrdenVentaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
    }
    
    public List<Cliente> obtenerClientes() {
        return clienteDAO.listarTodos();
    }
    
    public List<Empleado> obtenerEmpleados() {
        return empleadoDAO.listarTodos();
    }
    
    /**
     * PUNTO 7: Método puro de negocio, independiente de Swing.
     * Ideal para integrarse con apps móviles o web.
     */
    public boolean crearPedido(int idCliente, int idEmpleado, double total, String estado) throws Exception {
        if (total <= 0) {
            throw new Exception("El total de la orden debe ser mayor a cero.");
        }
        
        OrdenVenta orden = new OrdenVenta();
        orden.setFecha(new java.sql.Date(System.currentTimeMillis()));
        
        Cliente cliente = new Cliente();
        cliente.setId(idCliente); // Asumiendo que obtienes el ID de la vista
        orden.setCliente(cliente);
        
        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        orden.setEmpleado(empleado);
        
        orden.setTotal(total);
        orden.setEstado(estado); // Cumpliendo el Punto 1
        
        return ordenDAO.registrarOrden(orden);
    }
    
    /**
     * PUNTO 6: Precio dinámico.
     * Simula la conexión al inventario mientras se integra el código de André.
     */
    public double obtenerPrecioProducto(String nombreProducto) {
        // TODO: Reemplazar esto por ProductoDAO cuando André suba sus archivos.
        String productoLower = nombreProducto.toLowerCase();
        
        if (productoLower.contains("laptop")) return 7500.00;
        if (productoLower.contains("impresora")) return 1200.00;
        if (productoLower.contains("mouse")) return 250.00;
        
        return 150.00; // Precio por defecto
    }
}