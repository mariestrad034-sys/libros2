package com.example.libros2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityEliminarBinding
import java.io.File

class EliminarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEliminarBinding
    private lateinit var rutaAlmacenamiento: String

    external fun eliminarLibro(ruta: String, titulo: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEliminarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        binding.btnEliminar.setOnClickListener {
            val tituloAEliminar = binding.etEliminarTitulo.text.toString().trim()

            if (tituloAEliminar.isEmpty()) {
                Toast.makeText(this, "Ingresa un título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultado = eliminarLibro(rutaAlmacenamiento, tituloAEliminar)
            binding.tvResultado.text = resultado
            binding.etEliminarTitulo.text.clear()
        }
    }
}