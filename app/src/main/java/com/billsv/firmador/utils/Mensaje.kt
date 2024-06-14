package com.billsv.firmador.utils


import com.billsv.firmador.constantes.Errores
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import java.io.IOException

class Mensaje {

    @Throws(IOException::class)
    fun ok(body: Any): ResponseBody {
        val responseBody = ResponseBody().apply {
            status = ResponseBody.status_ok
            this.body = body
        }
        return responseBody
    }

    @Throws(IOException::class)
    fun ok(body: JSONObject): ResponseBody {
        val responseBody = ResponseBody().apply {
            status = ResponseBody.status_ok
            val str = body.toString()
            val mapper = ObjectMapper()
            val factory = mapper.factory
            val parser = factory.createParser(str)
            val objNode = mapper.readTree<JsonNode>(parser)
            this.body = objNode
        }
        return responseBody
    }

    fun error(codigo: String, mensaje: String): ResponseBody {
        val body = BodyMensaje(codigo, mensaje)
        return ResponseBody().apply {
            status = ResponseBody.status_error
            this.body = body
        }
    }

    fun error(codigo: String, mensaje: Any): ResponseBody {
        val body = BodyMensaje(codigo, mensaje)
        return ResponseBody().apply {
            status = ResponseBody.status_error
            this.body = body
        }
    }

    fun error(body: Any): ResponseBody {
        return ResponseBody().apply {
            status = ResponseBody.status_error
            this.body = body
        }
    }

    fun error(body: JSONObject): ResponseBody {
        return ResponseBody().apply {
            status = ResponseBody.status_error
            this.body = body
        }
    }

    fun error(error: Errores): ResponseBody {
        val body = BodyMensaje(error.getCodigo(), error.getTexto())
        return ResponseBody().apply {
            status = ResponseBody.status_error
            this.body = body
        }
    }
}
