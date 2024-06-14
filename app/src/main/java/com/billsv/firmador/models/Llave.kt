package com.billsv.firmador.models


import com.billsv.firmador.constantes.TipoLlave

class Llave {
    var keyType: TipoLlave? = null
    var algorithm: String? = null
    lateinit var encodied: ByteArray
    var format: String? = null
    var clave: String? = null

    override fun toString(): String {
        return "Key [keyType=" + keyType + ", algorithm=" + algorithm + ", encodied=" + encodied.contentToString() + ", format=" + format + "]"
    }
}

