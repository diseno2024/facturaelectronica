package com.billsv.firmador.models

import com.billsv.firmador.models.minec.Certificado
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date


@JsonIgnoreProperties(ignoreUnknown = true)
class CertificadoMH : AbsDocumentos() {
    var nit: String? = null
    var publicKey: Llave? = null
    var privateKey: Llave? = null
    var activo: Boolean? = null
    var certificado: Certificado? = null
    var clavePub: String? = null
    var clavePri: String? = null
    var verificado: Boolean? = null

    @JsonFormat(pattern = "yyyy-MM-dd'Y'HH:mm:ss.SSS'Z'")
    var fechaVerificacion: Date? = null

    override fun toString(): String {
        return ("Certificado [nit=" + nit + ", publicKey=" + publicKey + ", privateKey=" + privateKey + ", activo="
                + activo + ", clavePub=" + clavePub + ", clavePri=" + clavePri + "]")
    }
}

