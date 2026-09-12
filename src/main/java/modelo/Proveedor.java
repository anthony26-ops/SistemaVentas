/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author garci
 */
public class Proveedor {
    private int idProveedor;
    private String nombreEmpresa;
    private String telefono;
    private String direccion;
    
    public Proveedor (){
    }
    
    public Proveedor(int idProveedor, String nombreEmpresa, String telefono, String direccion) {
        this.idProveedor = idProveedor;
        this.nombreEmpresa = nombreEmpresa;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    
    public int getIdProveedor() {
        return idProveedor;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    @Override
    public String toString(){
        return nombreEmpresa;
    }
}
