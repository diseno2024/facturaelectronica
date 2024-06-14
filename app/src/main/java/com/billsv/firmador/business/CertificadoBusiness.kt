package com.billsv.firmador.business

import android.content.Context
import com.billsv.firmador.constantes.Constantes
import com.billsv.firmador.filter.FirmarDocumentoFilter
import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.security.Cryptographic
import com.billsv.firmador.utils.FileUtils
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.jvnet.hk2.annotations.Service
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException

@Service
class CertificadoBusiness {
    private val context: Context
        get() {
            TODO()
        }
    private val cryptographic: Cryptographic? = null
    private val fileUtilis: FileUtils? = null

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun recuperarCertifiado(filter: FirmarDocumentoFilter): CertificadoMH? {
        val xmlMapper = XmlMapper().registerModule(JavaTimeModule())
        var certificado: CertificadoMH? = null
        val crypto = cryptographic!!.encrypt(filter.passwordPri!!, Cryptographic.SHA512)
        val path = File(context.filesDir, "${Constantes.DIRECTORY_UPLOADS}/${filter.nit}.crt")
        val contenido: String = fileUtilis?.LeerArchivo(path) ?: ""
        certificado = xmlMapper.readValue(contenido, CertificadoMH::class.java)
        if (certificado.privateKey!!.clave == crypto) {
            return certificado
        }
        logger.info("Password no valido: " + certificado.nit)
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CertificadoBusiness::class.java)
    }
}

