package com.example.libros2

import android.os.Bundle
import android.view.View
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

        // Detectar en tiempo real si marcan o desmarcan "Prestado"
        binding.cbPrestado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si está marcado, aparece el campo con visibilidad VISIBLE
                binding.etPersona.visibility = View.VISIBLE
            } else {
                // Si se desmarca, se esconde (GONE) y se limpia el texto automáticamente
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

            // 2. Validar que si está prestado, ponga el nombre obligatoriamente
            if (prestado && persona.isEmpty()) {
                Toast.makeText(this, "❌ Debes colocar el nombre de la persona a quien se lo prestaste", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 3. Juntar los géneros seleccionados en un solo texto
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

            // Mostramos el resultado de C++ en un mensaje flotante (Toast)
            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show()

            // ✅ CORREGIDO: Se eliminó la línea "Kleid()" que causaba el error
            binding.etTitulo.text.clear()
            binding.etAutor.text.clear()
            binding.etPersona.text.clear()
            binding.cbPrestado.isChecked = false
            binding.cbRomance.isChecked = false
            binding.cbAccion.isChecked = false
            binding.cbDrama.isChecked = false
            binding.cbComedia.isChecked = false

            // Aseguramos que el campo se esconda de nuevo al reiniciar el formulario
            binding.etPersona.visibility = View.GONE
        }
    }
}