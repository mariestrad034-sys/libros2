package com.example.libros2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.libros2.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rutaAlmacenamiento: String

    external fun obtenerEstadisticas(ruta: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // LÓGICA DEL MODO OSCURO
        binding.switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Botón para ir a Registrar
        binding.btnMenuRegistrar.setOnClickListener {
            val intent = Intent(this, RegistrarActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir a Ver Libros
        binding.btnMenuVer.setOnClickListener {
            val intent = Intent(this, VerActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir a Eliminar
        binding.btnMenuEliminar.setOnClickListener {
            val intent = Intent(this, EliminarActivity::class.java)
            startActivity(intent)
        }

        // 🔄 NUEVA ACCIÓN: Botón para ir a Prestar Libro
        binding.btnMenuPrestar.setOnClickListener {
            val intent = Intent(this, PrestarActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir a Devolver Libro
        binding.btnMenuDevolver.setOnClickListener {
            val intent = Intent(this, DevolverActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir a Historial de Pagos
        binding.btnMenuHistorialPagos.setOnClickListener {
            val intent = Intent(this, HistorialPagosActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarTableroEstadisticas()
    }

    private fun actualizarTableroEstadisticas() {
        val datosRaw = obtenerEstadisticas(rutaAlmacenamiento)
        val partes = datosRaw.split("-")
        if (partes.size == 3) {
            val total = partes[0]
            val disponibles = partes[1]
            val prestados = partes[2]

            binding.tvTotalLibros.text = "Total de libros: $total"
            binding.tvLibrosDisponibles.text = "Disponibles en estante: $disponibles"
            binding.tvLibrosPrestados.text = "Prestados actualmente: $prestados"
        }
    }
}