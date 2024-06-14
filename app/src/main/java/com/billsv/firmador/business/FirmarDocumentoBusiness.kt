package com.billsv.firmador.business

import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.security.KeyGenerator
import com.billsv.firmador.utils.FileUtils
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jvnet.hk2.annotations.Service
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.file.Path
import java.security.PrivateKey

@Service
class FirmarDocumentoBusiness {
    private val fileUtils: FileUtils? = null

    private val keyGenerator: KeyGenerator? = null

    /**
     * Método para crear un JSON Web Signing (JWS).
     * @param certificado
     * @param ruta
     * @throws Exception
     */
    @Throws(Exception::class)
    fun firmarJSON(certificado: CertificadoMH, ruta: Path) {
        val contenido: String = readFileContent(ruta)
        val jws = JsonWebSignature()
        jws.payload = contenido
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA512
        val key: PrivateKey? = keyGenerator?.ByteToPrivateKey(certificado.privateKey!!.encodied)
        jws.key = key
        fileUtils?.crearArchivo(ruta.toString(), jws.compactSerialization)
    }

    /**
     * Método para crear un JSON Web Signing (JWS).
     * @param certificado
     * @param contenido, DTE que se quiere firmar
     * @throws Exception
     */
    @Throws(Exception::class)
    fun firmarJSON(certificado: CertificadoMH, contenido: String?): String {
        val jws = JsonWebSignature()
        jws.payload = contenido
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA512
        val key: PrivateKey? = keyGenerator?.ByteToPrivateKey(certificado.privateKey!!.encodied)
        jws.key = key
        return jws.compactSerialization
    }

    /**
     * Helper method to read file content as a String.
     * @param path
     * @return file content as String
     * @throws IOException
     */
    @Throws(Exception::class)
    private fun readFileContent(path: Path): String {
        val file = File(path.toString()) // Convert Path to File without using toFile()
        val writer = StringWriter()
        val buffer = CharArray(1024)
        BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).use { reader ->
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }
        return writer.toString()
    }

    companion object {
        val logger = LoggerFactory.getLogger(FirmarDocumentoBusiness::class.java)
    }
}
