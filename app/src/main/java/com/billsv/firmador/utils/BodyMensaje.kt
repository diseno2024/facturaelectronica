package com.billsv.firmador.utils

import java.io.Serializable

class BodyMensaje(var codigo: String, var mensaje: Any?) : Serializable {

    companion object {
        private const val serialVersionUID = 985885270535689192L
    }

    constructor(codigo: String, mensaje: String) : this(codigo, mensaje as Any?)

}