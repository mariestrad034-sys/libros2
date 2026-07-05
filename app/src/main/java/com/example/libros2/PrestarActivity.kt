package com.example.libros2

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityPrestarBinding
import java.io.File

class PrestarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestarBinding
    private lateinit var rutaAlmacenamiento: String

    // Enlazamos la sexta función nativa de C++
    external fun prestarLibro(ruta: String, titulo: String, persona: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrestarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        // 1. Cargar la lista al abrir la ventana
        cargarLibrosDisponibles()

        binding.btnConfirmarPrestar.setOnClickListener {
            val persona = binding.etPrestarPersona.text.toString().trim()

            if (persona.isEmpty()) {
                binding.tvResultadoPrestar.text = "⚠️ Por favor, ingresa el nombre de la persona."
                return@setOnClickListener
            }

            // 2. Recorremos el contenedor buscando qué CheckBoxes se marcaron
            var prestadosCount = 0
            for (i in 0 until binding.llLibrosDisponibles.childCount) {
                val view = binding.llLibrosDisponibles.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    val titulo = view.text.toString()
                    // Llamamos a tu función en C++ por cada libro seleccionado[cite: 1]
                    prestarLibro(rutaAlmacenamiento, titulo, persona)
                    prestadosCount++
                }
            }

            // 3. Resultado final y limpieza
            if (prestadosCount > 0) {
                binding.tvResultadoPrestar.text = "✅ ¡$prestadosCount libro(s) prestado(s) exitosamente!"
                binding.etPrestarPersona.text.clear()
                cargarLibrosDisponibles() // Recargamos para que desaparezcan los que ya se prestaron
            } else {
                binding.tvResultadoPrestar.text = "⚠️ Selecciona al menos un libro."
            }
        }
    }

    private fun cargarLibrosDisponibles() {
        binding.llLibrosDisponibles.removeAllViews() // Limpiamos la vista anterior
        val file = File(rutaAlmacenamiento)

        if (file.exists()) {
            file.forEachLine { linea ->
                // Tu C++ usa el separador '|' para guardar y leer los datos[cite: 1]
                val partes = linea.split("|")

                // Aseguramos que la línea tenga el formato correcto antes de leer
                if (partes.size >= 4) {
                    val titulo = partes[0]
                    val prestado = partes[3] // "0" significa que NO está prestado[cite: 1]

                    if (prestado == "0") {
                        val checkBox = CheckBox(this)
                        checkBox.text = titulo
                        checkBox.textSize = 16f
                        // Adaptamos el color para que se vea bien en Modo Oscuro o Claro
                        checkBox.setTextColor(resources.getColor(R.color.colorTextoPrincipal, theme))
                        binding.llLibrosDisponibles.addView(checkBox)
                    }
                }
            }
        }
    }
}