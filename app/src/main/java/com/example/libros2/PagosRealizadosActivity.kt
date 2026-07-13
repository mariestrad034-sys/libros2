package com.example.libros2

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.libros2.databinding.ActivityPagosRealizadosBinding
import java.io.File

class PagosRealizadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagosRealizadosBinding
    private lateinit var rutaAlmacenamiento: String

    external fun obtenerPagosRealizados(ruta: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagosRealizadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Cargar y mostrar pagos realizados
        cargarPagosRealizados()

        binding.btnVolverPagosRealizados.setOnClickListener {
            finish()
        }
    }

    private fun cargarPagosRealizados() {
        binding.containerPagosRealizados.removeAllViews()

        // Obtener datos de C++
        val datosRaw = obtenerPagosRealizados(rutaAlmacenamiento)

        if (datosRaw.startsWith("No hay") || datosRaw.trim().isEmpty()) {
            val tvMensaje = TextView(this).apply {
                text = "No hay pagos realizados aún."
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@PagosRealizadosActivity, R.color.colorTextoSecundario))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 30, 0, 0)
                }
            }
            binding.containerPagosRealizados.addView(tvMensaje)
            return
        }

        val margen = 12
        val listaPagos = datosRaw.split(";")

        for (pagoRaw in listaPagos) {
            if (pagoRaw.trim().isEmpty()) continue

            val datos = pagoRaw.split("|")
            if (datos.size >= 3) {
                val nombre = datos[0]
                val titulo = datos[1]
                val monto = datos[2]

                // Crear contenedor para cada pago realizado
                val filaPago = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, margen, 0, margen)
                    }
                    setPadding(15, 15, 15, 15)
                    setBackgroundColor(ContextCompat.getColor(this@PagosRealizadosActivity, R.color.colorTarjeta))
                }

                // Información del pago
                val tvInfo = TextView(this).apply {
                    text = "👤 $nombre\n📚 $titulo\n💵 Monto: $$monto\n✅ Pagado"
                    textSize = 14f
                    setTextColor(Color.parseColor("#00E676"))
                    setLineSpacing(4f, 1f)
                }
                filaPago.addView(tvInfo)

                binding.containerPagosRealizados.addView(filaPago)

                // Línea divisoria
                val divisoria = android.view.View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        setMargins(0, margen, 0, 0)
                    }
                    setBackgroundColor(ContextCompat.getColor(this@PagosRealizadosActivity, R.color.colorTextoSecundario))
                }
                binding.containerPagosRealizados.addView(divisoria)
            }
        }
    }
}