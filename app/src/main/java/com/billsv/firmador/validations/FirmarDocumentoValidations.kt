package com.billsv.firmador.validations

import com.billsv.firmador.filter.FirmarDocumentoFilter
import org.jvnet.hk2.annotations.Service
import org.slf4j.LoggerFactory


@Service
class FirmarDocumentoValidations : AbstractValidations() {
    fun v2validar(filter: FirmarDocumentoFilter): List<String>? {
        requeridos = ValidarNIT(filter.nit)
        if (filter.compactSerialization == null || filter.compactSerialization!!.length <= 0) {
            requeridos?.add(REQ_JWS)
        }
        return requeridos
    }

    fun v1validar(filter: FirmarDocumentoFilter): List<String> {
        requeridos = ValidarNIT(filter.nit)
        if (filter.nombreDocumento == null || filter.nombreDocumento!!.length <= 0) {
            requeridos?.add(REQ_NOMBRE_DOCUMENTO)
        }
        if (filter.nombreFirma == null || filter.nombreFirma!!.length <= 0) {
            requeridos?.add(REQ_NOMBRE_FIRMA)
        }
        logger.info("requeridos: $requeridos")
        return requeridos!!
    }

    fun v3validar(filter: FirmarDocumentoFilter): List<String> {
        requeridos = ValidarNIT(filter.nit)
        if (validarNIT != null) {
            requeridos?.add(validarNIT!!)
        }
        //		if(filter.getDteJson() == null || filter.getDteJson().length()<=0) {
//			this.requeridos.add(REQ_JSON_DTE);
//		}
        if (filter.passwordPri == null || filter.passwordPri!!.length <= 0) {
            requeridos?.add(REQ_CLAVE_PRIVADA)
        }
        return requeridos!!
    }

    fun v5validar(filter: FirmarDocumentoFilter): List<String> {
        requeridos = ValidarNIT(filter.nit)
        if (filter.dteJson == null) {
            requeridos?.add(REQ_JSON_DTE)
        }
        if (filter.passwordPri == null || filter.passwordPri!!.length <= 0) {
            requeridos?.add(REQ_CLAVE_PRIVADA)
        }
        return requeridos!!
    }

    override var validarNIT: String? = null
        set(validarNIT) {
            super.validarNIT = validarNIT
        }

    companion object {
        var logger = LoggerFactory.getLogger(FirmarDocumentoValidations::class.java)
    }
}
