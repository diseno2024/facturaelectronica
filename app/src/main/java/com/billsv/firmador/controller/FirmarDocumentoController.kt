package com.billsv.firmador.controller

import com.billsv.firmador.business.CertificadoBusiness
import com.billsv.firmador.business.FirmarDocumentoBusiness
import com.billsv.firmador.constantes.Errores
import com.billsv.firmador.constantes.Errores.errores
import com.billsv.firmador.filter.FirmarDocumentoFilter
import com.billsv.firmador.models.CertificadoMH
import com.billsv.firmador.validations.FirmarDocumentoValidations
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.NoSuchAlgorithmException
import javax.servlet.http.HttpServletResponse

@FirmarDocumentoController.RequestMapping("/firmardocumento")
@FirmarDocumentoController.CrossOrigin(origins = "*", maxAge = 3600)
class FirmarDocumentoController : Controller() {
    annotation class CrossOrigin(val origins: String, val maxAge: Int)
    annotation class RequestMapping(val value: String)

    private val certificadoBusiness: CertificadoBusiness? = null

    private val business: FirmarDocumentoBusiness? = null

    private val validation: FirmarDocumentoValidations? = null

    /**
     *
     * @param filter
     * @return
     * @throws Exception
     */
    fun firmar(filter: FirmarDocumentoFilter, response: HttpServletResponse) {
        var certificado: CertificadoMH? = null
        try {
            validation!!.v5validar(filter)
            if (validation.isValido()) {
                certificado = certificadoBusiness!!.recuperarCertifiado(filter)
                if (certificado != null) {
                    val ow = ObjectMapper().writer().withDefaultPrettyPrinter()
                    val dteString: String
                    try {
                        dteString = ow.writeValueAsString(filter.dteJson)
                        val dteObject = JSONObject(dteString)
                        if (dteObject != null) {
                            logger.info("dteObject != null")
                            val firma: String
                            firma = business!!.firmarJSON(certificado, dteString)
                            response.status = HttpServletResponse.SC_OK
                            return response.writer.println(mensaje?.ok(firma))
                        }
                    } catch (e: JsonProcessingException) {
                        logger.info(errores.COD_810_CONVERTIR_JSON_A_STRING, e.message)
                        response.status = HttpServletResponse.SC_OK
                        return response.writer.println(mensaje?.error(Errores.COD_810_CONVERTIR_JSON_A_STRING))
                    } catch (e: Exception) {
                        logger.info(errores.COD_811_CONVERTIR_STRING_A_JSON, e.message)
                        response.status = HttpServletResponse.SC_OK
                        return response.writer.println(mensaje?.error(Errores.COD_811_CONVERTIR_STRING_A_JSON))
                    }
                } else {
                    response.status = HttpServletResponse.SC_OK
                    return response.writer.println(mensaje?.error(Errores.COD_803_ERROR_LLAVE_PRUBLICA))
                }
            } else {
                response.status = HttpServletResponse.SC_OK
                response.writer.println(validation.requeridos?.let {
                    mensaje?.error(Errores.COD_809_DATOS_REQUERIDOS)
                })
                return
            }
        } catch (e1: IOException) {
            logger.error(e1.message)
            response.status = HttpServletResponse.SC_OK
            return response.writer.println(mensaje?.error(errores.COD_812_NO_FILE, e1.message))
        } catch (e1: NoSuchAlgorithmException) {
            logger.error(e1.message)
            response.status = HttpServletResponse.SC_OK
            return response.writer.println(mensaje?.error(errores.COD_804_ERROR_NO_CATALOGADO, e1.message))
        }
        response.status = HttpServletResponse.SC_OK
        return response.writer.println(mensaje?.error(Errores.COD_804_ERROR_NO_CATALOGADO))
    }


    val status: String
        get() = "Application is running...!!"

    companion object {
        val logger = LoggerFactory.getLogger(FirmarDocumentoController::class.java)
    }
}

