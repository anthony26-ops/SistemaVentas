/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;
import java.util.List;
import modelo.Bodega;
/**
 *
 * @author garci
 */
public interface BodegaDAO {
    void guardar(Bodega bodega);
    List<Bodega> listar();
}