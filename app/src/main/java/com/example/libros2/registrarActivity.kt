package com.example.libros2

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityRegistrarBinding
import java.io.File
import java.io.FileOutputStream

class RegistrarActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarBinding
    private lateinit var rutaAlmacenamiento: String

    // Variable para almacenar la ruta de la foto internamente
    private var rutaImagenSeleccionada: String = "sin_imagen"

    // Modificamos la función nativa para que también reciba la imagen
    external fun registrarLibro(ruta: String, titulo: String, autor: String, persona: String, prestado: Boolean, generos: String, imagen: String): String

    // Lanzador para abrir la galería
    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding.ivPortadaPreview.setImageURI(uri) // Muestra la foto en pantalla
            rutaImagenSeleccionada = guardarImagenInternamente(uri) // Guarda la foto en la app
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Acción del nuevo botón de imagen
        binding.btnSeleccionarImagen.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        // Detectar en tiempo real si marcan o desmarcan "Prestado"
        binding.cbPrestado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etPersona.visibility = View.VISIBLE
            } else {
                binding.etPersona.visibility = View.GONE
                binding.etPersona.text.clear()
            }
        }

        binding.btnGuardar.setOnClickListener {
            val titulo = binding.etTitulo.text.toString().trim()
            val autor = binding.etAutor.text.toString().trim()
            val persona = binding.etPersona.text.toString().trim()
            val prestado = binding.cbPrestado.isChecked

            // 1. Validación básica de Título y Autor
            if (titulo.isEmpty() || autor.isEmpty()) {
                Toast.makeText(this, "Por favor llena Título y Autor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validar que si está prestado, ponga el nombre
            if (prestado && persona.isEmpty()) {
                Toast.makeText(this, "Debes colocar el nombre de la persona a quien se lo prestaste", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 3. Juntar los géneros seleccionados
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

            // Mandamos todo a C++, incluyendo la ruta de la imagen
            val resultado = registrarLibro(rutaAlmacenamiento, titulo, autor, persona, prestado, generosTexto, rutaImagenSeleccionada)

            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show()

            // Limpieza del formulario
            binding.etTitulo.text.clear()
            binding.etAutor.text.clear()
            binding.etPersona.text.clear()
            binding.cbPrestado.isChecked = false
            binding.cbRomance.isChecked = false
            binding.cbAccion.isChecked = false
            binding.cbDrama.isChecked = false
            binding.cbComedia.isChecked = false
            binding.etPersona.visibility = View.GONE

            // Limpiamos la imagen para el siguiente registro
            binding.ivPortadaPreview.setImageDrawable(null)
            rutaImagenSeleccionada = "sin_imagen"
        }
    }

    // Función para copiar la foto de la galería a los archivos privados de la app
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