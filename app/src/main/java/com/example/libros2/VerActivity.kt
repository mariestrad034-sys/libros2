package com.example.libros2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityVerBinding
import java.io.File

class VerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerBinding
    private lateinit var rutaAlmacenamiento: String

    external fun mostrarLibros(ruta: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // Al abrir la pantalla, se cargan automáticamente los libros sin oprimir botones
        val lista = mostrarLibros(rutaAlmacenamiento)
        binding.tvResultado.text = lista
    }
}