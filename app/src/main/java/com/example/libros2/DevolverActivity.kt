package com.example.libros2

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityDevolverBinding
import java.io.File

class DevolverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevolverBinding
    private lateinit var rutaAlmacenamiento: String

    // Enlazamos la función nativa de C++[cite: 1]
    external fun devolverLibro(ruta: String, titulo: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevolverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // 1. Cargar libros que están prestados al abrir la ventana
        cargarLibrosPrestados()

        binding.btnConfirmarDevolver.setOnClickListener {
            var devueltosCount = 0

            // 2. Recorrer los checkboxes buscando cuáles se marcaron
            for (i in 0 until binding.llLibrosPrestados.childCount) {
                val view = binding.llLibrosPrestados.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    // Usamos la propiedad 'tag' que guardamos abajo para obtener el título exacto
                    val titulo = view.tag.toString()

                    // Llamamos a tu función en C++ para cambiar el estado[cite: 1]
                    devolverLibro(rutaAlmacenamiento, titulo)
                    devueltosCount++
                }
            }

            // 3. Resultados
            if (devueltosCount > 0) {
                binding.tvResultadoDevolver.text = "✅ ¡$devueltosCount libro(s) devuelto(s) y disponible(s)!"
                cargarLibrosPrestados() // Recargar la lista para quitar los devueltos
            } else {
                binding.tvResultadoDevolver.text = "⚠️ Selecciona al menos un libro."
            }
        }
    }

    private fun cargarLibrosPrestados() {
        binding.llLibrosPrestados.removeAllViews() // Limpiar lista
        val file = File(rutaAlmacenamiento)

        if (file.exists()) {
            file.forEachLine { linea ->
                // Tu archivo C++ usa el separador '|'[cite: 1]
                val partes = linea.split("|")

                if (partes.size >= 4) {
                    val titulo = partes[0]
                    val persona = partes[2] // A quién se le prestó[cite: 1]
                    val prestado = partes[3] // "1" significa prestado[cite: 1]

                    if (prestado == "1") {
                        val checkBox = CheckBox(this)
                        // Texto amigable para el usuario
                        checkBox.text = "$titulo (De: $persona)"
                        // Guardamos el título exacto, oculto en la propiedad tag, para mandarlo limpio a C++
                        checkBox.tag = titulo
                        checkBox.textSize = 16f
                        checkBox.setTextColor(resources.getColor(R.color.colorTextoPrincipal, theme))

                        binding.llLibrosPrestados.addView(checkBox)
                    }
                }
            }

            // Si después de buscar no hay ninguno prestado, damos un aviso
            if (binding.llLibrosPrestados.childCount == 0) {
                binding.tvResultadoDevolver.text = "No hay libros prestados actualmente."
            } else {
                binding.tvResultadoDevolver.text = ""
            }
        }
    }
}