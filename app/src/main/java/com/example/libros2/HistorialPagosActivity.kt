package com.example.libros2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.libros2.databinding.ActivityHistorialPagosBinding
import java.io.File

class HistorialPagosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialPagosBinding
    private lateinit var rutaAlmacenamiento: String

    external fun obtenerPrestamosPendientes(ruta: String): String
    external fun marcarPagado(ruta: String, titulo: String, persona: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPagosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Cargar y mostrar préstamos pendientes
        cargarPrestamosPendientes()

        // 👇 Botón Pendientes
        binding.btnPendientes.setOnClickListener {
            cargarPrestamosPendientes()
            binding.tvTituloPagos.text = "Préstamos pendientes de pago:"
        }

        // 👇 Botón Realizados
        binding.btnRealizados.setOnClickListener {
            val intent = Intent(this, PagosRealizadosActivity::class.java)
            startActivity(intent)
        }

        // 👇 Botón Volver
        binding.btnVolverHistorial.setOnClickListener {
            finish()
        }
    }

    private fun cargarPrestamosPendientes() {
        binding.containerPagos.removeAllViews()

        // Obtener datos de C++
        val datosRaw = obtenerPrestamosPendientes(rutaAlmacenamiento)

        if (datosRaw.startsWith("No hay") || datosRaw.trim().isEmpty()) {
            val tvMensaje = TextView(this).apply {
                text = "✅ No hay préstamos pendientes de pago."
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@HistorialPagosActivity, R.color.colorTextoSecundario))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 30, 0, 0)
                }
            }
            binding.containerPagos.addView(tvMensaje)
            return
        }

        val margen = 12
        val listaPrestamos = datosRaw.split(";")

        for (prestamoRaw in listaPrestamos) {
            if (prestamoRaw.trim().isEmpty()) continue

            val datos = prestamoRaw.split("|")
            if (datos.size >= 4) {
                val nombre = datos[0]
                val titulo = datos[1]
                val monto = datos[2]
                val estado = datos[3]

                // Crear contenedor para cada préstamo
                val filaPago = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, margen, 0, margen)
                    }
                    setPadding(15, 15, 15, 15)
                    setBackgroundColor(ContextCompat.getColor(this@HistorialPagosActivity, R.color.colorTarjeta))
                }

                // Información del préstamo
                val tvInfo = TextView(this).apply {
                    text = "👤 $nombre\n📚 $titulo\n💵 Monto: $$monto"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@HistorialPagosActivity, R.color.colorTextoPrincipal))
                    setLineSpacing(4f, 1f)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 12)
                    }
                }
                filaPago.addView(tvInfo)

                // Checkbox para marcar como pagado
                val cbPagado = CheckBox(this).apply {
                    text = "✅ Pago Completado"
                    textSize = 15f
                    setTextColor(
                        if (estado == "pagado") {
                            Color.parseColor("#00E676")
                        } else {
                            ContextCompat.getColor(this@HistorialPagosActivity, R.color.colorTextoPrincipal)
                        }
                    )
                    isChecked = (estado == "pagado")

                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            val resultado = marcarPagado(rutaAlmacenamiento, titulo, nombre)
                            Toast.makeText(
                                this@HistorialPagosActivity,
                                resultado,
                                Toast.LENGTH_SHORT
                            ).show()

                            setTextColor(Color.parseColor("#00E676"))

                            cargarPrestamosPendientes()
                        }
                    }
                }
                filaPago.addView(cbPagado)

                binding.containerPagos.addView(filaPago)

                // Línea divisoria
                val divisoria = android.view.View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        setMargins(0, margen, 0, 0)
                    }
                    setBackgroundColor(ContextCompat.getColor(this@HistorialPagosActivity, R.color.colorTextoSecundario))
                }
                binding.containerPagos.addView(divisoria)
            }
        }
    }
}