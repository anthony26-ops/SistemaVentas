/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author garci
 */
public class Bodega {
    private int idBodega;
    private String nombre;
    private String direccion;
    private String telefono;
    
    public Bodega(){
        
    }
    public Bodega(int idBodega, String nombre, String direccion, String telefono) {
        this.idBodega = idBodega;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public int getIdBodega() {
        return idBodega;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    
    public void setIdBodega(int idBodega) {
        this.idBodega = idBodega;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    @Override
    public String toString(){
        return nombre;
    }   
}
