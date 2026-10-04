package controlador;

import dao.FacturaDAO;
import dao.OrdenVentaDAO;
import dao.impl.FacturaDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.util.List;
import modelo.Factura;
import modelo.OrdenVenta;

/**
 * Controlador de Facturación. Deliberadamente NO expone ninguna forma de
 * crear una factura a mano (cliente/empleado/productos escritos en pantalla):
 * la única vía es {@link #generarFacturaDesdeOrden(int)}, que exige una
 * Orden de Venta ya despachada. Antes existía un crearFactura(...) que
 * armaba una factura "directa" sin orden ni despacho detrás (usado por una
 * pestaña "Nuevo Registro" en FrmFacturacion) y que descontaba inventario
 * por su cuenta; se eliminó porque permitía saltarse el flujo obligatorio
 * del proyecto (Cliente -&gt; Orden de Venta -&gt; Despacho -&gt; Salida de
 * Inventario -&gt; Facturación).
 */
public class FacturaController {

    private FacturaDAO facturaDAO;
    private OrdenVentaDAO ordenVentaDAO;

    public FacturaController() {
        this.facturaDAO = new FacturaDAOImpl();
        this.ordenVentaDAO = new OrdenVentaDAOImpl();
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