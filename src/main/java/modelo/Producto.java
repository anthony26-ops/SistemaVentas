/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author garci
 */
public class Producto{
    private int idProducto;
    private String nombre;
    private String descripcion;
    private double precio;
    private int existencia;
    private Categoria categoria;
    private Proveedor proveedor;
    
    public Producto(){
    }
    
    public Producto(int idProducto, String nombre, String descripcion, double Precio, int existencia, Categoria categoria, Proveedor proveedor) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = Precio;
        this.existencia = existencia;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }    
    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double Precio) {
        this.precio = Precio;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public boolean hayExistencia(int cantidad) {
        return existencia >= cantidad ;
    }
    
    @Override
    public String toString() {
        return nombre + " - Q" + precio;
    }    
    
    
}
