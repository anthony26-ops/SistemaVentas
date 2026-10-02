# Sistema de Ventas

Proyecto de Programación II: sistema de escritorio en Java Swing (MVC + DAO) con base
de datos MySQL. Cubre el flujo Cliente → Orden de Venta → Despacho → Salida de
Inventario → Facturación → Pago.

## Requisitos previos

Cada integrante necesita, en su propia máquina:

1. **JDK 26** o superior (el `pom.xml` compila con `maven.compiler.release=26`).
2. **MySQL** instalado y **corriendo** en `127.0.0.1:3306`, con el usuario `root` y
   **sin contraseña**. Esa es la credencial que usa el proyecto por defecto
   (`src/main/java/bd/ConexionMySQL.java`). Si tu `root` local ya tiene contraseña,
   avisa al grupo antes de probar: hay que ajustar `USER`/`PASSWORD` en ese archivo o
   quitarle la contraseña a tu usuario `root`.
3. **Maven** (si vas a correr el proyecto desde terminal) o **NetBeans** (trae Maven
   integrado, es la forma más simple).

No hace falta crear la base de datos a mano ni correr ningún script `.sql`: el
programa la crea sola la primera vez que arranca.

## Cómo traer el proyecto

```bash
git clone https://github.com/anthony26-ops/SistemaVentas.git
cd SistemaVentas
```

Si ya lo tenés clonado, simplemente:

```bash
git pull
```

## Cómo ejecutarlo

### Opción A — Desde NetBeans (recomendada)

1. `File > Open Project...` y seleccioná la carpeta del repo (NetBeans lo reconoce
   como proyecto Maven por el `pom.xml`).
2. Click derecho sobre el proyecto → **Run**, o el botón de "play" verde.
   NetBeans compila y ejecuta `com.mycompany.sistemaventas.SistemaVentas`
   automáticamente.

### Opción B — Desde terminal con Maven

```bash
mvn compile
mvn org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.mainClass=com.mycompany.sistemaventas.SistemaVentas
```

(La primera vez Maven va a descargar el plugin `exec-maven-plugin`, necesita
internet para esa única vez.)

## Primer arranque

Al iniciar por primera vez, el programa automáticamente:

1. Crea la base de datos `sistema_ventas` si no existe.
2. Crea todas las tablas necesarias.
3. Crea un usuario administrador de prueba.

## Iniciar sesión

En la pantalla de login que aparece al abrir el programa:

| Usuario | Contraseña |
|---------|------------|
| `admin` | `admin123` |

Con ese usuario (rol `ADMIN`) tenés acceso a todos los módulos del menú: Clientes,
Empleados, Productos, Bodega/Inventario, Hacer Pedido, Despacho, Facturación y Pagos.

Los demás roles (`VENDEDOR`, `BODEGA`, `CAJA`) solo ven los módulos que les
corresponden, pero todavía no hay una pantalla para crear usuarios nuevos desde la
interfaz — por ahora hay que insertarlos directo en la tabla `ventas_usuarios` (la
contraseña debe guardarse como hash de BCrypt, no en texto plano).

## Problemas comunes

- **"No se pudo conectar a sistema_ventas" / `ConexionException`**: MySQL no está
  corriendo, o está en un puerto distinto a 3306, o tu `root` tiene contraseña.
  Verificá con `mysqladmin -h127.0.0.1 -uroot ping`.
- **`UnsupportedClassVersionError` al ejecutar**: estás corriendo el programa con una
  versión de Java más vieja que la que lo compiló. Revisá `java -version` y que
  apunte a JDK 26+.
- **Puerto 3306 ocupado por otra instancia de MySQL/MariaDB**: detené la otra
  instancia o cambiá el `PORT` en `ConexionMySQL.java` (pero ahí mismo hay que
  avisarle al resto del equipo, porque es un cambio compartido).
