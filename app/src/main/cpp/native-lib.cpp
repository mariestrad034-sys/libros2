#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <algorithm>

struct Libro {
    std::string titulo;
    std::string autor;
    std::string persona;
    bool prestado;
    std::string generos;
    std::string imagen;
    std::string fechaDevolucion;
    std::string sinopsis;
    std::string monto;
};

// Función auxiliar: Reemplaza saltos de línea en la sinopsis
std::string escaparSinopsis(const std::string& sinopsis) {
    std::string resultado = sinopsis;
    size_t pos = 0;
    while ((pos = resultado.find('\n', pos)) != std::string::npos) {
        resultado.replace(pos, 1, "[ENTER]");
        pos += 7;
    }
    return resultado;
}

// Función auxiliar: Recupera saltos de línea en la sinopsis
std::string desescaparSinopsis(const std::string& sinopsis) {
    std::string resultado = sinopsis;
    size_t pos = 0;
    while ((pos = resultado.find("[ENTER]", pos)) != std::string::npos) {
        resultado.replace(pos, 7, "\n");
        pos += 1;
    }
    return resultado;
}

// Función interna: Guarda en disco
void guardarEnArchivo(const std::string& ruta, const std::vector<Libro>& biblioteca) {
    std::ofstream archivo(ruta, std::ios::trunc);
    if (archivo.is_open()) {
        for (const auto& l : biblioteca) {
            archivo << l.titulo << "|"
                    << l.autor << "|"
                    << l.persona << "|"
                    << (l.prestado ? "1" : "0") << "|"
                    << l.generos << "|"
                    << l.imagen << "|"
                    << l.fechaDevolucion << "|"
                    << escaparSinopsis(l.sinopsis) << "|"
                    << l.monto << "\n";
        }
        archivo.close();
    }
}

// Función interna: Carga desde disco
std::vector<Libro> cargarDesdeArchivo(const std::string& ruta) {
    std::vector<Libro> biblioteca;
    std::ifstream archivo(ruta);

    if (archivo.is_open()) {
        std::string linea;
        while (std::getline(archivo, linea)) {
            if (linea.empty()) continue;

            std::stringstream ss(linea);
            std::string titulo, autor, persona, prestadoStr, generos, imagen, fechaDevolucion, sinopsis, monto;

            if (std::getline(ss, titulo, '|') &&
                std::getline(ss, autor, '|') &&
                std::getline(ss, persona, '|') &&
                std::getline(ss, prestadoStr, '|') &&
                std::getline(ss, generos, '|') &&
                std::getline(ss, imagen, '|') &&
                std::getline(ss, fechaDevolucion, '|') &&
                std::getline(ss, sinopsis, '|')) {

                std::getline(ss, monto);

                biblioteca.push_back({
                                             titulo,
                                             autor,
                                             persona,
                                             prestadoStr == "1",
                                             generos,
                                             imagen,
                                             fechaDevolucion,
                                             desescaparSinopsis(sinopsis),
                                             monto
                                     });
            }
        }
        archivo.close();
    }
    return biblioteca;
}

// 1. REGISTRAR LIBRO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_RegistrarActivity_registrarLibro(
        JNIEnv* env, jobject thiz, jstring ruta, jstring titulo, jstring autor,
        jstring persona, jboolean prestado, jstring generos, jstring imagen,
        jstring fecha_devolucion, jstring sinopsis, jstring monto) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cAutor = env->GetStringUTFChars(autor, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);
    const char* cGeneros = env->GetStringUTFChars(generos, nullptr);
    const char* cImagen = env->GetStringUTFChars(imagen, nullptr);
    const char* cFecha = env->GetStringUTFChars(fecha_devolucion, nullptr);
    const char* cSinopsis = env->GetStringUTFChars(sinopsis, nullptr);
    const char* cMonto = env->GetStringUTFChars(monto, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);

    biblioteca.push_back({
                                 cTitulo,
                                 cAutor,
                                 cPersona,
                                 prestado == JNI_TRUE,
                                 cGeneros,
                                 cImagen,
                                 cFecha,
                                 cSinopsis,
                                 cMonto
                         });

    guardarEnArchivo(cRuta, biblioteca);

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(autor, cAutor);
    env->ReleaseStringUTFChars(persona, cPersona);
    env->ReleaseStringUTFChars(generos, cGeneros);
    env->ReleaseStringUTFChars(imagen, cImagen);
    env->ReleaseStringUTFChars(fecha_devolucion, cFecha);
    env->ReleaseStringUTFChars(sinopsis, cSinopsis);
    env->ReleaseStringUTFChars(monto, cMonto);

    return env->NewStringUTF("Libro guardado con éxito.");
}

// 2. FILTRAR Y MOSTRAR LIBROS
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_VerActivity_filtrarLibrosPorGenero(JNIEnv* env, jobject thiz, jstring ruta, jstring genero_filtro) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cFiltro = env->GetStringUTFChars(genero_filtro, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string filtro(cFiltro);
    std::string resultado = "";
    int contador = 0;

    for (const auto& l : biblioteca) {
        if (filtro == "Todos" || l.generos.find(filtro) != std::string::npos) {
            std::string estado = l.prestado ? "Prestado a: " + l.persona : "Disponible en estante";
            resultado += l.titulo + "|" + l.autor + "|" + l.generos + "|" + estado + "|"
                         + l.imagen + "|" + l.fechaDevolucion + "|" + escaparSinopsis(l.sinopsis) + "|" + l.monto + ";";
            contador++;
        }
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(genero_filtro, cFiltro);

    if (contador == 0) {
        return env->NewStringUTF("No hay libros registrados con este género.");
    }

    return env->NewStringUTF(resultado.c_str());
}

// 3. ELIMINAR LIBRO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_EliminarActivity_eliminarLibro(JNIEnv* env, jobject thiz, jstring ruta, jstring titulo) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string objetivo(cTitulo);
    bool encontrado = false;

    for (auto it = biblioteca.begin(); it != biblioteca.end(); ) {
        if (it->titulo == objetivo) {
            it = biblioteca.erase(it);
            encontrado = true;
        } else {
            ++it;
        }
    }

    if (encontrado) guardarEnArchivo(cRuta, biblioteca);

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);

    return env->NewStringUTF(encontrado ? "Libro eliminado." : "No se encontró el libro.");
}

// 4. DEVOLVER LIBRO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_DevolverActivity_devolverLibro(JNIEnv* env, jobject thiz, jstring ruta, jstring titulo) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string objetivo(cTitulo);
    bool encontrado = false;
    bool yaEstabaDisponible = false;

    for (auto& l : biblioteca) {
        if (l.titulo == objetivo) {
            encontrado = true;
            if (!l.prestado) {
                yaEstabaDisponible = true;
            } else {
                l.prestado = false;
                l.persona = "Ninguno";
                l.fechaDevolucion = "";
            }
            break;
        }
    }

    std::string mensaje;
    if (!encontrado) {
        mensaje = "❌ No se encontró ningún libro con el título \"" + objetivo + "\".";
    } else if (yaEstabaDisponible) {
        mensaje = "El libro \"" + objetivo + "\" ya se encontraba disponible en el estante.";
    } else {
        guardarEnArchivo(cRuta, biblioteca);
        mensaje = "¡Éxito! El libro \"" + objetivo + "\" ha sido devuelto y ya está disponible.";
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);

    return env->NewStringUTF(mensaje.c_str());
}

// 5. OBTENER ESTADÍSTICAS
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_MainActivity_obtenerEstadisticas(JNIEnv* env, jobject thiz, jstring ruta) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    int total = biblioteca.size();
    int prestados = 0;

    for (const auto& l : biblioteca) {
        if (l.prestado) prestados++;
    }

    int disponibles = total - prestados;
    std::string dataEstadisticas = std::to_string(total) + "-" +
                                   std::to_string(disponibles) + "-" +
                                   std::to_string(prestados);

    env->ReleaseStringUTFChars(ruta, cRuta);

    return env->NewStringUTF(dataEstadisticas.c_str());
}

// 6. PRESTAR LIBRO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_PrestarActivity_prestarLibro(
        JNIEnv* env, jobject thiz, jstring ruta, jstring titulo,
        jstring persona, jstring fecha_devolucion, jstring monto) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);
    const char* cFecha = env->GetStringUTFChars(fecha_devolucion, nullptr);
    const char* cMonto = env->GetStringUTFChars(monto, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string objetivo(cTitulo);
    std::string usuario(cPersona);
    std::string fechaVencimiento(cFecha);
    std::string montoValue(cMonto);

    bool encontrado = false;
    bool yaPrestado = false;

    for (auto& l : biblioteca) {
        if (l.titulo == objetivo) {
            encontrado = true;
            if (l.prestado) {
                yaPrestado = true;
            } else {
                l.prestado = true;
                l.persona = usuario;
                l.fechaDevolucion = fechaVencimiento;
                l.monto = montoValue;
            }
            break;
        }
    }

    std::string mensaje;
    if (!encontrado) {
        mensaje = "❌ No se encontró ningún libro con el título \"" + objetivo + "\".";
    } else if (yaPrestado) {
        mensaje = "El libro \"" + objetivo + "\" ya está prestado actualmente.";
    } else {
        guardarEnArchivo(cRuta, biblioteca);
        mensaje = "¡Éxito! El libro \"" + objetivo + "\" ha sido prestado a " + usuario + ".";
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(persona, cPersona);
    env->ReleaseStringUTFChars(fecha_devolucion, cFecha);
    env->ReleaseStringUTFChars(monto, cMonto);

    return env->NewStringUTF(mensaje.c_str());
}

// 7. OBTENER PRÉSTAMOS PENDIENTES DE PAGO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_HistorialPagosActivity_obtenerPrestamosPendientes(JNIEnv* env, jobject thiz, jstring ruta) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string resultado = "";
    int contador = 0;

    for (const auto& l : biblioteca) {
        if (l.prestado && !l.monto.empty() && l.monto != "0") {
            resultado += l.persona + "|" + l.titulo + "|" + l.monto + "|no_pagado;";
            contador++;
        }
    }

    env->ReleaseStringUTFChars(ruta, cRuta);

    if (contador == 0) {
        return env->NewStringUTF("No hay préstamos pendientes de pago.");
    }

    return env->NewStringUTF(resultado.c_str());
}

// 8. MARCAR PRÉSTAMO COMO PAGADO
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_HistorialPagosActivity_marcarPagado(JNIEnv* env, jobject thiz, jstring ruta, jstring titulo, jstring persona) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string objetivo(cTitulo);
    std::string personaObj(cPersona);
    bool encontrado = false;

    for (auto& l : biblioteca) {
        if (l.titulo == objetivo && l.persona == personaObj && l.prestado) {
            l.prestado = false;
            l.persona = "Ninguno";
            l.fechaDevolucion = "";
            l.monto = "0";
            encontrado = true;
            break;
        }
    }

    if (encontrado) {
        guardarEnArchivo(cRuta, biblioteca);
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(persona, cPersona);

    return env->NewStringUTF(encontrado ? "✅ Pago registrado correctamente." : "❌ No se pudo registrar el pago.");
}