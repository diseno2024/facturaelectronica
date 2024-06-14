package com.billsv.firmador.validations

import com.billsv.firmador.filter.FirmarDocumentoFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FirmarDocumentoValidations : AbstractValidations() {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FirmarDocumentoValidations::class.java)
    }

    fun v2validar(filter: FirmarDocumentoFilter): MutableList<String>? {
        Requeridos = validarNIT(filter.nit)
        if (filter.compactSerialization == null || filter.compactSerialization!!.length <= 0) {
            Requeridos?.add(REQ_JWS)
        }
        return Requeridos
    }

    fun v1validar(filter: FirmarDocumentoFilter): MutableList<String>? {
        Requeridos = validarNIT(filter.nit)
        if (filter.nombreDocumento == null || filter.nombreDocumento!!.length <= 0) {
            Requeridos?.add(REQ_NOMBRE_DOCUMENTO)
        }
        if (filter.nombreFirma == null || filter.nombreFirma!!.length <= 0) {
            Requeridos?.add(REQ_NOMBRE_FIRMA)
        }

        logger.info("requeridos: $Requeridos")
        return Requeridos
    }

    fun v3validar(filter: FirmarDocumentoFilter): MutableList<String>? {
        Requeridos = validarNIT(filter.nit)
        if (validarNIT(filter.nit) != null) {
            Requeridos?.add(validarNIT(filter.nit)!!.toString())
        }
        //if(filter.dteJson == null || filter.dteJson.length() <= 0) {
        //    requeridos.add(REQ_JSON_DTE)
        //}
        if (filter.passwordPri == null || filter.passwordPri!!.length <= 0) {
            Requeridos?.add(REQ_CLAVE_PRIVADA)
        }
        return Requeridos
    }

    fun v5validar(filter: FirmarDocumentoFilter): MutableList<String>? {
        Requeridos = validarNIT(filter.nit)
        if (filter.dteJson == null) {
            Requeridos!!.add(REQ_JSON_DTE)
        }
        if (filter.passwordPri == null || filter.passwordPri!!.length <= 0) {
            Requeridos!!.add(REQ_CLAVE_PRIVADA)
        }
        return Requeridos
    }

    fun getValidarNIT(): MutableList<String>? {
        return validarNIT("")
    }
}
