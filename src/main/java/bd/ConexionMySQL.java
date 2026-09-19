package bd;

import java.sql.Connection;
import java.sql.DriverManager;
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
     * NOTA DE INTEGRACIÓN (Anthony): las tablas de Producto/Bodega (André),
     * Factura/OrdenVenta (Carlos), Pago/OrdenDespacho (Oscar) y Usuario
     * (Anthony) usan la convención "ventas_*" con columna "id" que define
     * el documento base. El módulo de Cliente/Empleado de Cristofer, en
     * cambio, quedó implementado con sus propias tablas
     * "personas"/"clientes"/"empleados" (nombres y llaves distintas, con
     * id_cliente/id_empleado autoincrementales). El documento original
     * asumía que las FK de OrdenVenta/Factura/OrdenDespacho/Usuario
     * apuntarían a "ventas_clientes(id)"/"ventas_empleados(id)", pero esas
     * tablas no existen: por eso esas FK apuntan aquí a clientes(id_cliente)
     * y empleados(id_empleado), las tablas reales de Cristofer. Si él llega
     * a renombrar su esquema para alinearlo al documento, hay que actualizar
     * estas referencias también.
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
                    + "CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES personas(id_persona) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // -- sección Producto/Categoria/Inventario/Proveedor/Bodega -- (André)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_categorias ("
                    + "id_categoria INT NOT NULL PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "descripcion VARCHAR(200)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_proveedores ("
                    + "id_proveedor INT NOT NULL PRIMARY KEY,"
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
                    + "id_bodega INT NOT NULL PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "direccion VARCHAR(200),"
                    + "telefono VARCHAR(50)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_inventario ("
                    + "id_inventario INT NOT NULL PRIMARY KEY,"
                    + "producto_id INT NOT NULL,"
                    + "bodega_id INT NOT NULL,"
                    + "cantidad_disponible INT NOT NULL,"
                    + "CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto),"
                    + "CONSTRAINT fk_inventario_bodega FOREIGN KEY (bodega_id) REFERENCES ventas_bodegas(id_bodega)"
                    + ") ENGINE=InnoDB");

            // -- sección Factura/DetalleFactura/OrdenVenta/DetalleOrdenVenta -- (Carlos)
            // Las FK de cliente/vendedor apuntan a las tablas reales que creó
            // Cristofer (clientes/empleados), no a ventas_clientes/
            // ventas_empleados como decía el documento original, porque esas
            // no existen (ver nota de integración arriba).
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_ordenes_venta ("
                    + "id_orden_venta INT NOT NULL PRIMARY KEY,"
                    + "fecha DATE NOT NULL,"
                    + "cliente_id INT NOT NULL,"
                    + "vendedor_id INT NOT NULL,"
                    + "estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',"
                    + "CONSTRAINT fk_ov_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id_cliente),"
                    + "CONSTRAINT fk_ov_vendedor FOREIGN KEY (vendedor_id) REFERENCES empleados(id_empleado)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_orden_venta_detalles ("
                    + "id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "orden_venta_id INT NOT NULL,"
                    + "producto_id INT NOT NULL,"
                    + "cantidad_solicitada INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_ovd_orden FOREIGN KEY (orden_venta_id) REFERENCES ventas_ordenes_venta(id_orden_venta) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_ovd_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto)"
                    + ") ENGINE=InnoDB");

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
                    + "CONSTRAINT fk_factura_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id_cliente),"
                    + "CONSTRAINT fk_factura_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id_empleado)"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ventas_factura_detalles ("
                    + "id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "factura_id INT NOT NULL,"
                    + "producto_id INT NOT NULL,"
                    + "cantidad INT NOT NULL,"
                    + "precio_unitario DECIMAL(12,2) NOT NULL,"
                    + "CONSTRAINT fk_detalle_factura FOREIGN KEY (factura_id) REFERENCES ventas_facturas(id_factura) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES ventas_productos(id_producto)"
                    + ") ENGINE=InnoDB");

            // -- sección Pago/OrdenDespacho/DetalleOrdenDespacho -- (Oscar)
            // Tablas reales de su PagoDAOImpl/OrdenDespachoDAOImpl (nombres y
            // columnas distintos a los que proponía el documento original).
            // id_factura / id_orden_venta quedan sin FOREIGN KEY, tal como
            // los dejó Oscar, porque su DAO no valida contra esas tablas.
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS pagos ("
                    + "id_pago INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_factura INT NOT NULL,"
                    + "fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "monto DECIMAL(10,2) NOT NULL CHECK (monto > 0),"
                    + "tipo_pago VARCHAR(20) NOT NULL,"
                    + "referencia VARCHAR(100),"
                    + "estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADO'"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ordenes_despacho ("
                    + "id_orden_despacho INT AUTO_INCREMENT PRIMARY KEY,"
                    + "id_orden_venta INT NOT NULL,"
                    + "fecha_despacho TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "direccion_entrega VARCHAR(200) NOT NULL,"
                    + "transportista VARCHAR(100),"
                    + "estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'"
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

        } catch (SQLException e) {
            throw new ConexionException("No se pudieron crear las tablas del sistema", e);
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
