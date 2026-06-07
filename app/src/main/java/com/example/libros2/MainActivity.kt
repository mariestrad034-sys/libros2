package com.example.libros2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}