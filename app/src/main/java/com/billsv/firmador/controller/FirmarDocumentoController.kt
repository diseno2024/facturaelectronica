package com.billsv.firmador.controller

import com.billsv.firmador.business.CertificadoBusiness
import com.billsv.firmador.business.FirmarDocumentoBusiness
import com.billsv.firmador.filter.FirmarDocumentoFilter
import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.constantes.Errores
import com.billsv.firmador.utils.Mensaje
import com.billsv.firmador.validations.FirmarDocumentoValidations
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.NoSuchAlgorithmException
import javax.servlet.http.HttpServletResponse

class FirmarDocumentoController {
    private lateinit var mensaje: Mensaje
    private val logger: Logger = LoggerFactory.getLogger(FirmarDocumentoController::class.java)
    private lateinit var certificadoBusiness: CertificadoBusiness
    private lateinit var business: FirmarDocumentoBusiness
    private lateinit var validation: FirmarDocumentoValidations

    fun firmar(filter: FirmarDocumentoFilter, response: HttpServletResponse) {
        mensaje = Mensaje()
        var certificado: CertificadoMH? = null
        try {
            validation.v5validar(filter)
            if (validation.isValid()) {
                certificado = certificadoBusiness.recuperarCertifiado(filter)
                if (certificado != null) {
                    val ow = ObjectMapper().writer().withDefaultPrettyPrinter()
                    val dteString = ow.writeValueAsString(filter.dteJson)
                    val dteObject = JSONObject(dteString)
                    if (dteObject != null) {
                        logger.info("dteObject != null")
                        val firma = business.firmarContenidoJSON(certificado, dteString)
                        response.status = HttpServletResponse.SC_OK
                        response.writer.println(mensaje.ok(firma))
                        return
                    }
                } else {
                    response.status = HttpServletResponse.SC_OK
                    response.writer.println(mensaje.error(Errores.COD_803_ERROR_LLAVE_PUBLICA))
                    return
                }
            } else {
                response.status = HttpServletResponse.SC_OK
                response.writer.println(validation.Requeridos?.let {
                    mensaje.error(Errores.COD_809_DATOS_REQUERIDOS,
                        it
                    )
                })
                return
            }
        } catch (e1: IOException) {
            logger.error(e1.message)
            response.status = HttpServletResponse.SC_OK
            response.writer.println(e1.message?.let { mensaje.error(Errores.COD_812_NO_FILE, it) })
            return
        } catch (e1: NoSuchAlgorithmException) {
            logger.error(e1.message)
            response.status = HttpServletResponse.SC_OK
            response.writer.println(e1.message?.let { mensaje.error(Errores.COD_804_ERROR_NO_CATALOGADO, it) })
            return
        }
        response.status = HttpServletResponse.SC_OK
        response.writer.println(mensaje.error(Errores.COD_804_ERROR_NO_CATALOGADO))
    }

    fun getStatus(): String {
        return "Application is running...!!"
    }
}