/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ProductoDAO;
import dao.ProductoDAOImpl;
import modelo.Producto;
import modelo.Categoria;
import modelo.Proveedor;
import java.util.List;

/**
 *
 * @author garci
 */
public class ProductoController {
    private ProductoDAO dao = new ProductoDAOImpl();

    public void guardar(Producto p) {
        dao.guardar(p);
    }

    public List<Producto> listar() {
        return dao.listar();
    }

    public Producto buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    public void actualizarExistencia(int idProducto, int nuevaExistencia) {
        dao.actualizarExistencia(idProducto, nuevaExistencia);
    }

    public List<Categoria> listarCategorias() {
        return ((ProductoDAOImpl) dao).listarCategorias();
    }

    public List<Proveedor> listarProveedores() {
        return ((ProductoDAOImpl) dao).listarProveedores();
    }
}