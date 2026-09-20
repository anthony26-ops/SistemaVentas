/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;
import modelo.Producto;
/**
 *
 * @author garci
 */
public interface ProductoDAO {
    void guardar(Producto producto);
    List<Producto> listar();
    Producto buscarPorId(int id);
    void actualizarExistencia(int idProducto, int nuevaExistencia);
}