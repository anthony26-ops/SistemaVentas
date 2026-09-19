package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.FacturaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.FacturaDAOImpl;
import java.util.List;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Factura;
import modelo.Producto;

public class FacturaController {

    private FacturaDAO facturaDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ProductoDAO productoDAO;

    public FacturaController() {
        this.facturaDAO = new FacturaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }

    public List<Cliente> obtenerClientes() {
        return clienteDAO.listarTodos();
    }

    public List<Empleado> obtenerEmpleados() {
        return empleadoDAO.listarTodos();
    }

    public double obtenerPrecioProducto(String nombreProducto) {
        for (Producto p : productoDAO.listar()) {
            if (p.getNombre() != null && p.getNombre().equalsIgnoreCase(nombreProducto)) {
                return p.getPrecio();
            }
        }
        return 0.0;
    }

    public List<Producto> obtenerProductos() {
        return productoDAO.listar();
    }

    /**
     * Método puro de negocio, independiente de Swing.
     */
    public boolean crearFactura(int idCliente, int idEmpleado, double total, String estado) throws Exception {
        if (total <= 0) {
            throw new Exception("El total de la factura debe ser mayor a cero.");
        }
        
        Factura factura = new Factura();
        factura.setFecha(new java.sql.Date(System.currentTimeMillis()));
        
        Cliente cliente = new Cliente();
        cliente.setId(idCliente); 
        factura.setCliente(cliente);
        
        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        factura.setEmpleado(empleado);
        
        factura.setTotal(total);
        factura.setEstado(estado); 
        
        return facturaDAO.registrarFactura(factura);
    }
}