package com.billsv.firmador.validations

import org.slf4j.LoggerFactory


abstract class AbstractValidations {
    var requeridos: MutableList<String>? = null
    protected open var validarNIT: String? = null
    protected var valido: Boolean? = null
    fun ValidarNIT(nit: String?): MutableList<String>? {
        val requeridos: MutableList<String> = ArrayList()
        if (nit == null) {
            requeridos.add(REQ_NIT)
        } else if (!nit.matches("\\d{14}".toRegex())) {
            requeridos.add(REQ_NIT_FORMATO)
        }
        return requeridos
    }

    fun isValido(): Boolean {
        valido = false
        if (requeridos!!.size == 0) valido = true else {
            logger.info("isValido(): " + requeridos!!.size)
        }
        return valido!!
    }

    companion object {
        val logger = LoggerFactory.getLogger(AbstractValidations::class.java)
        var REQ_NIT = "NIT es requerido"
        var REQ_DATOS = "Objeto se recibió vacío"
        var REQ_NIT_FORMATO = "Formato de NIT no valido - (00000000000000)  "
        var REQ_JWS = "JSON WEB Signing es requerido"
        var REQ_NOMBRE_DOCUMENTO = "El nombre del docuemnto es requerido"
        var REQ_NOMBRE_FIRMA = "El nombre del firma es requerido"
        var REQ_JSON_DTE = "JsonDTE es requerido"
        var REQ_CLAVE_PRIVADA = "Clave privada es requerida"
        var REQ_CONFIRMACION_PRI = "Clave priva y confirmación no son iguales"
        var REQ_CLAVE_PUBLICA = "Clave publica es requerida"
        var REQ_CONFIRMACION_PUB = "Clave publica y confirmación no son iguales"
        var REQ_COMPACT_SERIALIZATION = "La Serialización Compacta es requerida"
        var REQ_SUBJECT_CONTRY_NAME = "Nombre del país es requerido"
        var REQ_SUBJECT_ORGANI_NAME = "Nombre de la organización es requerido"
        var REQ_SUBJECT_ORGANI_UNIT = "Nombre de la unidad es requerido"
        var REQ_SUBJECT_ORGANI_IDEN = "Organization Identifier es requerido"
        var REQ_SUBJECT_SURNAME = "Apellido del firmante es un campo requerido"
        var REQ_SUBJECT_GIVENNAME = "Nombre del firmante es un campo requerido"
        var REQ_SUBJECT_COMMON_NAME = "CommonName es un campo requerido"
        var REQ_SUBJECT_DESCRIPCION = "NRC es un campo requerido"
        var REQ_SUBJECT_EMAIL_ORGANI = "Correo de organización es un campo requerido"
    }
}

