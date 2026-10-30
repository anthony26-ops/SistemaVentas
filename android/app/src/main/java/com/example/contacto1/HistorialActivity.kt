package com.example.contacto1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.util.concurrent.Executors

class HistorialActivity : AppCompatActivity() {


    private val executor = Executors.newSingleThreadExecutor()
    private val listaOrdenes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val listaHistorialView = findViewById<ListView>(R.id.listaHistorial)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaOrdenes)
        listaHistorialView.adapter = adapter

        cargarHistorial()
    }

    private fun cargarHistorial() {
        executor.execute {
            try {
                val conn: Connection = Db.conectar(this@HistorialActivity)

                // Extraemos las órdenes desde MySQL de la más nueva a la más vieja
                val sql = "SELECT id, fecha, total, metodo_pago FROM ordenes_venta ORDER BY id DESC"
                val stmt = conn.createStatement()
                val rs = stmt.executeQuery(sql)

                while (rs.next()) {
                    val id = rs.getInt("id")
                    val fecha = rs.getString("fecha")
                    val total = rs.getDouble("total")
                    val pago = rs.getString("metodo_pago") ?: "No especificado"

                    // Formateamos cómo se verá cada fila en la pantalla
                    val infoOrden = "Orden #$id - Fecha: $fecha\nTotal: Q$total | Pago: $pago"
                    listaOrdenes.add(infoOrden)
                }

                conn.close()

                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    if (listaOrdenes.isEmpty()) {
                        Toast.makeText(this@HistorialActivity, "No hay órdenes registradas", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@HistorialActivity, "Error: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}