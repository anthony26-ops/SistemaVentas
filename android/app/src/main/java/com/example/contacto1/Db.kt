package com.example.contacto1

import android.content.Context
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

/**
 * Único lugar donde la app abre la conexión a MySQL. Antes la URL, el usuario y la
 * contraseña estaban escritos dentro de cada pantalla (contraseña incluida, y el
 * repositorio es público).
 *
 * Valores por defecto = los mismos del programa de escritorio: usuario root sin
 * contraseña, base sistema_ventas. El servidor es 10.0.2.2: es la dirección con la que
 * el EMULADOR de Android ve al computador donde corre MySQL ("127.0.0.1" dentro del
 * emulador sería el propio teléfono, no el computador).
 *
 * Para usar otro servidor, usuario o contraseña SIN tocar el código: copiar
 * app/src/main/assets/db.properties.example como db.properties (en esa misma carpeta).
 * Ese archivo está ignorado por git, así ninguna contraseña llega al repositorio.
 */
object Db {

    private const val ARCHIVO = "db.properties"

    fun conectar(context: Context): Connection {
        val cfg = Properties()
        try {
            context.assets.open(ARCHIVO).use { cfg.load(it) }
        } catch (e: java.io.IOException) {
            // No hay db.properties: se usan los valores por defecto.
        }
        val host = cfg.getProperty("host", "10.0.2.2")
        val puerto = cfg.getProperty("puerto", "3306")
        val base = cfg.getProperty("base", "sistema_ventas")
        val usuario = cfg.getProperty("usuario", "root")
        val clave = cfg.getProperty("clave", "")

        Class.forName("com.mysql.jdbc.Driver")
        val url = "jdbc:mysql://$host:$puerto/$base?useSSL=false&serverTimezone=UTC&connectTimeout=8000"
        return DriverManager.getConnection(url, usuario, clave)
    }
}
