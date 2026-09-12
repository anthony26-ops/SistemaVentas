/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;
import modelo.OrdenVenta;

public interface OrdenVentaDAO {
    boolean registrarOrden(OrdenVenta orden);
    List<OrdenVenta> listarOrdenes();
    OrdenVenta obtenerOrdenPorId(int idOrden);
}
