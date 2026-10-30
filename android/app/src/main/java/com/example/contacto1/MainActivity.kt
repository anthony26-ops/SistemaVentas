package com.example.contacto1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

data class DetallePedido(val idProducto: Int, val nombre: String, val cantidad: Int, val precio: Double, val subtotal: Double)
data class ProductoInfo(val id: Int, val nombre: String, val precio: Double)

class MainActivity : AppCompatActivity() {


    private val executor = Executors.newSingleThreadExecutor()
    private val detallesGuardados = mutableListOf<DetallePedido>()
    private val listaVisual = mutableListOf<String>()

    // Listas dinámicas para la UI y mapeo de datos
    private val listaClientes = mutableListOf<String>()
    private val listaVendedores = mutableListOf<String>()
    private val listaProductosPresentacion = mutableListOf<String>()
    private val mapaProductos = mutableMapOf<Int, ProductoInfo>()

    private var totalPedido = 0.00
    private lateinit var adapterDetalles: ArrayAdapter<String>

    private lateinit var spinnerClientes: AutoCompleteTextView
    private lateinit var spinnerVendedores: AutoCompleteTextView
    private lateinit var spinnerProductos: AutoCompleteTextView
    private lateinit var spinnerPago: AutoCompleteTextView
    private lateinit var lblTotal: TextView
    private lateinit var txtCantidad: EditText
    private lateinit var txtObservaciones: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lblFechaActual = findViewById<TextView>(R.id.lblFechaActual)
        spinnerClientes = findViewById(R.id.spinnerClientes)
        spinnerVendedores = findViewById(R.id.spinnerVendedores)
        spinnerPago = findViewById(R.id.spinnerPago)
        txtObservaciones = findViewById(R.id.txtObservaciones)
        spinnerProductos = findViewById(R.id.spinnerProductos)
        txtCantidad = findViewById(R.id.txtCantidad)
        lblTotal = findViewById(R.id.lblTotal)
        val listaDetallesView = findViewById<ListView>(R.id.listaDetalles)

        val btnAgregarDetalle = findViewById<Button>(R.id.btnAgregarDetalle)
        val btnLimpiar = findViewById<Button>(R.id.btnLimpiar)
        val btnGenerarPedido = findViewById<Button>(R.id.btnGenerarPedido)
        val btnVerHistorial = findViewById<Button>(R.id.btnVerHistorial)

        btnVerHistorial.setOnClickListener {
            val intent = android.content.Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        lblFechaActual.text = sdf.format(Date())

        // Opciones fijas para método de pago
        val pagos = arrayOf("Efectivo", "Tarjeta", "Transferencia", "Crédito")
        spinnerPago.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pagos))

        adapterDetalles = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaVisual)
        listaDetallesView.adapter = adapterDetalles

        // Agregar detalle usando el precio dinámico del catálogo
        btnAgregarDetalle.setOnClickListener {
            val seleccion = spinnerProductos.text.toString()
            val cantidadStr = txtCantidad.text.toString()

            if (seleccion.isNotEmpty() && cantidadStr.isNotEmpty()) {
                try {
                    val idProducto = seleccion.split(" - ")[0].toInt()
                    val productoObj = mapaProductos[idProducto]

                    if (productoObj != null) {
                        val cantidad = cantidadStr.toInt()
                        val precio = productoObj.precio
                        val subtotal = cantidad * precio

                        detallesGuardados.add(DetallePedido(idProducto, productoObj.nombre, cantidad, precio, subtotal))
                        listaVisual.add("$cantidad x ${productoObj.nombre} = Q$subtotal")
                        adapterDetalles.notifyDataSetChanged()

                        totalPedido += subtotal
                        lblTotal.text = "Total: Q$totalPedido"
                        txtCantidad.text.clear()
                    } else {
                        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Formato inválido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Selecciona producto y cantidad", Toast.LENGTH_SHORT).show()
            }
        }

        btnLimpiar.setOnClickListener {
            limpiarFormulario()
        }

        btnGenerarPedido.setOnClickListener {
            guardarPedido()
        }
    }

    override fun onResume() {
        super.onResume()
        // Carga los datos más recientes del sistema cada vez que la pantalla se muestra
        cargarCatalogos()
    }

    private fun cargarCatalogos() {
        executor.execute {
            try {
                // 1. Cargar Clientes
                val clientes = Api.obtenerLista("/clientes")
                val tempClientes = mutableListOf<String>()
                for (i in 0 until clientes.length()) {
                    val c = clientes.getJSONObject(i)
                    tempClientes.add("${c.getInt("id")} - ${c.getString("nombre")} ${c.getString("apellido")}")
                }

                // 2. Cargar Vendedores/Empleados
                val empleados = Api.obtenerLista("/empleados")
                val tempVendedores = mutableListOf<String>()
                for (i in 0 until empleados.length()) {
                    val e = empleados.getJSONObject(i)
                    tempVendedores.add("${e.getInt("id")} - ${e.getString("nombre")} ${e.getString("apellido")}")
                }

                // 3. Cargar Productos y Precios
                val productos = Api.obtenerLista("/productos")
                val tempProductosStr = mutableListOf<String>()
                val tempProductosMap = mutableMapOf<Int, ProductoInfo>()
                for (i in 0 until productos.length()) {
                    val p = productos.getJSONObject(i)
                    val id = p.getInt("id")
                    val nombre = p.getString("nombre")
                    val precio = p.getDouble("precio")

                    tempProductosStr.add("$id - $nombre (Q$precio)")
                    tempProductosMap[id] = ProductoInfo(id, nombre, precio)
                }

                // Actualizar la interfaz en el hilo principal
                runOnUiThread {
                    listaClientes.clear()
                    listaClientes.addAll(tempClientes)
                    spinnerClientes.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, listaClientes))

                    listaVendedores.clear()
                    listaVendedores.addAll(tempVendedores)
                    spinnerVendedores.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, listaVendedores))

                    listaProductosPresentacion.clear()
                    listaProductosPresentacion.addAll(tempProductosStr)
                    mapaProductos.clear()
                    mapaProductos.putAll(tempProductosMap)
                    spinnerProductos.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, listaProductosPresentacion))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cargar catálogos: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun guardarPedido() {
        val clienteSeleccionado = spinnerClientes.text.toString()
        val vendedorSeleccionado = spinnerVendedores.text.toString()
        val pagoSeleccionado = spinnerPago.text.toString()
        val observaciones = txtObservaciones.text.toString()

        if (detallesGuardados.isEmpty() || clienteSeleccionado.isEmpty() || vendedorSeleccionado.isEmpty() || pagoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Faltan datos requeridos (Cliente, Vendedor, Pago o Productos)", Toast.LENGTH_LONG).show()
            return
        }

        val idCliente = clienteSeleccionado.split(" - ")[0].toIntOrNull()
        val idVendedor = vendedorSeleccionado.split(" - ")[0].toIntOrNull()
        if (idCliente == null || idVendedor == null) {
            Toast.makeText(this, "Elige el cliente y el vendedor de la lista", Toast.LENGTH_LONG).show()
            return
        }

        // Solo se mandan ids y cantidades: el precio y el total los calcula la API con el catálogo.
        val productos = JSONArray()
        for (detalle in detallesGuardados) {
            productos.put(JSONObject().put("idProducto", detalle.idProducto).put("cantidad", detalle.cantidad))
        }
        val pedido = JSONObject()
            .put("idCliente", idCliente)
            .put("idEmpleado", idVendedor)
            .put("metodoPago", pagoSeleccionado)
            .put("observaciones", observaciones)
            .put("productos", productos)

        executor.execute {
            try {
                val guardado = Api.enviarPedido(pedido)

                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "¡Pedido #${guardado.getInt("id")} de Q${guardado.getDouble("total")} guardado!",
                        Toast.LENGTH_LONG
                    ).show()
                    limpiarFormulario()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al guardar: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun limpiarFormulario() {
        detallesGuardados.clear()
        listaVisual.clear()
        adapterDetalles.notifyDataSetChanged()
        totalPedido = 0.00
        lblTotal.text = "Total: Q0.00"
        spinnerClientes.text.clear()
        spinnerVendedores.text.clear()
        spinnerPago.text.clear()
        spinnerProductos.text.clear()
        txtObservaciones.text.clear()
    }
}