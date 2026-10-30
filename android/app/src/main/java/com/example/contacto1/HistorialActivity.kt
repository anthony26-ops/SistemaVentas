package com.example.contacto1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                // La API entrega los pedidos de la más nueva a la más vieja
                val pedidos = Api.obtenerLista("/pedidos")
                val filas = mutableListOf<String>()
                for (i in 0 until pedidos.length()) {
                    val p = pedidos.getJSONObject(i)
                    val pago = p.optString("metodoPago", "No especificado")

                    // Formateamos cómo se verá cada fila en la pantalla
                    filas.add(
                        "Orden #${p.getInt("id")} - Fecha: ${p.getString("fecha")}\n" +
                            "Total: Q${p.getDouble("total")} | Pago: $pago\n" +
                            "Estado: ${p.getString("estado")}"
                    )
                }

                runOnUiThread {
                    listaOrdenes.clear()
                    listaOrdenes.addAll(filas)
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
