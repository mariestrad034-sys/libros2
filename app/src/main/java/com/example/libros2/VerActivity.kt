package com.example.libros2

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.libros2.databinding.ActivityVerBinding
import java.io.File

class VerActivity: AppCompatActivity() {
    private lateinit var binding: ActivityVerBinding
    private lateinit var rutaAlmacenamiento: String

    // Función nativa que llamará a C++
    external fun filtrarLibrosPorGenero(ruta: String, generoFiltro: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.loadLibrary("libros2")
        rutaAlmacenamiento = File(filesDir, "biblioteca.txt").absolutePath

        val opcionesFiltro = arrayOf("Todos", "Romance", "Acción", "Drama", "Comedia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesFiltro)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerGeneros.adapter = adapter

        // Detectar cuándo el usuario cambia de opción en el filtro
        binding.spinnerGeneros.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val generoSeleccionado = opcionesFiltro[position]

                // Solicitamos los libros filtrados a C++
                val librosFiltrados = filtrarLibrosPorGenero(rutaAlmacenamiento, generoSeleccionado)

                // Limpiamos el catálogo visual antes de mostrar los nuevos resultados
                binding.containerLibros.removeAllViews()

                // Si C++ responde que no hay libros o está vacío
                if (librosFiltrados.startsWith("No hay libros") || librosFiltrados.trim().isEmpty()) {
                    val tvError = TextView(this@VerActivity).apply {
                        text = librosFiltrados
                        textSize = 16f
                        setTextColor(Color.GRAY)
                        setPadding(0, 20, 0, 0)
                    }
                    binding.containerLibros.addView(tvError)
                    return
                }

                // Convertimos las medidas DP a Píxeles para controlar los tamaños desde código
                val escala = resources.displayMetrics.density
                val anchoImagen = (90 * escala).toInt()
                val altoImagen = (120 * escala).toInt()
                val margen = (12 * escala).toInt()

                // Separamos la cadena de libros que viene desde C++ por el carácter ";"
                val listaLibros = librosFiltrados.split(";")

                for (libroRaw in listaLibros) {
                    if (libroRaw.trim().isEmpty()) continue

                    // Separamos las propiedades de cada libro por el carácter "|"
                    val datos = libroRaw.split("|")
                    if (datos.size >= 5) {
                        val titulo = datos[0]
                        val autor = datos[1]
                        val generos = datos[2]
                        val estado = datos[3]
                        val rutaImg = datos[4].trim()

                        // 1. Creamos la fila horizontal para el libro (Fila = Foto + Textos)
                        val filaLibro = LinearLayout(this@VerActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, margen, 0, margen)
                        }

                        // 2. Creamos el visor de la Imagen de portada
                        val ivPortada = ImageView(this@VerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(anchoImagen, altoImagen).apply {
                                setMargins(0, 0, margen, 0)
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP

                            // Si el libro tiene una imagen válida guardada, la cargamos
                            if (rutaImg != "sin_imagen" && File(rutaImg).exists()) {
                                setImageURI(Uri.fromFile(File(rutaImg)))
                            } else {
                                // Si no tiene, se le pone un fondo gris estético por defecto
                                setBackgroundColor(Color.parseColor("#D3D3D3"))
                            }
                        }

                        // 3. Creamos el bloque de textos (Título, Autor, etc.)
                        val bloqueTexto = LinearLayout(this@VerActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val tvInfo = TextView(this@VerActivity).apply {
                            text = "📌 Título: $titulo\n✍️ Autor: $autor\n🎭 Géneros: $generos\n💡 Estado: $estado"
                            textSize = 15f
                            setLineSpacing(4f, 1f)
                        }
                        bloqueTexto.addView(tvInfo)

                        // 4. Juntamos todo en la fila y lo añadimos al contenedor principal
                        filaLibro.addView(ivPortada)
                        filaLibro.addView(bloqueTexto)
                        binding.containerLibros.addView(filaLibro)

                        // 5. Agregamos una línea divisoria sutil entre libro y libro
                        val lineaDivisoria = View(this@VerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                            setBackgroundColor(Color.parseColor("#E0E0E0"))
                        }
                        binding.containerLibros.addView(lineaDivisoria)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}