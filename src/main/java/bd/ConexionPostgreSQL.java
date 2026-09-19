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
    private static final String PASSWORD = "postgres";
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
            // Tabla de personas (tabla base para herencia o datos comunes)
stmt.executeUpdate(
    "CREATE TABLE IF NOT EXISTS personas (" +
    "id_persona SERIAL PRIMARY KEY, " +
    "nombre VARCHAR(100) NOT NULL, " +
    "apellido VARCHAR(100) NOT NULL, " +
    "telefono VARCHAR(20), " +
    "email VARCHAR(100)" +
    ");"
);

// Tabla de clientes
stmt.executeUpdate(
    "CREATE TABLE IF NOT EXISTS clientes (" +
    "id_cliente SERIAL PRIMARY KEY, " +
    "id_persona INT REFERENCES personas(id_persona) ON DELETE CASCADE, " +
    "nombre VARCHAR(100) NOT NULL, " +
    "apellido VARCHAR(100) NOT NULL, " +
    "telefono VARCHAR(20), " +
    "email VARCHAR(100), " +
    "nit VARCHAR(20)" +
    ");"
);

// Tabla de empleados
stmt.executeUpdate(
    "CREATE TABLE IF NOT EXISTS empleados (" +
    "id_empleado SERIAL PRIMARY KEY, " +
    "id_persona INT REFERENCES personas(id_persona) ON DELETE CASCADE, " +
    "nombre VARCHAR(100) NOT NULL, " +
    "apellido VARCHAR(100) NOT NULL, " +
    "telefono VARCHAR(20), " +
    "email VARCHAR(100), " +
    "puesto VARCHAR(50) NOT NULL, " +
    "salario NUMERIC(10,2) NOT NULL" +
    ");"
);
            // -- sección Producto/Categoria/Inventario/Proveedor -- (André)
            // -- sección Factura/DetalleFactura -- (Carlos)
            // -- sección Pago / Orden de Despacho -- (Oscar)
            // Tabla de pagos aplicados a una factura
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pagos (" +
                "id_pago SERIAL PRIMARY KEY, " +
                "id_factura INT NOT NULL, " +
                "fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "monto NUMERIC(10,2) NOT NULL CHECK (monto > 0), " +
                "tipo_pago VARCHAR(20) NOT NULL, " +
                "referencia VARCHAR(100), " +
                "estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADO'" +
                ");"
            );

            // Tabla de ordenes de despacho
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ordenes_despacho (" +
                "id_orden_despacho SERIAL PRIMARY KEY, " +
                "id_orden_venta INT NOT NULL, " +
                "fecha_despacho TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "direccion_entrega VARCHAR(200) NOT NULL, " +
                "transportista VARCHAR(100), " +
                "estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'" +
                ");"
            );

            // Tabla de detalle de la orden de despacho
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS detalle_orden_despacho (" +
                "id_detalle_despacho SERIAL PRIMARY KEY, " +
                "id_orden_despacho INT NOT NULL REFERENCES ordenes_despacho(id_orden_despacho) ON DELETE CASCADE, " +
                "id_producto INT NOT NULL, " +
                "nombre_producto VARCHAR(100) NOT NULL, " +
                "cantidad INT NOT NULL CHECK (cantidad > 0)" +
                ");"
            );
            // Nota: id_factura e id_orden_venta quedan como INT simple (sin FOREIGN KEY)
            // mientras el modulo de Facturacion / Orden de Venta no cree sus tablas.


        } catch (SQLException e) {
            throw new ConexionException("No se pudieron crear las tablas del sistema", e);
        }
    }
}
