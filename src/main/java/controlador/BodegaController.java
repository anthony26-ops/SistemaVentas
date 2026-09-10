/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.BodegaDAO;
import dao.BodegaDAOImpl;
import modelo.Bodega;
import java.util.List;

/**
 *
 * @author garci
 */
public class BodegaController {
    private BodegaDAO dao = new BodegaDAOImpl();
    
    public void guardar (Bodega b){
        dao.guardar(b);
    }
    
    public List<Bodega> listar() {
        return dao.listar();
    }
}