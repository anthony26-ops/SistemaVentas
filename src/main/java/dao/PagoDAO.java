package dao;

import java.util.List;
import modelo.Pago;

public interface PagoDAO {
    boolean insertar(Pago pago);
    boolean actualizar(Pago pago);
    boolean eliminar(int idPago);
    Pago obtenerPorId(int idPago);
    List<Pago> listarTodos();
    List<Pago> listarPorFactura(int idFactura);
    double totalPagadoPorFactura(int idFactura);
}
