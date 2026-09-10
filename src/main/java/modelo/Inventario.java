/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author garci
 */
public class Inventario {
    private int idInventario;
    private Producto producto;
    private int cantidadDisponible;
    private Bodega bodega;
    
    public Inventario(){}
    
    public Inventario(int idInventario, Producto producto, int cantidadDisponible, Bodega bodega) {
        this.idInventario = idInventario;
        this.producto = producto;
        this.cantidadDisponible = cantidadDisponible;
        this.bodega = bodega;
    }

    public int getIdInventario() {
        return idInventario;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public Bodega getBodega() {
        return bodega;
    }

    public void setIdInventario(int idInventario) {
        this.idInventario = idInventario;
    }

    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public void setBodega(Bodega bodega) {
        this.bodega = bodega;
    }

    public void actualizarStock(int cantidad) {
        cantidadDisponible += cantidad;
    }            
}
