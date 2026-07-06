#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>

struct Libro {
    std::string titulo;
    std::string autor;
    std::string persona;
    bool prestado;
    std::string generos;
    std::string imagen; // Campo nuevo para guardar la ruta de la portada
};

// --- Funciones base de persistencia mejoradas con separador único '|' ---

void guardarEnArchivo(const std::string& ruta, const std::vector<Libro>& biblioteca) {
    std::ofstream archivo(ruta, std::ios::trunc);
    if (archivo.is_open()) {
        for (const auto& l : biblioteca) {
            // Guardamos todos los campos separados de forma segura por '|'
            archivo << l.titulo << "|" << l.autor << "|" << l.persona << "|"
                    << (l.prestado ? "1" : "0") << "|" << l.generos << "|"
                    << l.imagen << "\n";
        }
        archivo.close();
    }
}

std::vector<Libro> cargarDesdeArchivo(const std::string& ruta) {
    std::vector<Libro> biblioteca;
    std::ifstream archivo(ruta);
    if (archivo.is_open()) {
        std::string linea;
        while (std::getline(archivo, linea)) {
            if (linea.empty()) continue;
            std::stringstream ss(linea);
            std::string titulo, autor, persona, prestadoStr, generos, imagen;

            // Leemos los 6 campos estructurados
            if (std::getline(ss, titulo, '|') &&
                std::getline(ss, autor, '|') &&
                std::getline(ss, persona, '|') &&
                std::getline(ss, prestadoStr, '|') &&
                std::getline(ss, generos, '|') &&
                std::getline(ss, imagen)) {

                biblioteca.push_back({titulo, autor, persona, prestadoStr == "1", generos, imagen});
            }
        }
        archivo.close();
    }
    return biblioteca;
}

// 1. REGISTRAR LIBRO (Soporta el parámetro de la imagen)
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_RegistrarActivity_registrarLibro(
        JNIEnv* env, jobject thiz, jstring ruta, jstring titulo,
        jstring autor, jstring persona, jboolean prestado, jstring generos, jstring imagen) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cAutor = env->GetStringUTFChars(autor, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);
    const char* cGeneros = env->GetStringUTFChars(generos, nullptr);
    const char* cImagen = env->GetStringUTFChars(imagen, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    biblioteca.push_back({cTitulo, cAutor, cPersona, prestado == JNI_TRUE, cGeneros, cImagen});

    guardarEnArchivo(cRuta, biblioteca);

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(autor, cAutor);
    env->ReleaseStringUTFChars(persona, cPersona);
    env->ReleaseStringUTFChars(generos, cGeneros);
    env->ReleaseStringUTFChars(imagen, cImagen);

    return env->NewStringUTF("Libro guardado con éxito.");
}

// 2. FILTRAR Y MOSTRAR LIBROS (Estructurado con '|' y ';' para Kotlin)
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
            // Evaluamos el estado de disponibilidad[cite: 1]
            std::string estado = l.prestado ? "Prestado a: " + l.persona : "Disponible en estante";

            // Construimos la cadena compacta: Titulo|Autor|Generos|Estado|RutaImagen;[cite: 1]
            resultado += l.titulo + "|" + l.autor + "|" + l.generos + "|" + estado + "|" + l.imagen + ";";
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

// 3. ELIMINAR LIBRO (Mantiene tus mensajes originales)
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

// 4. DEVOLVER LIBRO (Mantiene tus validaciones y mensajes originales)
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
            }
            break;
        }
    }

    std::string mensaje;
    if (!encontrado) {
        mensaje = "X No se encontró ningún libro con el titulo \"" + objetivo + "\".";
    } else if (yaEstabaDisponible) {
        mensaje = " El libro \"" + objetivo + "\" ya se encontraba disponible en el estante.";
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
    std::string dataEstadisticas = std::to_string(total) + "-" + std::to_string(disponibles) + "-" + std::to_string(prestados);

    env->ReleaseStringUTFChars(ruta, cRuta);
    return env->NewStringUTF(dataEstadisticas.c_str());
}

// 6. PRESTAR LIBRO (Mantiene tus validaciones y mensajes originales)
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_PrestarActivity_prestarLibro(JNIEnv* env, jobject thiz, jstring ruta, jstring titulo, jstring persona) {
    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);
    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    std::string objetivo(cTitulo);
    std::string usuario(cPersona);
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
            }
            break;
        }
    }

    std::string mensaje;
    if (!encontrado) {
        mensaje = "X No se encontró ningún libro con el titulo \"" + objetivo + "\".";
    } else if (yaPrestado) {
        mensaje = " El libro \"" + objetivo + "\" ya está prestado actualmente.";
    } else {
        guardarEnArchivo(cRuta, biblioteca);
        mensaje = "¡Éxito! El libro \"" + objetivo + "\" ha sido prestado a " + usuario + ".";
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(persona, cPersona);
    return env->NewStringUTF(mensaje.c_str());
}