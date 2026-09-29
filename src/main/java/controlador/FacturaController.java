package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.FacturaDAO;
import dao.OrdenVentaDAO;
import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.FacturaDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.List;
import modelo.Cliente;
import modelo.DetalleFactura;
import modelo.Empleado;
import modelo.Factura;
import modelo.OrdenVenta;
import modelo.Producto;

public class FacturaController {

    private FacturaDAO facturaDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ProductoDAO productoDAO;
    private OrdenVentaDAO ordenVentaDAO;

    public FacturaController() {
        this.facturaDAO = new FacturaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.ordenVentaDAO = new OrdenVentaDAOImpl();
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

    /**
     * Igual que crearFactura, pero guardando también el detalle de
     * productos (List&lt;DetalleFactura&gt;): inserta la factura, sus
     * líneas, y descuenta la existencia de cada producto.
     */
    public boolean crearFactura(int idCliente, int idEmpleado, List<DetalleFactura> detalles, String estado) throws Exception {
        return crearFactura(idCliente, idEmpleado, detalles, estado, new java.util.Date());
    }

    /**
     * Igual que crearFactura, pero recibiendo la fecha elegida en el
     * selector de calendario de la pantalla en vez de asumir siempre
     * "ahora mismo".
     */
    public boolean crearFactura(int idCliente, int idEmpleado, List<DetalleFactura> detalles, String estado,
            java.util.Date fecha) throws Exception {
        if (detalles == null || detalles.isEmpty()) {
            throw new Exception("La factura debe tener al menos un producto.");
        }

        double total = 0;
        for (DetalleFactura d : detalles) {
            total += d.getSubtotal();
        }

        Factura factura = new Factura();
        factura.setFecha(new java.sql.Date((fecha != null ? fecha : new java.util.Date()).getTime()));

        Cliente cliente = new Cliente();
        cliente.setId(idCliente);
        factura.setCliente(cliente);

        Empleado empleado = new Empleado();
        empleado.setId(idEmpleado);
        factura.setEmpleado(empleado);

        factura.setDetalles(detalles);
        factura.setTotal(total);
        factura.setEstado(estado);

        return facturaDAO.registrarFactura(factura);
    }

    /**
     * Órdenes de venta listas para facturar: ya fueron despachadas (y por lo
     * tanto ya se les descontó el inventario). Este es el paso 7 del flujo
     * obligatorio: Cliente -&gt; OrdenVenta -&gt; Despacho -&gt; Salida de
     * Inventario -&gt; Facturación.
     */
    public List<OrdenVenta> listarOrdenesListasParaFacturar() {
        return ordenVentaDAO.listarDespachadas();
    }

    public OrdenVenta obtenerOrden(int idOrdenVenta) {
        return ordenVentaDAO.obtenerOrdenPorId(idOrdenVenta);
    }

    /**
     * Genera la factura a partir de una OrdenVenta ya despachada: copia su
     * detalle y marca la orden como "Facturada". No vuelve a descontar
     * inventario (ya se descontó al confirmar el despacho). Devuelve null
     * si la orden todavía no está despachada.
     */
    public Factura generarFacturaDesdeOrden(int idOrdenVenta) {
        OrdenVenta orden = new OrdenVenta();
        orden.setIdOrdenVenta(idOrdenVenta);
        return facturaDAO.generarFacturaDesdeOrden(orden);
    }

    public Factura obtenerFacturaPorOrden(int idOrdenVenta) {
        return facturaDAO.obtenerFacturaPorOrden(idOrdenVenta);
    }

    public Factura obtenerFacturaPorId(int idFactura) {
        return facturaDAO.obtenerFacturaPorId(idFactura);
    }

    public List<Factura> listarFacturas() {
        return facturaDAO.listarFacturas();
    }
}