package controlador;

import dao.FacturaDAO;
import dao.impl.FacturaDAOImpl;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Factura;

public class FacturaController {
    
    private FacturaDAO facturaDAO;
    
    public FacturaController() {
        this.facturaDAO = new FacturaDAOImpl();
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