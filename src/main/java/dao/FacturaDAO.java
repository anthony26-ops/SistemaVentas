/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;
import modelo.Factura;
import modelo.OrdenVenta;

public interface FacturaDAO {
    boolean registrarFactura(Factura factura);
    List<Factura> listarFacturas();
    Factura obtenerFacturaPorId(int idFactura);

    /**
     * Transaccional: toma los detalles de una OrdenVenta ya registrada,
     * genera su Factura, descuenta la existencia de cada producto vía
     * ProductoDAO.actualizarExistencia(...) y marca la orden como
     * "Facturada". Devuelve null si la orden no existe o no tiene detalle.
     */
    Factura generarFacturaDesdeOrden(OrdenVenta orden);
}