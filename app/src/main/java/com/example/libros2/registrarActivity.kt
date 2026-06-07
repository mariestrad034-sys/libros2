package com.example.libros2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityRegistrarBinding
import java.io.File

class RegistrarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarBinding
    private lateinit var rutaAlmacenamiento: String

    // Modificamos la función nativa para que también reciba la cadena de géneros
    external fun registrarLibro(ruta: String, titulo: String, autor: String, persona: String, prestado: Boolean, generos: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        binding.btnRegistrar.setOnClickListener {
            val titulo = binding.etTitulo.text.toString().trim()
            val autor = binding.etAutor.text.toString().trim()
            val persona = binding.etPersona.text.toString().trim()
            val prestado = binding.cbPrestado.isChecked

            // 1. Validación básica de Título y Autor
            if (titulo.isEmpty() || autor.isEmpty()) {
                Toast.makeText(this, "Por favor llena Título y Autor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. CORRECCIÓN SOLICITADA: Validar que si está prestado, ponga el nombre "a juro"
            if (prestado && persona.isEmpty()) {
                Toast.makeText(this, "❌ Debes colocar el nombre de la persona a quien se lo prestaste", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 3. Juntar los géneros seleccionados en un solo texto (ej: "Romance, Drama")
            val listaGeneros = mutableListOf<String>()
            if (binding.cbRomance.isChecked) listaGeneros.add("Romance")
            if (binding.cbAccion.isChecked) listaGeneros.add("Acción")
            if (binding.cbDrama.isChecked) listaGeneros.add("Drama")
            if (binding.cbComedia.isChecked) listaGeneros.add("Comedia")

            if (listaGeneros.isEmpty()) {
                Toast.makeText(this, "Por favor selecciona al menos un género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convertimos la lista de géneros a un solo String separado por comas
            val generosTexto = listaGeneros.joinToString(", ")

            // Mandamos todo a C++
            val resultado = registrarLibro(rutaAlmacenamiento, titulo, autor, persona, prestado, generosTexto)
            binding.tvResultado.text = resultado

            // Limpiar el formulario
            binding.etTitulo.text.clear()
            binding.etAutor.text.clear()
            binding.etPersona.text.clear()
            binding.cbPrestado.isChecked = false
            binding.cbRomance.isChecked = false
            binding.cbAccion.isChecked = false
            binding.cbDrama.isChecked = false
            binding.cbComedia.isChecked = false
        }
    }
}