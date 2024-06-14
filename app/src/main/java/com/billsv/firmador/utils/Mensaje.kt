package com.billsv.firmador.utils


import com.billsv.firmador.constantes.Errores
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import org.jvnet.hk2.annotations.Service
import java.io.IOException


@Service
class Mensaje {
    fun ok(body: Any?): ResponseBody {
        val responseBody = ResponseBody()
        responseBody.status = ResponseBody.status_ok
        responseBody.body = body
        return responseBody
    }

    @Throws(IOException::class)
    fun ok(body: JSONObject): ResponseBody {
        val responseBody = ResponseBody()
        responseBody.status = ResponseBody.status_ok
        val str = body.toString()
        val mapper = ObjectMapper()
        val factory = mapper.factory
        val parser = factory.createParser(str)
        val objNode = mapper.readTree<JsonNode>(parser)
        responseBody.body = objNode
        return responseBody
    }

    fun error(codigo: String?, mensaje: String?): ResponseBody {
        val responseBody = ResponseBody()
        val body = BodyMensaje(codigo!!, mensaje!!)
        responseBody.status = ResponseBody.status_error
        responseBody.body = body
        return responseBody
    }

    fun error(codigo: String?, mensaje: Any?): ResponseBody {
        val responseBody = ResponseBody()
        val body = BodyMensaje(codigo!!, mensaje!!)
        responseBody.status = ResponseBody.status_error
        responseBody.body = body
        return responseBody
    }

    fun error(body: Any?): ResponseBody {
        val responseBody = ResponseBody()
        responseBody.status = ResponseBody.status_error
        responseBody.body = body
        return responseBody
    }

    fun error(body: JSONObject?): ResponseBody {
        val responseBody = ResponseBody()
        responseBody.status = ResponseBody.status_error
        responseBody.body = body
        return responseBody
    }

    fun error(error: Errores): ResponseBody {
        val responseBody = ResponseBody()
        val body = BodyMensaje(error.code, error.text)
        responseBody.status = ResponseBody.status_error
        responseBody.body = body
        return responseBody
    }
}

