package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import modelo.Telefono;

/**
 * Acceso a la tabla telefonos_cliente (un cliente tiene uno o varios teléfonos).
 *
 * Las dos operaciones de escritura reciben la {@link Connection} del llamador
 * en vez de abrir una propia: así ClienteDAOImpl las ejecuta dentro de LA MISMA
 * transacción que guarda o actualiza al cliente (todo o nada). Si abrieran su
 * propia conexión, la transacción dejaría de ser una sola.
 */
public interface TelefonoDAO {

    /** Inserta una fila por teléfono, ligada al cliente indicado, usando la conexión recibida. */
    void insertarTodos(Connection cn, int idCliente, List<Telefono> telefonos) throws SQLException;

    /** Borra todos los teléfonos del cliente (se usa al modificar: se reemplaza la lista completa). */
    void eliminarPorCliente(Connection cn, int idCliente) throws SQLException;

    /** Teléfonos de un cliente (consulta con su propia conexión). */
    List<Telefono> listarPorCliente(int idCliente);

    /** Todos los teléfonos agrupados por id de cliente, en una sola consulta. */
    Map<Integer, List<Telefono>> listarTodosPorCliente();
}
