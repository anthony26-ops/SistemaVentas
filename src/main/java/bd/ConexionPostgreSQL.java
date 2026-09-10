package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maneja la conexión a PostgreSQL y la creación inicial de la base de datos
 * "sistema_ventas" y sus tablas.
 *
 * @author anthonyjolon
 */
public class ConexionPostgreSQL {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5432;
    private static final String DATABASE = "sistema_ventas";
    private static final String USER = "postgres";
    private static final String PASSWORD = "posgres";
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;
    private static final String DRIVER = "org.postgresql.Driver";

    private ConexionPostgreSQL() {
    }

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de PostgreSQL", e);
        }
    }

    /**
     * Abre una conexión hacia la base de datos "sistema_ventas".
     */
    public static Connection getConnection() throws ConexionException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new ConexionException("No se pudo conectar a " + DATABASE, e);
        }
    }

    /**
     * Verifica que exista la base "sistema_ventas" (creándola si hace falta)
     * y luego crea las tablas necesarias. Debe llamarse una sola vez al
     * iniciar la aplicación.
     */
    public static void inicializar() throws ConexionException {
        crearBaseDatosSiNoExiste();
        crearTablas();
    }

    /**
     * PostgreSQL no soporta "CREATE DATABASE IF NOT EXISTS", así que hay que
     * verificar primero si ya existe consultando el catálogo, conectándose
     * a la base administrativa "postgres" (que siempre existe).
     */
    private static void crearBaseDatosSiNoExiste() throws ConexionException {
        String urlPostgres = "jdbc:postgresql://" + HOST + ":" + PORT + "/postgres";

        try (Connection conexion = DriverManager.getConnection(urlPostgres, USER, PASSWORD);
             Statement stmt = conexion.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + DATABASE + "'")) {
                if (!rs.next()) {
                    stmt.executeUpdate("CREATE DATABASE " + DATABASE);
                }
            }
        } catch (SQLException e) {
            throw new ConexionException("No se pudo verificar/crear la base " + DATABASE, e);
        }
    }

    /**
     * Crea (si no existen) todas las tablas del sistema. Cada compañero
     * agrega el CREATE TABLE de su módulo en la sección correspondiente.
     */
    private static void crearTablas() throws ConexionException {
        try (Connection conexion = getConnection();
             Statement stmt = conexion.createStatement()) {

            // -- sección Cliente/Empleado -- (Cristofer)
            // -- sección Producto/Categoria/Inventario/Proveedor -- (André)
            // -- sección Factura/DetalleFactura -- (Carlos)
            // -- sección Pago -- (Oscar)

        } catch (SQLException e) {
            throw new ConexionException("No se pudieron crear las tablas del sistema", e);
        }
    }
}
