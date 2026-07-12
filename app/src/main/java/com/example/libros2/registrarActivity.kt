package com.example.libros2

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityRegistrarBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class RegistrarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarBinding
    private lateinit var rutaAlmacenamiento: String
    private var rutaImagenSeleccionada: String = "sin_imagen"

    // Firma nativa actualizada para incluir la sinopsis y el monto
    external fun registrarLibro(
        ruta: String,
        titulo: String,
        autor: String,
        persona: String,
        prestado: Boolean,
        generos: String,
        imagen: String,
        fechaDevolucion: String,
        sinopsis: String,
        monto: String
    ): String

    private val abrirGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.ivPortadaPreview.setImageURI(uri)
                rutaImagenSeleccionada = guardarImagenInternamente(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        binding.btnSeleccionarImagen.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        // CONTROLADOR DE CALENDARIO: Despliega el DatePicker nativo tipo HTML
        binding.etFechaDevolucion.setOnClickListener {
            val calendarioActual = Calendar.getInstance()
            val anio = calendarioActual.get(Calendar.YEAR)
            val mes = calendarioActual.get(Calendar.MONTH)
            val dia = calendarioActual.get(Calendar.DAY_OF_MONTH)

            val selectorFecha = DatePickerDialog(this, { _, anioSel, mesSel, diaSel ->
                val fechaFormateada = String.format("%02d/%02d/%d", diaSel, mesSel + 1, anioSel)
                binding.etFechaDevolucion.setText(fechaFormateada)
            }, anio, mes, dia)

            selectorFecha.datePicker.minDate = System.currentTimeMillis() // Evita fechas pasadas
            selectorFecha.show()
        }

        // Mostrar u ocultar campos extras si está prestado
        binding.cbPrestado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etPersona.visibility = View.VISIBLE
                binding.etFechaDevolucion.visibility = View.VISIBLE
                binding.etMontoPrestamo.visibility = View.VISIBLE
            } else {
                binding.etPersona.visibility = View.GONE
                binding.etFechaDevolucion.visibility = View.GONE
                binding.etMontoPrestamo.visibility = View.GONE
                binding.etPersona.text.clear()
                binding.etFechaDevolucion.text.clear()
                binding.etMontoPrestamo.text.clear()
            }
        }

        binding.btnGuardar.setOnClickListener {
            val titulo = binding.etTitulo.text.toString().trim()
            val autor = binding.etAutor.text.toString().trim()
            val persona = binding.etPersona.text.toString().trim()
            val prestado = binding.cbPrestado.isChecked
            val fechaDevolucion = binding.etFechaDevolucion.text.toString().trim()
            val sinopsis = binding.etSinopsis.text.toString().trim()
            val monto = binding.etMontoPrestamo.text.toString().trim()

            if (titulo.isEmpty() || autor.isEmpty()) {
                Toast.makeText(this, "Por favor llena Título y Autor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (prestado && (persona.isEmpty() || fechaDevolucion.isEmpty())) {
                Toast.makeText(
                    this,
                    "Debes ingresar la persona y la fecha de devolución",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val listaGeneros = mutableListOf<String>()
            if (binding.cbRomance.isChecked) listaGeneros.add("Romance")
            if (binding.cbAccion.isChecked) listaGeneros.add("Acción")
            if (binding.cbDrama.isChecked) listaGeneros.add("Drama")
            if (binding.cbComedia.isChecked) listaGeneros.add("Comedia")

            if (listaGeneros.isEmpty()) {
                Toast.makeText(this, "Por favor selecciona al menos un género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val generosTexto = listaGeneros.joinToString(",")

            // Enviamos todos los parámetros procesados a C++
            val resultado = registrarLibro(
                rutaAlmacenamiento,
                titulo,
                autor,
                persona,
                prestado,
                generosTexto,
                rutaImagenSeleccionada,
                fechaDevolucion,
                sinopsis,
                monto
            )

            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show()

            // Limpieza completa del formulario para el próximo registro
            binding.etTitulo.text.clear()
            binding.etAutor.text.clear()
            binding.etPersona.text.clear()
            binding.etFechaDevolucion.text.clear()
            binding.etSinopsis.text.clear()
            binding.etMontoPrestamo.text.clear()
            binding.cbPrestado.isChecked = false
            binding.cbRomance.isChecked = false
            binding.cbAccion.isChecked = false
            binding.cbDrama.isChecked = false
            binding.cbComedia.isChecked = false
            binding.etPersona.visibility = View.GONE
            binding.etFechaDevolucion.visibility = View.GONE
            binding.etMontoPrestamo.visibility = View.GONE
            binding.ivPortadaPreview.setImageDrawable(null)
            rutaImagenSeleccionada = "sin_imagen"
        }
    }

    private fun guardarImagenInternamente(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nombreArchivo = "portada_${System.currentTimeMillis()}.jpg"
            val archivoDestino = File(filesDir, nombreArchivo)
            val outputStream = FileOutputStream(archivoDestino)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            "sin_imagen"
        }
    }
}