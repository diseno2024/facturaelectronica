package com.billsv.firmador.validations

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractValidations {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AbstractValidations::class.java)
        const val REQ_NIT = "NIT es requerido"
        const val REQ_DATOS = "Objeto se recibió vacío"
        const val REQ_NIT_FORMATO = "Formato de NIT no valido - (00000000000000)"
        const val REQ_JWS = "JSON WEB Signing es requerido"
        const val REQ_NOMBRE_DOCUMENTO = "El nombre del docuemnto es requerido"
        const val REQ_NOMBRE_FIRMA = "El nombre del firma es requerido"
        const val REQ_JSON_DTE = "JsonDTE es requerido"
        const val REQ_CLAVE_PRIVADA = "Clave privada es requerida"
        const val REQ_CONFIRMACION_PRI = "Clave priva y confirmación no son iguales"
        const val REQ_CLAVE_PUBLICA = "Clave publica es requerida"
        const val REQ_CONFIRMACION_PUB = "Clave publica y confirmación no son iguales"
        const val REQ_COMPACT_SERIALIZATION = "La Serialización Compacta es requerida"
        const val REQ_SUBJECT_CONTRY_NAME = "Nombre del país es requerido"
        const val REQ_SUBJECT_ORGANI_NAME = "Nombre de la organización es requerido"
        const val REQ_SUBJECT_ORGANI_UNIT = "Nombre de la unidad es requerido"
        const val REQ_SUBJECT_ORGANI_IDEN = "Organization Identifier es requerido"
        const val REQ_SUBJECT_SURNAME = "Apellido del firmante es un campo requerido"
        const val REQ_SUBJECT_GIVENNAME = "Nombre del firmante es un campo requerido"
        const val REQ_SUBJECT_COMMON_NAME = "CommonName es un campo requerido"
        const val REQ_SUBJECT_DESCRIPCION = "NRC es un campo requerido"
        const val REQ_SUBJECT_EMAIL_ORGANI = "Correo de organización es un campo requerido"
    }

    var Requeridos: MutableList<String>? = null
    protected var validarNitMessage: String? = null
    protected var valido: Boolean? = null

    fun validarNIT(nit: String?): MutableList<String>? {
        val requeridos = mutableListOf<String>()
        if (nit == null) {
            requeridos.add(REQ_NIT)
        } else if (!nit.matches("\\d{14}".toRegex())) {
            requeridos.add(REQ_NIT_FORMATO)
        }
        return requeridos
    }

    fun isValid(): Boolean {
        valido = false
        valido = Requeridos?.isEmpty() ?: false
        return valido ?: false
    }
}
