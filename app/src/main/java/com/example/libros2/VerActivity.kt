package com.example.libros2

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityVerBinding
import java.io.File

class VerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerBinding
    private lateinit var rutaAlmacenamiento: String

    // Declaramos la función nativa que filtra en C++
    external fun filtrarLibrosPorGenero(ruta: String, generoFiltro: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // CONFIGURACIÓN CORREGIDA: Usando android.R.layout para evitar el error de referencia
        val opcionesFiltro = arrayOf("Todos", "Romance", "Acción", "Drama", "Comedia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesFiltro)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignamos el adaptador al Spinner real
        binding.spinnerGeneros.adapter = adapter

        // Detectar cuándo el usuario cambia de opción en el filtro
        binding.spinnerGeneros.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val generoSeleccionado = opcionesFiltro[position]

                // Llamamos a C++ pasándole la ruta del archivo y el género que queremos buscar
                val librosFiltrados = filtrarLibrosPorGenero(rutaAlmacenamiento, generoSeleccionado)

                // ✅ CORREGIDO: Se eliminó la línea de tvResultadoDevolver que causaba el error
                binding.tvCatalogoContenido.text = librosFiltrados
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se hace nada si no hay selección
            }
        }
    }
}