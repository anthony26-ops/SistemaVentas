package com.example.contacto1

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Único lugar donde la app habla con el sistema. Ya no se conecta a MySQL: llama a la
 * API REST del programa de escritorio (clase api.ServidorApi del proyecto Java).
 *
 * La dirección es 127.0.0.1 DENTRO del teléfono. Con el cable USB conectado, el comando
 *     adb reverse tcp:8090 tcp:8090
 * hace que ese puerto del teléfono llegue al puerto 8090 del computador, donde escucha la
 * API. Así los datos viajan por el cable y la API no necesita estar abierta a la red.
 */
object Api {

    private const val BASE = "http://127.0.0.1:8090/api"

    /** Error con un mensaje listo para mostrarle al usuario. */
    class ApiException(mensaje: String) : Exception(mensaje)

    fun obtenerLista(ruta: String): JSONArray = JSONArray(pedir("GET", ruta, null))

    fun enviarPedido(pedido: JSONObject): JSONObject = JSONObject(pedir("POST", "/pedidos", pedido.toString()))

    private fun pedir(metodo: String, ruta: String, cuerpo: String?): String {
        var conexion: HttpURLConnection? = null
        try {
            conexion = URL(BASE + ruta).openConnection() as HttpURLConnection
            conexion.requestMethod = metodo
            conexion.connectTimeout = 5000
            conexion.readTimeout = 10000
            conexion.setRequestProperty("Accept", "application/json")
            if (cuerpo != null) {
                conexion.doOutput = true
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }
            }

            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (codigo !in 200..299) {
                throw ApiException(mensajeDeError(texto, codigo))
            }
            return texto
        } catch (e: IOException) {
            throw ApiException(
                "No se pudo conectar con la API. Revise que esté corriendo en el computador " +
                    "y que el cable USB esté conectado (adb reverse tcp:8090 tcp:8090)."
            )
        } finally {
            conexion?.disconnect()
        }
    }

    /** La API responde los errores como {"error": "mensaje"}. */
    private fun mensajeDeError(texto: String, codigo: Int): String {
        return try {
            JSONObject(texto).optString("error", "Error $codigo del servidor")
        } catch (e: org.json.JSONException) {
            "Error $codigo del servidor"
        }
    }
}
