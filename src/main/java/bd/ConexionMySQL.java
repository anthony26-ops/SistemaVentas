package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maneja la conexión a MySQL y la creación inicial de la base de datos
 * "sistema_ventas" y sus tablas.
 *
 * @author anthonyjolon
 */
public class ConexionMySQL {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3306;
    private static final String DATABASE = "sistema_ventas";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private ConexionMySQL() {
    }

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de MySQL", e);
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
        sembrarUsuarioAdmin();
    }

    /**
     * A diferencia de PostgreSQL, MySQL sí soporta
     * "CREATE DATABASE IF NOT EXISTS", así que no hace falta conectarse
     * primero a una base administrativa aparte.
     */
    private static void crearBaseDatosSiNoExiste() throws ConexionException {
        String urlServidor = "jdbc:mysql://" + HOST + ":" + PORT
                + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection conexion = DriverManager.getConnection(urlServidor, USER, PASSWORD);
                Statement stmt = conexion.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE);
        } catch (SQLException e) {
            throw new ConexionException("No se pudo verificar/crear la base " + DATABASE, e);
        }
    }

    /**
     * Crea (si no existen) todas las tablas del sistema. Cada compañero
     * agrega el CREATE TABLE de su módulo en la sección correspondiente.
     * IMPORTANTE: la tabla de Usuario va al final porque tiene una llave
     * foránea hacia empleados, así que esa tabla debe existir primero.
     *
     * NOTA DE INTEGRACIÓN (Anthony): las tablas de este método usan los
     * nombres/columnas que los DAOImpl de cada quien realmente escribieron
     * en su SQL (no siempre coinciden con los nombres "ventas_*" que
     * proponía el documento original, ver ejemplos abajo). El módulo de
     * Cliente/Empleado de Cristofer quedó como "personas"/"clientes"/
     * "empleados" (con id_cliente/id_empleado autoincrementales); el de
     * Factura/OrdenVenta de Carlos como "ordenes_venta"/"facturas" (con PK
     * "id"); el de Pago/OrdenDespacho de Oscar como "pagos"/
     * "ordenes_despacho". Las FK de este método apuntan siempre a esas
     * tablas reales. El catálogo de André (ventas_productos, ventas_
     * categorias, etc.) sí sigue la convención "ventas_*" tal como la
     * escribió él.
     */
    private static void crearTablas() throws ConexionException {
        try (Connection conexion = getConnection();
                Statement stmt = conexion.createStatement()) {

            // -- sección Cliente/Empleado -- (Cristofer)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS personas ("
                    + "id_persona INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS clientes ("
                    + "id_cliente INT AUTO_INCREMENT PRIMARY KEY, "
                    + "id_persona INT, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100), "
                    + "nit VARCHAR(20), "
                    + "CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona) REFERENCES personas(id_persona) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS empleados ("
                    + "id_empleado INT AUTO_INCREMENT PRIMARY KEY, "
                    + "id_persona INT, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "apellido VARCHAR(100) NOT NULL, "
                    + "telefono VARCHAR(20), "
                    + "email VARCHAR(100), "
                    + "puesto VARCHAR(50) NOT NULL, "
                    + "salario DECIMAL(10,2) NOT NULL, "
                    + "tipo_empleado VARCHAR(20) NOT NULL DEFAULT 'GENERAL', "
                    + "zona_venta VARCHAR(100), "
                    + "meta_ventas_mensual DECIMAL(12,2), "
                    + "CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES personas(id_persona) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // -- sección Producto/Categoria/Inventario/Proveedor/Bodega -- (André)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_categorias ("
                    + "id_categoria INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "descripcion VARCHAR(200)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_proveedores ("
                    + "id_proveedor INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nombre_empresa VARCHAR(150) NOT NULL,"
                    + "telefono VARCHAR(50),"
                    + "direccion VARCHAR(200)"
                    + ") ENGINE=InnoDB");

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
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_bodegas ("
                    + "id_bodega INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "direccion VARCHAR(200),"
                    + "telefono VARCHAR(50)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_inventario ("
                    + "id_inventario INT AUTO_INCREMENT PRIMARY KEY,"
                    + "producto_id INT NOT NULL,"
                    + "bodega_id INT NOT NULL,"
                    + "cantidad_disponible INT NOT NULL,"
                    + "CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto),"
                    + "CONSTRAINT fk_inventario_bodega FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega),"
                    + "CONSTRAINT uq_inventario_producto_bodega UNIQUE (producto_id, bodega_id)"
                    + ") ENGINE=InnoDB");

            // -- sección Factura/DetalleFactura/OrdenVenta/DetalleOrdenVenta -- (Carlos)
            // Nombres y columnas reales que usa su OrdenVentaDAOImpl/
            // FacturaDAOImpl ("ordenes_venta"/"facturas", con PK "id"), no
            // "ventas_ordenes_venta"/"ventas_facturas" como decía el
            // documento original — esas tablas no las usaba ningún DAO.
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ordenes_venta ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "fecha DATE NOT NULL,"
                    + "id_cliente INT NOT NULL,"
                    + "id_empleado INT NOT NULL,"
                    + "total DECIMAL(12,2) NOT NULL,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_ov_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),"
                    + "CONSTRAINT fk_ov_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS orden_venta_detalles ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_orden_venta INT NOT NULL,"
                    + "id_producto INT NOT NULL,"
                    + "cantidad INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_ovd_orden FOREIGN KEY (id_orden_venta) REFERENCES ordenes_venta(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_ovd_producto FOREIGN KEY (id_producto) REFERENCES ventas_productos(id_producto)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS facturas ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_orden_venta INT,"
                    + "fecha DATE NOT NULL,"
                    + "id_cliente INT NOT NULL,"
                    + "id_empleado INT NOT NULL,"
                    + "total DECIMAL(12,2) NOT NULL,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_factura_orden FOREIGN KEY (id_orden_venta) REFERENCES ordenes_venta(id),"
                    + "CONSTRAINT fk_factura_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),"
                    + "CONSTRAINT fk_factura_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS factura_detalles ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_factura INT NOT NULL,"
                    + "id_producto INT NOT NULL,"
                    + "cantidad INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_fd_factura FOREIGN KEY (id_factura) REFERENCES facturas(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_fd_producto FOREIGN KEY (id_producto) REFERENCES ventas_productos(id_producto)"
                    + ") ENGINE=InnoDB");

            // -- sección Pago/OrdenDespacho/DetalleOrdenDespacho -- (Oscar)
            // Tablas reales de su PagoDAOImpl/OrdenDespachoDAOImpl (nombres y
            // columnas distintos a los que proponía el documento original).
            // Ahora que "facturas" y "ordenes_venta" existen de verdad, se
            // agregan las FK que Oscar había dejado sin restricción.
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS pagos ("
                    + "id_pago INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_factura INT NOT NULL,"
                    + "fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "monto DECIMAL(10,2) NOT NULL CHECK (monto > 0),"
                    + "tipo_pago VARCHAR(20) NOT NULL,"
                    + "referencia VARCHAR(100),"
                    + "estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADO',"
                    + "CONSTRAINT fk_pago_factura FOREIGN KEY (id_factura) REFERENCES facturas(id)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ordenes_despacho ("
                    + "id_orden_despacho INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_orden_venta INT NOT NULL,"
                    + "bodega_id INT NULL,"
                    + "fecha_despacho TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "direccion_entrega VARCHAR(200) NOT NULL,"
                    + "transportista VARCHAR(100),"
                    + "estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',"
                    + "CONSTRAINT fk_od_orden FOREIGN KEY (id_orden_venta) REFERENCES ordenes_venta(id),"
                    + "CONSTRAINT fk_od_bodega FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS detalle_orden_despacho ("
                    + "id_detalle_despacho INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_orden_despacho INT NOT NULL,"
                    + "id_producto INT NOT NULL,"
                    + "nombre_producto VARCHAR(100) NOT NULL,"
                    + "cantidad INT NOT NULL CHECK (cantidad > 0),"
                    + "CONSTRAINT fk_detalle_despacho_orden FOREIGN KEY (id_orden_despacho) REFERENCES ordenes_despacho(id_orden_despacho) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // -- sección Usuario -- (Anthony)
            // NOTA: la FK apunta a "empleados(id_empleado)", que es la tabla
            // que Cristofer realmente creó (ver nota de integración arriba),
            // no a "ventas_empleados(id)" como usan Carlos/Oscar.
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_usuarios ("
                    + "id_usuario INT NOT NULL PRIMARY KEY,"
                    + "empleado_id INT NOT NULL UNIQUE,"
                    + "nombre_usuario VARCHAR(50) NOT NULL UNIQUE,"
                    + "contrasena_hash VARCHAR(255) NOT NULL,"
                    + "rol VARCHAR(30) NOT NULL,"
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE,"
                    + "CONSTRAINT fk_usuario_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id_empleado)"
                    + ") ENGINE=InnoDB");

            migrarColumnasAutoIncrement(stmt);
            migrarInventarioPorBodega(stmt);
        } catch (SQLException e) {
            throw new ConexionException("No se pudieron crear las tablas del sistema", e);
        }
    }

    /**
     * Las instalaciones que ya tenían "ventas_categorias"/"ventas_proveedores"
     * creadas antes de este cambio quedaron con id_categoria/id_proveedor
     * como PRIMARY KEY normal (no autoincremental), porque CREATE TABLE IF
     * NOT EXISTS no vuelve a ejecutar el CREATE si la tabla ya existe. Este
     * método es idempotente (se puede correr en cada arranque sin problema)
     * y pone AUTO_INCREMENT en ambas columnas para que ya no haga falta
     * inventar el id a mano al agregar una categoría o un proveedor.
     *
     * MySQL no deja modificar una columna que ya es referenciada por una
     * FK ajena (error 1833: "used in a foreign key constraint"), así que
     * hay que quitar temporalmente las FK de ventas_productos que apuntan
     * a estas columnas, hacer el ALTER, y volver a crearlas.
     */
    private static void migrarColumnasAutoIncrement(Statement stmt) {
        try {
            stmt.executeUpdate("ALTER TABLE ventas_productos DROP FOREIGN KEY fk_producto_categoria");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_productos DROP FOREIGN KEY fk_producto_proveedor");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_categorias MODIFY id_categoria INT NOT NULL AUTO_INCREMENT");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_proveedores MODIFY id_proveedor INT NOT NULL AUTO_INCREMENT");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_productos ADD CONSTRAINT fk_producto_categoria "
                    + "FOREIGN KEY (categoria_id) REFERENCES ventas_categorias(id_categoria)");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_productos ADD CONSTRAINT fk_producto_proveedor "
                    + "FOREIGN KEY (proveedor_id) REFERENCES ventas_proveedores(id_proveedor)");
        } catch (SQLException ignored) {
        }
    }

    /**
     * Mismo problema que migrarColumnasAutoIncrement, pero para las
     * instalaciones que ya tenían "ventas_inventario" y "ordenes_despacho"
     * creadas antes de que el inventario se llevara por bodega: agrega la
     * llave autoincremental y la restricción UNIQUE que faltan en
     * ventas_inventario, y la columna bodega_id (con su FK) en
     * ordenes_despacho. Es idempotente: cada ALTER va en su propio
     * try/catch, así que si ya se aplicó antes simplemente falla
     * silenciosamente y sigue con el siguiente.
     */
    private static void migrarInventarioPorBodega(Statement stmt) {
        try {
            stmt.executeUpdate("ALTER TABLE ventas_inventario MODIFY id_inventario INT NOT NULL AUTO_INCREMENT");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ventas_inventario ADD CONSTRAINT uq_inventario_producto_bodega "
                    + "UNIQUE (producto_id, bodega_id)");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ordenes_despacho ADD COLUMN bodega_id INT NULL");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("ALTER TABLE ordenes_despacho ADD CONSTRAINT fk_od_bodega "
                    + "FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega)");
        } catch (SQLException ignored) {
        }

        // Migración de datos (una sola vez): si no existe todavía ninguna
        // bodega, se crea una por defecto; y a cualquier producto que tenga
        // existencia pero ningún renglón en ventas_inventario (típicamente
        // los productos que ya existían antes de este cambio) se le acredita
        // esa existencia en la primera bodega disponible, para no perder el
        // stock que ya se había cargado.
        try (ResultSet rsBodegas = stmt.executeQuery("SELECT id_bodega FROM ventas_bodegas ORDER BY id_bodega LIMIT 1")) {
            int idBodegaDefault;
            if (rsBodegas.next()) {
                idBodegaDefault = rsBodegas.getInt("id_bodega");
            } else {
                stmt.executeUpdate("INSERT INTO ventas_bodegas (nombre, direccion, telefono) "
                        + "VALUES ('Bodega Principal', 'Sin dirección registrada', '')");
                try (ResultSet rsNueva = stmt.executeQuery("SELECT LAST_INSERT_ID() AS id")) {
                    rsNueva.next();
                    idBodegaDefault = rsNueva.getInt("id");
                }
            }

            String sqlMigrar = "INSERT INTO ventas_inventario (producto_id, bodega_id, cantidad_disponible) "
                    + "SELECT p.id_producto, " + idBodegaDefault + ", p.existencia "
                    + "FROM ventas_productos p "
                    + "WHERE p.existencia > 0 "
                    + "AND NOT EXISTS (SELECT 1 FROM ventas_inventario i WHERE i.producto_id = p.id_producto)";
            stmt.executeUpdate(sqlMigrar);
        } catch (SQLException ignored) {
        }
    }

    /**
     * Crea un usuario administrador de prueba (usuario: admin,
     * contraseña: admin123) la primera vez que se corre el programa, para
     * poder probar el login mientras el resto de módulos no tiene todavía
     * una pantalla de "crear usuario". No hace nada si ya existe.
     */
    private static void sembrarUsuarioAdmin() throws ConexionException {
        try (Connection conexion = getConnection();
                Statement stmt = conexion.createStatement()) {

            stmt.executeUpdate(
                    "INSERT IGNORE INTO personas (id_persona, nombre, apellido, telefono, email) "
                    + "VALUES (1, 'Admin', 'Sistema', '00000000', 'admin@sistemaventas.com')");

            stmt.executeUpdate(
                    "INSERT IGNORE INTO empleados (id_empleado, id_persona, nombre, apellido, telefono, email, puesto, salario) "
                    + "VALUES (1, 1, 'Admin', 'Sistema', '00000000', 'admin@sistemaventas.com', 'Administrador', 0)");

            String contrasenaHash = org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt());
            try (java.sql.PreparedStatement ps = conexion.prepareStatement(
                    "INSERT IGNORE INTO ventas_usuarios (id_usuario, empleado_id, nombre_usuario, contrasena_hash, rol, activo) "
                    + "VALUES (1, 1, 'admin', ?, 'ADMIN', TRUE)")) {
                ps.setString(1, contrasenaHash);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ConexionException("No se pudo crear el usuario admin de prueba", e);
        }
    }
}
