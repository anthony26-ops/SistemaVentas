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
    Factura obtenerFacturaPorOrden(int idOrdenVenta);

    /**
     * Transaccional: toma los detalles de una OrdenVenta que YA fue
     * despachada, genera su Factura y marca la orden como "Facturada".
     * No descuenta inventario (eso ya ocurrió al confirmar el despacho).
     * Devuelve null si la orden no existe, no tiene detalle, o todavía no
     * está en estado "Despachada".
     */
    Factura generarFacturaDesdeOrden(OrdenVenta orden);
}