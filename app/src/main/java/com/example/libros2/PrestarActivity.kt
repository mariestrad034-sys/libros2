package com.example.libros2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityPrestarBinding
import java.io.File
import java.util.Calendar

class PrestarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestarBinding
    private lateinit var rutaAlmacenamiento: String

    // Firma nativa de C++ actualizada con la fecha de devolución y el monto
    external fun prestarLibro(
        ruta: String,
        titulo: String,
        persona: String,
        fechaDevolucion: String,
        monto: String
    ): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrestarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Cargar la lista al abrir la ventana
        cargarLibrosDisponibles()

        // EVENTO DEL CALENDARIO: Despliega el selector nativo
        binding.etPrestarFechaDevolucion.setOnClickListener {
            val calendarioActual = Calendar.getInstance()
            val anio = calendarioActual.get(Calendar.YEAR)
            val mes = calendarioActual.get(Calendar.MONTH)
            val dia = calendarioActual.get(Calendar.DAY_OF_MONTH)

            val selectorFecha = DatePickerDialog(this, { _, anioSel, mesSel, diaSel ->
                val fechaFormateada = String.format("%02d/%02d/%d", diaSel, mesSel + 1, anioSel)
                binding.etPrestarFechaDevolucion.setText(fechaFormateada)
            }, anio, mes, dia)

            selectorFecha.datePicker.minDate = System.currentTimeMillis() // Evita seleccionar fechas pasadas
            selectorFecha.show()
        }

        binding.btnConfirmarPrestar.setOnClickListener {
            val persona = binding.etPrestarPersona.text.toString().trim()
            val fechaDevolucion = binding.etPrestarFechaDevolucion.text.toString().trim()
            val monto = binding.etMontoPrestar.text.toString().trim()

            // Validación de campos vacíos
            if (persona.isEmpty() || fechaDevolucion.isEmpty()) {
                binding.tvResultadoPrestar.text = "Por favor, ingresa el nombre de la persona y la fecha de devolución."
                return@setOnClickListener
            }

            var prestadosCount = 0

            // Recorremos el contenedor buscando qué CheckBoxes se marcaron
            for (i in 0 until binding.llLibrosDisponibles.childCount) {
                val view = binding.llLibrosDisponibles.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    val titulo = view.text.toString()
                    // Mandamos los datos limpios a C++
                    prestarLibro(rutaAlmacenamiento, titulo, persona, fechaDevolucion, monto)
                    prestadosCount++
                }
            }

            // Resultado final y limpieza
            if (prestadosCount > 0) {
                binding.tvResultadoPrestar.text = "¡$prestadosCount libro(s) prestado(s) exitosamente!"
                binding.etPrestarPersona.text.clear()
                binding.etPrestarFechaDevolucion.text.clear()
                binding.etMontoPrestar.text.clear()
                cargarLibrosDisponibles() // Recargamos la lista
            } else {
                binding.tvResultadoPrestar.text = "Selecciona al menos un libro."
            }
        }
    }

    private fun cargarLibrosDisponibles() {
        binding.llLibrosDisponibles.removeAllViews() // Limpiamos la vista anterior

        val file = File(rutaAlmacenamiento)
        if (file.exists()) {
            file.forEachLine { linea ->
                val partes = linea.split("|")
                // Aseguramos formato correcto (ahora son 8 partes: titulo, autor, persona, prestado, generos, imagen, fecha, sinopsis, monto)
                if (partes.size >= 7) {
                    val titulo = partes[0]
                    val prestado = partes[3]
                    if (prestado == "0") { // "0" significa disponible
                        val checkBox = CheckBox(this)
                        checkBox.text = titulo
                        checkBox.textSize = 16f
                        checkBox.setTextColor(resources.getColor(R.color.colorTextoPrincipal, theme))
                        binding.llLibrosDisponibles.addView(checkBox)
                    }
                }
            }
        }
    }
}