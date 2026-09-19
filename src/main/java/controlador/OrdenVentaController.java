package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.OrdenVentaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.List;
import modelo.Cliente;
import modelo.DetalleOrdenVenta;
import modelo.Empleado;
import modelo.OrdenVenta;
import modelo.Producto;

public class OrdenVentaController {

    private OrdenVentaDAO ordenDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ProductoDAO productoDAO;

    public OrdenVentaController() {
        this.ordenDAO = new OrdenVentaDAOImpl();
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
     * Igual que crearPedido, pero guardando también el detalle de productos
     * (List&lt;DetalleOrdenVenta&gt;) para que la factura y el despacho puedan
     * generarse a partir de la orden real.
     */
    public boolean crearPedido(int idCliente, int idEmpleado, List<DetalleOrdenVenta> detalles, String estado) throws Exception {
        if (detalles == null || detalles.isEmpty()) {
            throw new Exception("El pedido debe tener al menos un producto.");
        }

        double total = 0;
        for (DetalleOrdenVenta d : detalles) {
            total += d.getSubtotal();
        }

        OrdenVenta orden = new OrdenVenta();
        orden.setFecha(new java.sql.Date(System.currentTimeMillis()));

        Cliente cliente = new Cliente();
        cliente.setId(idCliente);
        orden.setCliente(cliente);

        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        orden.setEmpleado(empleado);

        orden.setTotal(total);
        orden.setEstado(estado);
        orden.setDetalles(detalles);

        return ordenDAO.registrarOrden(orden);
    }

    public List<OrdenVenta> listarPendientes() {
        return ordenDAO.listarPendientes();
    }

    /**
     * PUNTO 6: Precio dinámico, tomado del catálogo real de Producto (André).
     */
    public double obtenerPrecioProducto(String nombreProducto) {
        for (Producto p : productoDAO.listar()) {
            if (p.getNombre() != null && p.getNombre().equalsIgnoreCase(nombreProducto)) {
                return p.getPrecio();
            }
        }
        return 0.0;
    }

    /**
     * Productos disponibles para el combo de la vista (André).
     */
    public List<Producto> obtenerProductos() {
        return productoDAO.listar();
    }
}