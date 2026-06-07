cmake_minimum_required(VERSION 3.22.1)

# El nombre de tu proyecto (y de tu librería nativa)
project("libros2")

# Define la librería nativa apuntando a tu archivo de C++ (native-lib.cpp)
add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        native-lib.cpp
)

# Busca la librería de logs de Android para poder depurar si es necesario
find_library(
        log-lib
        log
)

# Vincula las librerías del sistema con tu código nativo
target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        ${log-lib}
)