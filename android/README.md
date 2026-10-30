# Sistema de Ventas - App Android

App móvil (Kotlin) para **tomar pedidos desde el teléfono**: se elige cliente, vendedor,
productos con su cantidad y método de pago, y se guarda el pedido. También muestra el
historial de pedidos. Fue hecha por Aramis Ramos y se integró a este repositorio desde
<https://github.com/NotAramis/ProyectoSistemaVentasAndroid> (commit `c322e7b`).

## Cómo se conecta con el programa de escritorio

Las dos aplicaciones comparten **la misma base de datos MySQL** (`sistema_ventas`). La app
Android se conecta directo a MySQL por JDBC (conector `mysql-connector-java:5.1.49`); no hay
un servidor intermedio.

```
 App Android  --guarda pedido (estado "Pendiente")-->  MySQL  <--  Programa de escritorio
                                                       sistema_ventas
 Escritorio:  Despacho (elige bodega) -> Facturación -> Pago
```

Un pedido hecho en el teléfono aparece solo en el módulo **Despacho** del escritorio, y desde
ahí sigue el flujo normal (despacho, factura, pago).

### Qué usa la app de la base de datos

| Operación | Tabla y columnas |
|---|---|
| Lee catálogos | `clientes` (id_cliente, nombre, apellido), `empleados` (id_empleado, nombre, apellido), `ventas_productos` (id_producto, nombre, precio) |
| Guarda un pedido | `ordenes_venta` (fecha, id_cliente, id_empleado, total, estado, **metodo_pago**, **observaciones**) y `orden_venta_detalles` (id_orden_venta, id_producto, cantidad, precio_unitario) |
| Historial | `ordenes_venta` (id, fecha, total, **metodo_pago**) |

Las columnas en negrita (`metodo_pago`, `observaciones`) **no existían** en la tabla del programa
de escritorio y se agregaron para esta integración (`bd/ConexionMySQL.java`, con migración
automática para bases ya creadas). Son opcionales: los pedidos hechos en el escritorio las dejan
vacías. Si algún día se cambian estas tablas/columnas en el escritorio, hay que actualizar también
la app.

## Cómo ejecutarla

1. Tener MySQL corriendo en el computador y haber **abierto el programa de escritorio al menos
   una vez** con la versión actual (crea la base y agrega las columnas nuevas).
2. Abrir la carpeta `android/` con **Android Studio** (SDK de Android 36.1; si se pide, usar el
   JDK que trae Android Studio) y esperar la sincronización de Gradle.
3. Ejecutar en un **emulador**. Por defecto la app usa `10.0.2.2`, que es la dirección con la
   que el emulador ve al computador donde corre MySQL, con usuario `root` **sin contraseña**
   (igual que el programa de escritorio). No hace falta configurar nada más.

### Cambiar el servidor, usuario o contraseña (sin tocar el código)

Copiar `app/src/main/assets/db.properties.example` como `db.properties` en esa misma carpeta y
ajustar los valores. Ese archivo **está ignorado por git**: así ninguna contraseña llega al
repositorio. Si no existe, se usan los valores por defecto.

### Con un teléfono real

- En `db.properties` poner en `host` la IP del computador en la red wifi (por ejemplo
  `192.168.1.20`), con el teléfono y el computador en la **misma red**.
- MySQL debe aceptar conexiones desde la red (por defecto solo escucha en el propio computador) y
  el usuario debe tener permiso para conectarse desde esa dirección. Esto abre la base de datos a
  la red local: úsenlo solo en una red de confianza.

## Limitaciones conocidas (para tenerlas presentes)

- **Conector antiguo.** El conector 5.1.49 solo sabe autenticar usuarios sin contraseña o con el
  método antiguo `mysql_native_password`. Está **comprobado** que conecta con un `root` sin
  contraseña en MySQL 9.6. Con un usuario con contraseña en MySQL 8.4 o superior (donde el método
  por defecto es `caching_sha2_password`) la conexión puede fallar; la salida más sólida sería
  reemplazar el acceso directo a MySQL por una API (queda como mejora futura).
- **Sin validación de existencias.** La app no comprueba el inventario al tomar el pedido. No se
  pierde nada: el escritorio sí valida la existencia **de la bodega elegida** al despachar y
  rechaza el despacho con un mensaje si no alcanza.
- **Método de pago.** La app ofrece Efectivo, Tarjeta, Transferencia y Crédito, pero es solo una
  etiqueta guardada en el pedido. El módulo Pagos del escritorio hoy solo acepta Efectivo.
- Cada teléfono necesita acceso directo a la base de datos; no hay usuarios ni inicio de sesión.

## Qué se cambió respecto al repositorio original

- Nuevo `Db.kt`: un único punto de conexión; las pantallas dejaron de tener la URL, usuario y
  **contraseña escritos a mano**. El repositorio original es público y esa contraseña quedó en
  su historial: quien la usó en su MySQL debe **cambiarla**.
- `MainActivity.kt` y `HistorialActivity.kt`: solo cambian las líneas que abrían la conexión.
- Nombre de la app: de "contacto 1" a "Sistema de Ventas".
- No se copió la carpeta `.idea` (configuración personal del IDE; Android Studio la regenera).

## Estado de la verificación

- Las consultas SQL de la app se ejecutaron con su mismo conector (5.1.49) contra una base nueva
  con el esquema actualizado, y el escritorio llevó ese pedido hasta el pago.
- Los archivos Kotlin se compilaron con `kotlinc` contra el `android.jar` real del SDK.
- **No se compiló con Gradle ni se ejecutó en un emulador.** La primera compilación en Android
  Studio es la verificación pendiente.
