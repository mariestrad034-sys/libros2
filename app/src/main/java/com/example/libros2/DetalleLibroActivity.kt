package com.example.libros2

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityDetalleLibroBinding
import java.io.File

class DetalleLibroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleLibroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleLibroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos desde el Intent
        val titulo = intent.getStringExtra("titulo") ?: "Sin título"
        val autor = intent.getStringExtra("autor") ?: "Sin autor"
        val sinopsis = intent.getStringExtra("sinopsis") ?: "No hay sinopsis disponible."
        val rutaImagen = intent.getStringExtra("rutaImagen") ?: "sin_imagen"

        // Desescapar la sinopsis (reemplazar [ENTER] por saltos de línea reales)
        val sinopsisFormateada = sinopsis.replace("[ENTER]", "\n")

        // Establecer datos en la interfaz
        binding.tvTituloDetalle.text = titulo
        binding.tvTituloLibro.text = titulo
        binding.tvAutorLibro.text = "✍️ $autor"
        binding.tvSinopsisDetalle.text = if (sinopsisFormateada.trim().isEmpty()) {
            "No hay sinopsis disponible."
        } else {
            sinopsisFormateada
        }

        // Cargar imagen
        if (rutaImagen != "sin_imagen" && File(rutaImagen).exists()) {
            binding.ivPortadaDetalle.setImageURI(Uri.fromFile(File(rutaImagen)))
        } else {
            binding.ivPortadaDetalle.setBackgroundColor(android.graphics.Color.parseColor("#424242"))
        }

        // Botón volver
        binding.btnVolver.setOnClickListener {
            finish() // Cierra la actividad y vuelve a VerActivity
        }
    }
}