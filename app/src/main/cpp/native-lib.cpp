#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>

// Estructura actualizada con la propiedad de géneros
struct Libro {
    std::string titulo;
    std::string autor;
    std::string persona;
    bool prestado;
    std::string generos; // <- Nuevo campo
};

// Guardar en archivo (se añade el campo separado por un nuevo '|')
void guardarEnArchivo(const std::string& ruta, const std::vector<Libro>& biblioteca) {
    std::ofstream archivo(ruta, std::ios::trunc);
    if (archivo.is_open()) {
        for (const auto& l : biblioteca) {
            archivo << l.titulo << "|" << l.autor << "|" << l.persona << "|" << (l.prestado ? "1" : "0") << "|" << l.generos << "\n";
        }
        archivo.close();
    }
}

// Cargar desde archivo reconstruyendo la nueva estructura
std::vector<Libro> cargarDesdeArchivo(const std::string& ruta) {
    std::vector<Libro> biblioteca;
    std::ifstream archivo(ruta);
    if (archivo.is_open()) {
        std::string linea;
        while (std::getline(archivo, linea)) {
            if (linea.empty()) continue;

            std::stringstream ss(linea);
            std::string titulo, autor, persona, prestadoStr, generos;

            if (std::getline(ss, titulo, '|') &&
                std::getline(ss, autor, '|') &&
                std::getline(ss, persona, '|') &&
                std::getline(ss, prestadoStr, '|') &&
                std::getline(ss, generos, '|')) { // <- Leemos los géneros

                Libro l;
                l.titulo = titulo;
                l.autor = autor;
                l.persona = persona;
                l.prestado = (prestadoStr == "1");
                l.generos = generos;
                biblioteca.push_back(l);
            }
        }
        archivo.close();
    }
    return biblioteca;
}

// 1. REGISTRAR LIBRO ACTUALIZADO (Recibe el parámetro generos)
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_RegistrarActivity_registrarLibro(
        JNIEnv* env, jobject thiz, jstring ruta, jstring titulo, jstring autor, jstring persona, jboolean prestado, jstring generos) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    const char* cTitulo = env->GetStringUTFChars(titulo, nullptr);
    const char* cAutor = env->GetStringUTFChars(autor, nullptr);
    const char* cPersona = env->GetStringUTFChars(persona, nullptr);
    const char* cGeneros = env->GetStringUTFChars(generos, nullptr);

    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);

    // Creamos el libro incluyendo los géneros
    Libro nuevoLibro{cTitulo, cAutor, cPersona, prestado == JNI_TRUE, cGeneros};
    biblioteca.push_back(nuevoLibro);

    guardarEnArchivo(cRuta, biblioteca);

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);
    env->ReleaseStringUTFChars(autor, cAutor);
    env->ReleaseStringUTFChars(persona, cPersona);
    env->ReleaseStringUTFChars(generos, cGeneros);

    return env->NewStringUTF("Libro guardado con géneros con éxito.");
}

// 2. MOSTRAR LIBROS ACTUALIZADO (Muestra los géneros en la lista)
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libros2_VerActivity_mostrarLibros(JNIEnv* env, jobject thiz, jstring ruta) {

    const char* cRuta = env->GetStringUTFChars(ruta, nullptr);
    std::vector<Libro> biblioteca = cargarDesdeArchivo(cRuta);
    env->ReleaseStringUTFChars(ruta, cRuta);

    if (biblioteca.empty()) {
        return env->NewStringUTF("No hay libros registrados en el almacenamiento.");
    }

    std::string resultado;
    for (const auto& l : biblioteca) {
        resultado += "📖 Título: " + l.titulo + "\n";
        resultado += "   Autor: " + l.autor + "\n";
        resultado += "   🎭 Género(s): " + l.generos + "\n"; // <- Aquí se imprime en tu pantalla de "Ver Libros"
        if (l.prestado) {
            resultado += "   ⚠️ Prestado a: " + l.persona + "\n";
        } else {
            resultado += "   ✅ Disponible en estante\n";
        }
        resultado += "-----------------------------------\n";
    }

    return env->NewStringUTF(resultado.c_str());
}

// 3. ELIMINAR LIBRO (Se queda igual pero adaptado a la nueva estructura interna)
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

    std::string mensaje;
    if (encontrado) {
        guardarEnArchivo(cRuta, biblioteca);
        mensaje = "El libro \"" + objetivo + "\" ha sido eliminado correctamente.";
    } else {
        mensaje = "No se encontró ningún libro con el título \"" + objetivo + "\".";
    }

    env->ReleaseStringUTFChars(ruta, cRuta);
    env->ReleaseStringUTFChars(titulo, cTitulo);

    return env->NewStringUTF(mensaje.c_str());
}