package com.billsv.firmador.business

import android.content.Context
import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.security.KeyGenerator
import com.billsv.firmador.utils.FileUtils
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.PrivateKey

class FirmarDocumentoBusiness(
    private val context: Context,
    private val fileUtils: FileUtils,
    private val keyGenerator: KeyGenerator
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FirmarDocumentoBusiness::class.java)
    }

    /**
     * Método para crear un JSON Web Signing (JWS) a partir de un archivo.
     * @param certificado
     * @param ruta
     * @throws Exception
     */
    @Throws(Exception::class)
    fun firmarArchivoJSON(certificado: CertificadoMH, ruta: String) {
        val contenido = fileUtils.leerArchivo(ruta)
        val jws = JsonWebSignature().apply {
            payload = contenido
            algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA512
        }
        val encodedKey = certificado.privateKey?.encodied
            ?: throw IllegalArgumentException("Encoded private key is null")
        val key: PrivateKey = keyGenerator.byteToPrivateKey(encodedKey)
            ?: throw IllegalArgumentException("Failed to generate private key")
        jws.key = key
        fileUtils.crearArchivo(ruta, jws.compactSerialization)
    }

    /**
     * Método para crear un JSON Web Signing (JWS) a partir de un contenido de texto.
     * @param certificado
     * @param contenido, DTE que se quiere firmar
     * @throws Exception
     */
    @Throws(Exception::class)
    fun firmarContenidoJSON(certificado: CertificadoMH, contenido: String): String {
        val jws = JsonWebSignature().apply {
            payload = contenido
            algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA512
        }
        val encodedKey = certificado.privateKey?.encodied
            ?: throw IllegalArgumentException("Encoded private key is null")
        val key: PrivateKey = keyGenerator.byteToPrivateKey(encodedKey)
            ?: throw IllegalArgumentException("Failed to generate private key")
        jws.key = key
        return jws.compactSerialization
    }
}
