# Sistema de Ventas

Proyecto de Programación II: sistema de escritorio en Java Swing (MVC + DAO) con base
de datos MySQL. Cubre el flujo Cliente → Orden de Venta → Despacho → Salida de
Inventario → Facturación → Pago.

## Requisitos previos

Cada integrante necesita, en su propia máquina:

1. **JDK 26** o superior (el `pom.xml` compila con `maven.compiler.release=26`).
2. **MySQL** instalado y **corriendo** en `127.0.0.1:3306`. Por defecto el proyecto se
   conecta con el usuario `root` y **sin contraseña**. Si tu `root` tiene contraseña (es lo
   normal con el instalador de MySQL en Windows), **no edites el código**: define las
   variables de entorno antes de abrir el programa:
   - Windows: `setx DB_PASSWORD "tu_clave"` (una sola vez; `setx DB_USER "otro_usuario"` si
     no es `root`), y **cierra y vuelve a abrir NetBeans / la terminal**.
   - Mac/Linux: `export DB_PASSWORD=tu_clave` y abre NetBeans o el programa **desde esa
     misma terminal**.
   - Desde Maven también sirve `-DDB_PASSWORD=tu_clave` en el comando.
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

Al iniciar, el programa automáticamente:

1. Crea la base de datos `sistema_ventas` si no existe.
2. Crea todas las tablas necesarias (y actualiza las de una versión anterior).
3. Abre directamente el menú principal.

## Acceso al sistema

**El sistema ya no pide usuario ni contraseña**: abre directo en el menú principal con
rol administrador (acceso a todos los módulos). Así nadie queda bloqueado por tener una
base de datos distinta a la de otro integrante.

Si algún día se necesita volver a exigir login, basta cambiar una constante en
`SistemaVentas.java`: `REQUIERE_LOGIN = true` (la pantalla de login y la tabla
`ventas_usuarios` siguen intactas; el usuario de prueba es `admin` / `admin123`).

## Problemas comunes

- **Al abrir aparece "No se pudo preparar la base de datos MySQL"**: el mensaje trae la
  causa real. Casos típicos:
  - `Access denied for user 'root'`: tu `root` tiene contraseña; define `DB_PASSWORD`
    (ver Requisitos).
  - `Communications link failure` / `Connection refused`: MySQL no está corriendo o no
    está en el puerto 3306. Verifica con `mysqladmin -h127.0.0.1 -uroot ping`.
- **La base quedó a medias o dando errores raros** (por ejemplo, la creaste con una
  versión muy vieja del proyecto): bórrala y deja que el programa la cree limpia.
  Se pierden los datos de esa base, que son solo de prueba:
  ```bash
  mysql -uroot -p -e "DROP DATABASE IF EXISTS sistema_ventas;"
  ```
  y vuelve a abrir el programa.
- **`UnsupportedClassVersionError` al ejecutar**: estás corriendo el programa con una
  versión de Java más vieja que la que lo compiló. Revisa `java -version` y que
  apunte a JDK 26+.
- **Puerto 3306 ocupado por otra instancia de MySQL/MariaDB**: detén la otra instancia
  o cambia el `PORT` en `ConexionMySQL.java` (avísale al resto del equipo: es un
  cambio compartido).
