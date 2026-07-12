package com.example.libros2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.libros2.databinding.ActivityVerBinding
import android.widget.ImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VerActivity: AppCompatActivity() {
    private lateinit var binding: ActivityVerBinding
    private lateinit var rutaAlmacenamiento: String
    private var librosFiltradosRaw: String = ""

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

        // Buscador en tiempo real
        binding.etBuscarTitulo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarInterfazCatalogo(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.spinnerGeneros.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val generoSeleccionado = opcionesFiltro[position]
                librosFiltradosRaw = filtrarLibrosPorGenero(rutaAlmacenamiento, generoSeleccionado)
                actualizarInterfazCatalogo(binding.etBuscarTitulo.text.toString().trim())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarInterfazCatalogo(query: String) {
        binding.containerLibros.removeAllViews()

        if (librosFiltradosRaw.startsWith("No hay libros") || librosFiltradosRaw.trim().isEmpty()) {
            val tvError = TextView(this).apply {
                text = "No hay libros registrados con este género."
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@VerActivity, R.color.colorTextoSecundario))
                setPadding(0, 20, 0, 0)
            }
            binding.containerLibros.addView(tvError)
            return
        }

        val escala = resources.displayMetrics.density
        val anchoImagen = (90 * escala).toInt()
        val altoImagen = (120 * escala).toInt()
        val margen = (12 * escala).toInt()

        val listaLibros = librosFiltradosRaw.split(";")
        var librosMostradosContador = 0

        for (libroRaw in listaLibros) {
            if (libroRaw.trim().isEmpty()) continue

            val datos = libroRaw.split("|")
            // 👉 CAMBIO: Ahora verificamos que haya al menos 7 campos (agregamos sinopsis)
            if (datos.size >= 7) {
                val titulo = datos[0]
                val autor = datos[1]
                val generos = datos[2]
                val estado = datos[3]
                val rutaImg = datos[4].trim()

                // Extraemos la fecha de devolución (sexto parámetro)
                val fechaDevolucion = if (datos.size >= 6) datos[5].trim() else ""

                // 👉 NUEVO: Extraemos la sinopsis (séptimo parámetro)
                val sinopsis = if (datos.size >= 7) datos[6].trim() else ""

                if (query.isNotEmpty() && !titulo.contains(query, ignoreCase = true)) {
                    continue
                }

                librosMostradosContador++

                val filaLibro = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, margen, 0, margen)
                    gravity = Gravity.CENTER_VERTICAL
                }

                val ivPortada = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(anchoImagen, altoImagen).apply {
                        setMargins(0, 0, margen, 0)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    if (rutaImg != "sin_imagen" && File(rutaImg).exists()) {
                        setImageURI(Uri.fromFile(File(rutaImg)))
                    } else {
                        setBackgroundColor(Color.parseColor("#424242"))
                    }

                    // 👉 NUEVO: Listener para abrir la ventana de detalles al tocar la imagen
                    setOnClickListener {
                        val intent = Intent(this@VerActivity, DetalleLibroActivity::class.java).apply {
                            putExtra("titulo", titulo)
                            putExtra("autor", autor)
                            putExtra("sinopsis", sinopsis)
                            putExtra("rutaImagen", rutaImg)
                        }
                        startActivity(intent)
                    }
                }

                val bloqueTexto = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                // Datos básicos del libro (Título, Autor, Género)
                val tvInfo = TextView(this).apply {
                    text = "📌 Título: $titulo\n✍️ Autor: $autor\n🎭 Géneros: $generos"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@VerActivity, R.color.colorTextoPrincipal))
                    setLineSpacing(4f, 1f)
                }
                bloqueTexto.addView(tvInfo)

                // --- LÓGICA INTELIGENTE DE FECHAS Y COLORES ---
                var estadoTexto = "💡 Estado: $estado"
                var colorEstado = ContextCompat.getColor(this@VerActivity, R.color.colorTextoSecundario)
                var esVencido = false

                if (estado.contains("Prestado", ignoreCase = true) && fechaDevolucion.isNotEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaLimite = sdf.parse(fechaDevolucion)
                        val fechaHoy = Date() // Captura la fecha real del teléfono

                        if (fechaHoy.after(fechaLimite)) {
                            estadoTexto = "⚠️ VENCIDO (Debió devolverse: $fechaDevolucion)\n👤 $estado"
                            colorEstado = Color.parseColor("#FF1744") // Rojo brillante de alerta
                            esVencido = true
                        } else {
                            estadoTexto = "📅 Devolver el: $fechaDevolucion\n👤 $estado"
                            colorEstado = Color.parseColor("#FF9100") // Naranja estético de préstamo activo
                        }
                    } catch (e: Exception) {
                        estadoTexto = "💡 Estado: $estado\n📅 Vence: $fechaDevolucion"
                    }
                } else if (estado.contains("Disponible", ignoreCase = true)) {
                    colorEstado = Color.parseColor("#00E676") // Verde brillante para disponible
                }

                // Creamos un TextView exclusivo para el estado para que resalte mucho más
                val tvEstado = TextView(this).apply {
                    text = estadoTexto
                    textSize = 13f
                    setTextColor(colorEstado)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 4, 0, 0)
                }
                bloqueTexto.addView(tvEstado)
                // ----------------------------------------------

                val btnCompartir = Button(this).apply {
                    text = "Rentar\n📢"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#009688"))
                    setPadding(10, 5, 10, 5)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(margen, 0, 0, 0)
                    }

                    setOnClickListener {
                        // Desescapar la sinopsis para mostrarla correctamente
                        val sinopsisFormateada = sinopsis.replace("[ENTER]", "\n")

                        // Construir el mensaje base
                        var mensajePublicidad = "¡Hola! Te recomiendo este increíble libro de mi colección física disponible para renta: \n\n" +
                                "📚 *Título:* $titulo\n" +
                                "✍️ *Autor:* $autor\n" +
                                "🎭 *Géneros:* $generos\n" +
                                "✨ *Disponibilidad:* $estado\n"

                        // 👉 NUEVO: Si hay sinopsis, la agregamos al mensaje
                        if (sinopsisFormateada.trim().isNotEmpty()) {
                            mensajePublicidad += "\n📖 *Sinopsis:*\n$sinopsisFormateada\n"
                        }

                        mensajePublicidad += "\n¡Escríbeme al privado si te interesa leerlo para coordinar el alquiler! 📖"

                        val intentCompartir = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, mensajePublicidad)
                        }
                        context.startActivity(Intent.createChooser(intentCompartir, "Promocionar libro en:"))
                    }
                }

                filaLibro.addView(ivPortada)
                filaLibro.addView(bloqueTexto)
                filaLibro.addView(btnCompartir)
                binding.containerLibros.addView(filaLibro)

                val lineaDivisoria = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    setBackgroundColor(Color.parseColor("#2C2C2C"))
                }
                binding.containerLibros.addView(lineaDivisoria)
            }
        }

        if (librosMostradosContador == 0 && query.isNotEmpty()) {
            val tvSinResultados = TextView(this).apply {
                text = "🔍 No se encontraron libros que coincidan con \"$query\""
                textSize = 15f
                setTextColor(ContextCompat.getColor(this@VerActivity, R.color.colorTextoSecundario))
                setPadding(0, 40, 0, 0)
                gravity = Gravity.CENTER
            }
            binding.containerLibros.addView(tvSinResultados)
        }
    }
}