package com.example.libros2

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityEliminarBinding
import java.io.File

class EliminarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEliminarBinding
    private lateinit var rutaAlmacenamiento: String

    // Enlazamos la función nativa de C++[cite: 1]
    external fun eliminarLibro(ruta: String, titulo: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEliminarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // 1. Cargar todos los libros al abrir la ventana
        cargarTodosLosLibros()

        binding.btnConfirmarEliminar.setOnClickListener {
            var eliminadosCount = 0

            // 2. Recorrer los checkboxes buscando cuáles se marcaron
            for (i in 0 until binding.llLibrosEliminar.childCount) {
                val view = binding.llLibrosEliminar.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    // Usamos el 'tag' para obtener el título exacto
                    val titulo = view.tag.toString()

                    // Llamamos a tu función en C++ para eliminarlo[cite: 1]
                    eliminarLibro(rutaAlmacenamiento, titulo)
                    eliminadosCount++
                }
            }

            // 3. Resultados
            if (eliminadosCount > 0) {
                binding.tvResultadoEliminar.text = "✅ ¡$eliminadosCount libro(s) eliminado(s) de forma permanente!"
                cargarTodosLosLibros() // Recargar la lista para que desaparezcan los eliminados
            } else {
                binding.tvResultadoEliminar.text = "⚠️ Selecciona al menos un libro para eliminar."
            }
        }
    }

    private fun cargarTodosLosLibros() {
        binding.llLibrosEliminar.removeAllViews() // Limpiar lista
        val file = File(rutaAlmacenamiento)

        if (file.exists()) {
            file.forEachLine { linea ->
                // Tu archivo C++ usa el separador '|'[cite: 1]
                val partes = linea.split("|")

                if (partes.size >= 4) {
                    val titulo = partes[0]
                    val autor = partes[1] // Obtenemos el autor para mostrarlo[cite: 1]

                    val checkBox = CheckBox(this)
                    // Texto descriptivo para el usuario
                    checkBox.text = "$titulo (Por: $autor)"
                    // Guardamos el título exacto en el tag
                    checkBox.tag = titulo
                    checkBox.textSize = 16f
                    checkBox.setTextColor(resources.getColor(R.color.colorTextoPrincipal, theme))

                    binding.llLibrosEliminar.addView(checkBox)
                }
            }

            // Mensaje si la biblioteca está completamente vacía
            if (binding.llLibrosEliminar.childCount == 0) {
                binding.tvResultadoEliminar.text = "La biblioteca está vacía. No hay libros para eliminar."
            } else {
                binding.tvResultadoEliminar.text = ""
            }
        }
    }
}