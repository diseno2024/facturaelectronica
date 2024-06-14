package com.billsv.firmador.business

import android.content.Context
import com.billsv.firmador.constantes.Constantes
import com.billsv.firmador.filter.FirmarDocumentoFilter
import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.security.Cryptographic
import com.billsv.firmador.utils.FileUtils
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException

class CertificadoBusiness(
    private val context: Context,
    private val cryptographic: Cryptographic,
    private val fileUtils: FileUtils
) {

    private val logger = LoggerFactory.getLogger(CertificadoBusiness::class.java)

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun recuperarCertifiado(filter: FirmarDocumentoFilter): CertificadoMH? {
        val xmlMapper = XmlMapper().apply {
            registerModule(JavaTimeModule())
        }
        val nonNullablePasswordPri: String = filter.passwordPri ?: "valorPredeterminado"
        val crypto = cryptographic.encrypt(nonNullablePasswordPri, Cryptographic.SHA512)

        val path = File(context.filesDir, "${Constantes.DIRECTORY_UPLOADS}/${filter.nit}.crt")
        val contenido = fileUtils.leerArchivo(path.absolutePath)
        val certificado = xmlMapper.readValue(contenido, CertificadoMH::class.java)

        return if (certificado.privateKey?.clave == crypto) {
            certificado
        } else {
            logger.info("Password no valido: ${certificado.nit}")
            null
        }
    }
}