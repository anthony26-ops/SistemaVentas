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
     * IMPORTANTE: la tabla de Usuario va al final porque tiene una llave
     * foránea hacia ventas_empleados, así que esa tabla debe existir primero.
     *
     * NOTA DE INTEGRACIÓN (Anthony): las tablas de este método usan la
     * convención "ventas_*" con columna "id" que define el documento base
     * (ventas_clientes, ventas_empleados, etc.), porque es la que ya usan
     * los módulos de André, Carlos y Oscar. El módulo de Cliente/Empleado
     * de Cristofer, en cambio, quedó implementado con sus propias tablas
     * "personas"/"clientes"/"empleados" (nombres y llaves distintas). Las
     * dejo tal cual las escribió él, pero OJO: las llaves foráneas de
     * OrdenVenta, Factura, OrdenDespacho y Usuario apuntan a
     * ventas_clientes/ventas_empleados, que con el esquema actual de
     * Cristofer NO EXISTEN. Hay que resolverlo en equipo (ver resumen).
     */
    private static void crearTablas() throws ConexionException {
        try (Connection conexion = getConnection();
             Statement stmt = conexion.createStatement()) {

            // -- sección Cliente/Empleado -- (Cristofer)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS personas ("
                    + "id_persona SERIAL PRIMARY KEY, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS clientes ("
                    + "id_cliente SERIAL PRIMARY KEY, "
                    + "id_persona INT REFERENCES personas(id_persona) ON DELETE CASCADE, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100), "
                    + "nit VARCHAR(20)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS empleados ("
                    + "id_empleado SERIAL PRIMARY KEY, "
                    + "id_persona INT REFERENCES personas(id_persona) ON DELETE CASCADE, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100), "
                    + "puesto VARCHAR(50) NOT NULL, "
                    + "salario NUMERIC(10,2) NOT NULL"
                    + ")");

            // -- sección Producto/Categoria/Inventario/Proveedor/Bodega -- (André)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_categorias ("
                    + "id_categoria INT NOT NULL PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "descripcion VARCHAR(200)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_proveedores ("
                    + "id_proveedor INT NOT NULL PRIMARY KEY,"
                    + "nombre_empresa VARCHAR(150) NOT NULL,"
                    + "telefono VARCHAR(50),"
                    + "direccion VARCHAR(200)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_productos ("
                    + "id_producto INT NOT NULL PRIMARY KEY,"
                    + "nombre VARCHAR(120) NOT NULL,"
                    + "descripcion VARCHAR(200),"
                    + "precio DECIMAL(12,2) NOT NULL,"
                    + "existencia INT NOT NULL,"
                    + "categoria_id INT,"
                    + "proveedor_id INT,"
                    + "CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES ventas_categorias(id_categoria),"
                    + "CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES ventas_proveedores(id_proveedor)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_bodegas ("
                    + "id_bodega INT NOT NULL PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "direccion VARCHAR(200),"
                    + "telefono VARCHAR(50)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_inventario ("
                    + "id_inventario INT NOT NULL PRIMARY KEY,"
                    + "producto_id INT NOT NULL,"
                    + "bodega_id INT NOT NULL,"
                    + "cantidad_disponible INT NOT NULL,"
                    + "CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto),"
                    + "CONSTRAINT fk_inventario_bodega FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega)"
                    + ")");

            // -- sección Factura/DetalleFactura/OrdenVenta/DetalleOrdenVenta -- (Carlos)
            // OJO: ventas_clientes / ventas_empleados no existen todavía con el
            // esquema real de Cristofer (ver nota de integración arriba).
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_ordenes_venta ("
                    + "id_orden_venta INT NOT NULL PRIMARY KEY,"
                    + "fecha DATE NOT NULL,"
                    + "cliente_id INT NOT NULL,"
                    + "vendedor_id INT NOT NULL,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_ov_cliente FOREIGN KEY (cliente_id) REFERENCES ventas_clientes(id),"
                    + "CONSTRAINT fk_ov_vendedor FOREIGN KEY (vendedor_id) REFERENCES ventas_empleados(id)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_orden_venta_detalles ("
                    + "id_detalle BIGSERIAL PRIMARY KEY,"
                    + "orden_venta_id INT NOT NULL,"
                    + "producto_id INT NOT NULL,"
                    + "cantidad_solicitada INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_ovd_orden FOREIGN KEY (orden_venta_id) REFERENCES ventas_ordenes_venta(id_orden_venta) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_ovd_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_facturas ("
                    + "id_factura INT NOT NULL PRIMARY KEY,"
                    + "orden_venta_id INT,"
                    + "fecha DATE NOT NULL,"
                    + "cliente_id INT NOT NULL,"
                    + "empleado_id INT NOT NULL,"
                    + "subtotal DECIMAL(12,2) NOT NULL,"
                    + "impuesto DECIMAL(12,2) NOT NULL,"
                    + "total DECIMAL(12,2) NOT NULL,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_factura_orden FOREIGN KEY (orden_venta_id) REFERENCES ventas_ordenes_venta(id_orden_venta),"
                    + "CONSTRAINT fk_factura_cliente FOREIGN KEY (cliente_id) REFERENCES ventas_clientes(id),"
                    + "CONSTRAINT fk_factura_empleado FOREIGN KEY (empleado_id) REFERENCES ventas_empleados(id)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_factura_detalles ("
                    + "id_detalle BIGSERIAL PRIMARY KEY,"
                    + "factura_id INT NOT NULL,"
                    + "producto_id INT NOT NULL,"
                    + "cantidad INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_detalle_factura FOREIGN KEY (factura_id) REFERENCES ventas_facturas(id_factura) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto)"
                    + ")");

            // -- sección Pago/OrdenDespacho/DetalleOrdenDespacho -- (Oscar)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_ordenes_despacho ("
                    + "id_orden_despacho INT NOT NULL PRIMARY KEY,"
                    + "orden_venta_id INT NOT NULL,"
                    + "bodega_id INT NOT NULL,"
                    + "empleado_id INT NOT NULL,"
                    + "fecha_despacho DATE,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_od_orden FOREIGN KEY (orden_venta_id) REFERENCES ventas_ordenes_venta(id_orden_venta),"
                    + "CONSTRAINT fk_od_bodega FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega),"
                    + "CONSTRAINT fk_od_empleado FOREIGN KEY (empleado_id) REFERENCES ventas_empleados(id)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_orden_despacho_detalles ("
                    + "id_detalle BIGSERIAL PRIMARY KEY,"
                    + "orden_despacho_id INT NOT NULL,"
                    + "producto_id INT NOT NULL,"
                    + "cantidad_despachada INT NOT NULL,"
                    + "CONSTRAINT fk_odd_orden FOREIGN KEY (orden_despacho_id) REFERENCES ventas_ordenes_despacho(id_orden_despacho) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_odd_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto)"
                    + ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_pagos ("
                    + "id_pago INT NOT NULL PRIMARY KEY,"
                    + "factura_id INT NOT NULL,"
                    + "monto DECIMAL(12,2) NOT NULL,"
                    + "fecha_pago DATE NOT NULL,"
                    + "tipo_metodo VARCHAR(20) NOT NULL,"
                    + "detalle_metodo VARCHAR(150),"
                    + "CONSTRAINT fk_pago_factura FOREIGN KEY (factura_id) REFERENCES ventas_facturas(id_factura)"
                    + ")");

            // -- sección Usuario -- (Anthony)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_usuarios ("
                    + "id_usuario INT NOT NULL PRIMARY KEY,"
                    + "empleado_id INT NOT NULL UNIQUE,"
                    + "nombre_usuario VARCHAR(50) NOT NULL UNIQUE,"
                    + "contrasena_hash VARCHAR(255) NOT NULL,"
                    + "rol VARCHAR(30) NOT NULL,"
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE,"
                    + "CONSTRAINT fk_usuario_empleado FOREIGN KEY (empleado_id) REFERENCES ventas_empleados(id)"
                    + ")");

        } catch (SQLException e) {
            throw new ConexionException("No se pudieron crear las tablas del sistema", e);
        }
    }
}
