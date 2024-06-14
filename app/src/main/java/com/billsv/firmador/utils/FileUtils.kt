package com.billsv.firmador.utils

import org.jvnet.hk2.annotations.Service
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter


@Service
class FileUtils {
    /**
     * Metodo para crar una archivo generico
     * @param ruta
     * @param contendio
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun crearArchivo(ruta: String?, contendio: String?) {
        val archivo = PrintWriter(ruta)
        archivo.print(contendio)
        archivo.flush()
        archivo.close()
    }

    /**
     * Metodo para leer un archivo
     * @param path
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun LeerArchivo(path: File): String {
        val contendido = StringBuilder()
        val archivo = FileReader(path.toString())
        BufferedReader(archivo).use { buffer ->
            var linea = buffer.readLine()
            while (linea != null) {
                contendido.append(linea)
                linea = buffer.readLine()
            }
        }
        return contendido.toString()
    }

    companion object {
        var MEDIA_TYPE_APPLICATION_KEY = "application/pkcs8"
        var MEDIA_TYPE_APPLICATION_CRT = "application/x-x509-ca-cert"
    }
}