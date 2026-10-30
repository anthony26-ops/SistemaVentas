# Sistema de Ventas - App Android

App móvil (Kotlin) para **tomar pedidos desde el teléfono**: se elige cliente, vendedor,
productos con su cantidad y método de pago, y se guarda el pedido. También muestra el
historial de pedidos con su estado. Fue hecha por Aramis Ramos y se integró a este repositorio
desde <https://github.com/NotAramis/ProyectoSistemaVentasAndroid> (commit `c322e7b`).

## Cómo se conecta: cable USB + API REST

El teléfono **no habla con MySQL**: llama a una **API REST** (`src/main/java/api/ServidorApi.java`,
parte del proyecto Java) que corre en el computador y usa la misma base de datos y los mismos
controladores que el escritorio. La conexión entre el teléfono y el computador es **por cable USB**.

```
 Teléfono (app)  --HTTP a 127.0.0.1:8090-->  cable USB (adb reverse)  -->  API REST  -->  MySQL
                                                                    (computador)       sistema_ventas
                                                                                          ^
 Programa de escritorio -------------------------------------------------------------------+
```

- Dentro del teléfono, `127.0.0.1:8090` es "el propio teléfono". El comando `adb reverse` crea un
  túnel por el cable USB que lleva ese puerto hasta el puerto de la API en el computador.
- La API escucha **solo en `127.0.0.1`**: nadie en el wifi o la red puede llegar a ella. La única
  forma de alcanzarla desde el teléfono es el cable.
- La app **ya no lleva usuario ni contraseña de MySQL**.

Un pedido hecho en el teléfono queda en estado `Pendiente` y aparece solo en el módulo
**Despacho** del escritorio, donde sigue el flujo normal (despacho, factura, pago).

## Cómo ejecutarlo (cada vez)

Necesita en el computador: MySQL corriendo, JDK 17+, Maven y, para el cable, `adb`
(viene en Android Studio, carpeta `platform-tools` del SDK; en Mac suele ser
`~/Library/Android/sdk/platform-tools/adb`, en Windows
`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`).

**Una sola vez en el teléfono:** activar *Opciones de desarrollador* y dentro *Depuración por USB*
(Ajustes > Acerca del teléfono > tocar 7 veces "Número de compilación"). En Windows puede hacer
falta el driver USB del fabricante.

1. **Levantar la API** (desde la carpeta raíz del proyecto, la que tiene `pom.xml`):

   ```bash
   mvn -q compile org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.mainClass=api.ServidorApi
   ```

   También se puede abrir `api/ServidorApi.java` en NetBeans y ejecutar el archivo (Run File).
   Debe imprimir `API de Sistema de Ventas escuchando en http://127.0.0.1:8090/api/`. Al
   arrancar prepara la base igual que el escritorio, así que no hace falta abrir antes el escritorio.
   Se detiene con Ctrl+C.

2. **Conectar el teléfono por cable** y aceptar el aviso "¿Permitir depuración por USB?". Comprobar:

   ```bash
   adb devices
   ```

   Debe aparecer el teléfono como `device` (no `unauthorized`).

3. **Abrir el túnel** (hay que repetirlo si se desconecta el cable o se reinicia adb):

   ```bash
   adb reverse tcp:8090 tcp:8090
   ```

4. **Ejecutar la app** desde Android Studio abriendo la carpeta `android/` y eligiendo el teléfono
   como destino (▶ Run). Los pasos 2 y 3 también sirven para un **emulador**.

Si la app muestra *"No se pudo conectar con la API..."*: la API no está corriendo, el cable no está
conectado o falta el paso 3.

**Si el puerto 8090 ya está ocupado** en el computador, la API puede usar otro, por ejemplo con
`-DAPI_PORT=9000` en el comando del paso 1, y el túnel se abre así: `adb reverse tcp:8090 tcp:9000`
(la app siempre usa el 8090 dentro del teléfono; solo cambia el segundo número).

## La API

Todas las respuestas son JSON; los errores tienen la forma `{"error": "mensaje"}`.

| Método y ruta | Qué hace |
|---|---|
| `GET /api/salud` | Comprueba que la API está viva |
| `GET /api/clientes` | Catálogo de clientes (`id`, `nombre`, `apellido`) |
| `GET /api/empleados` | Catálogo de vendedores |
| `GET /api/productos` | Catálogo con `precio` y `existencia` |
| `GET /api/pedidos` | Historial, del más nuevo al más viejo |
| `GET /api/pedidos/{id}` | Un pedido con sus productos |
| `POST /api/pedidos` | Registra un pedido (responde `201` con `id` y `total`) |

Cuerpo de `POST /api/pedidos`:

```json
{
  "idCliente": 1,
  "idEmpleado": 2,
  "metodoPago": "Efectivo",
  "observaciones": "Entregar por la tarde",
  "productos": [
    { "idProducto": 1001, "cantidad": 2 },
    { "idProducto": 1002, "cantidad": 1 }
  ]
}
```

El teléfono **solo manda ids y cantidades**: el precio sale del catálogo y el total lo calcula el
servidor, así que no se pueden falsificar desde la app. La API aplica las mismas reglas que el
escritorio al crear una orden (cliente y vendedor deben existir, cantidades mayores a cero y
existencia suficiente) y responde `400` con el motivo cuando algo no se cumple. Si el mismo
producto viene en dos líneas, las cantidades se suman.

Prueba rápida sin teléfono:

```bash
curl http://127.0.0.1:8090/api/productos
```

## Columnas que agregó esta integración

La app guarda `metodo_pago` y `observaciones` en `ordenes_venta`. Esas columnas no existían: se
agregaron a `bd/ConexionMySQL.java` (con migración automática para bases ya creadas) y a
`database/sistema_ventas.sql`. Son opcionales: los pedidos hechos en el escritorio las dejan vacías.

## Limitaciones conocidas

- **La API no tiene login.** Su protección es que solo es alcanzable desde el propio computador y,
  por el túnel, desde el teléfono conectado por cable. Si algún día se publica en una red, habrá que
  agregarle autenticación.
- **Solo funciona con el cable conectado y la API corriendo** (no hay modo sin conexión).
- **Método de pago.** La app ofrece Efectivo, Tarjeta, Transferencia y Crédito, pero es solo una
  etiqueta guardada en el pedido. El módulo Pagos del escritorio hoy solo acepta Efectivo.
- El teléfono solo puede usar `http` hacia `127.0.0.1`/`localhost` (`res/xml/network_security_config.xml`).

## Qué se cambió respecto al repositorio original

- La app dejó de conectarse directo a MySQL: el nuevo `Api.kt` es el único punto de comunicación y
  las pantallas (`MainActivity.kt`, `HistorialActivity.kt`) leen JSON en lugar de usar JDBC. Se quitó
  la dependencia `mysql-connector-java` de `app/build.gradle.kts`.
- El repositorio original es público y traía la contraseña de MySQL escrita en el código; **esa
  contraseña quedó en su historial** y quien la usó en su MySQL debe **cambiarla**. Aquí no se copió.
- Nombre de la app: de "contacto 1" a "Sistema de Ventas".
- No se copió la carpeta `.idea` (configuración personal del IDE; Android Studio la regenera).

## Estado de la verificación

Verificado en este computador:

- La API contra una base nueva: todos los endpoints, validaciones (cliente, vendedor o producto
  inexistente, cantidad cero o negativa, sin existencia, JSON inválido, cuerpo demasiado grande,
  método no permitido), tildes guardadas en UTF-8 y 20 pedidos simultáneos sin errores.
- El escritorio llevó un pedido creado por la API hasta el pago.
- `Api.kt` ejecutado contra la API real, con el `org.json` de Android, incluida la lectura de los
  mismos campos que usan las pantallas y el aviso cuando la API está apagada.
- Los archivos Kotlin compilan con `kotlinc` contra el `android.jar` del SDK.

**No verificado** (no se pudo aquí): la compilación con Gradle en Android Studio, la app corriendo en
un emulador o teléfono, y el túnel `adb reverse` real (no había ningún dispositivo conectado).
