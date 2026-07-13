package com.example.libros2

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.libros2.databinding.ActivityDevolverBinding
import java.io.File

class DevolverActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDevolverBinding
    private lateinit var rutaAlmacenamiento: String
    private var libroSeleccionado = ""
    private var montoSeleccionado = ""

    external fun devolverLibro(ruta: String, titulo: String, pagado: Boolean): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevolverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        System.loadLibrary("libros2")

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Cargar libros prestados
        cargarLibrosPrestados()

        binding.btnDevolverLibro.setOnClickListener {
            if (libroSeleccionado.isEmpty()) {
                binding.tvResultadoDevolver.text = "Por favor selecciona un libro."
                binding.tvResultadoDevolver.setTextColor(ContextCompat.getColor(this, R.color.colorTextoSecundario))
                return@setOnClickListener
            }

            // ✅ IMPORTANTE: El checkbox debe coincidir con el estado del pago
            val pagado = binding.cbPagoConfirmado.isChecked

            val resultado = devolverLibro(rutaAlmacenamiento, libroSeleccionado, pagado)
            binding.tvResultadoDevolver.text = resultado
            binding.tvResultadoDevolver.setTextColor(
                ContextCompat.getColor(this, R.color.colorTextoSecundario)
            )

            // Limpiar y recargar
            libroSeleccionado = ""
            montoSeleccionado = ""
            binding.containerPagoConfirmacion.visibility = View.GONE
            binding.cbPagoConfirmado.isChecked = false
            cargarLibrosPrestados()
        }
    }

    private fun cargarLibrosPrestados() {
        binding.llLibrosPrestados.removeAllViews()
        val file = File(rutaAlmacenamiento)

        if (file.exists()) {
            file.forEachLine { linea ->
                val partes = linea.split("|")
                if (partes.size >= 9) {
                    val titulo = partes[0]
                    val prestado = partes[3]
                    val monto = partes[8]

                    if (prestado == "1") { // "1" significa prestado
                        val checkBox = CheckBox(this).apply {
                            text = "📚 $titulo"
                            textSize = 16f
                            setTextColor(ContextCompat.getColor(
                                this@DevolverActivity,
                                R.color.colorTextoPrincipal
                            ))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 8, 0, 8)
                            }

                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    libroSeleccionado = titulo
                                    montoSeleccionado = monto

                                    // Mostrar u ocultar el container de pago según si tiene monto
                                    if (monto.isNotEmpty() && monto != "0") {
                                        binding.containerPagoConfirmacion.visibility = View.VISIBLE
                                        binding.tvMontoDevolucion.text = "Monto a pagar: \$$monto"
                                        // NO marcamos automáticamente el checkbox
                                        binding.cbPagoConfirmado.isChecked = false
                                    } else {
                                        binding.containerPagoConfirmacion.visibility = View.GONE
                                        binding.cbPagoConfirmado.isChecked = false
                                    }
                                } else {
                                    libroSeleccionado = ""
                                    montoSeleccionado = ""
                                    binding.containerPagoConfirmacion.visibility = View.GONE
                                    binding.cbPagoConfirmado.isChecked = false
                                }
                            }
                        }
                        binding.llLibrosPrestados.addView(checkBox)
                    }
                }
            }
        }
    }
}
