package com.billsv.firmador.utils

import java.io.Serializable

class BodyMensaje : Serializable {
    var codigo: String
    var mensaje: Any

    constructor(codigo: String, mensaje: String) {
        this.codigo = codigo
        this.mensaje = mensaje
    }

    constructor(codigo: String, mensaje: Any) {
        this.codigo = codigo
        this.mensaje = mensaje
    }

    companion object {
        private const val serialVersionUID = 985885270535689192L
    }
}

