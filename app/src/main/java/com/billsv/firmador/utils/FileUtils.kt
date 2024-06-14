package com.billsv.firmador.utils


import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter

class FileUtils {

    companion object {
        const val MEDIA_TYPE_APPLICATION_KEY = "application/pkcs8"
        const val MEDIA_TYPE_APPLICATION_CRT = "application/x-x509-ca-cert"
    }

    /**
     * Método para crear un archivo genérico
     * @param ruta
     * @param contenido
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun crearArchivo(ruta: String, contenido: String) {
        PrintWriter(File(ruta)).use { archivo ->
            archivo.print(contenido)
        }
    }

    /**
     * Método para leer un archivo
     * @param path
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun leerArchivo(path: String): String {
        val contenido = StringBuilder()
        FileReader(path).use { archivo ->
            BufferedReader(archivo).use { buffer ->
                var linea = buffer.readLine()
                while (linea != null) {
                    contenido.append(linea)
                    linea = buffer.readLine()
                }
            }
        }
        return contenido.toString()
    }
}