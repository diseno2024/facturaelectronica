package com.billsv.firmador.models

import com.billsv.firmador.constantes.TipoLlave
import java.util.Arrays

data class Llave(
    var keyType: TipoLlave? = null,
    var algorithm: String? = null,
    var encodied: ByteArray? = null,
    var format: String? = null,
    var clave: String? = null
) {
    override fun toString(): String {
        return "Llave(keyType=$keyType, algorithm=$algorithm, encodied=${Arrays.toString(encodied)}, format=$format, clave=$clave)"
    }
}