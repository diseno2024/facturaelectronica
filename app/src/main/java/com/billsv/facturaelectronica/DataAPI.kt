package com.billsv.facturaelectronica

import com.fasterxml.jackson.annotation.JsonPropertyOrder

//auth

data class AuthResponse(
    val status: String,
    val body: AuthBody?,
)

data class AuthBody(
    val user: String,
    val token: String,
    val rol: Rol,
    val roles: List<String>,
    val tokenType: String,
    val estado: String,
    val fhProcesamiento: String?,
    val codigoMsg: String,
    val descripcionMsg: String,
    val observaciones: String?,
    val clasificaMsg: String
)

data class Rol(
    val nombre: String,
    val codigo: String,
    val descripcion: String?,
    val rolSuperior: String?,
    val nivel: Int?,
    val activo: Boolean?,
    val permisos: List<String>?
)

data class ErrorResponse(
    val status: String,
    val body: ErrorBody?
)

data class ErrorBody(
    val estado: String,
    val fhProcesamiento: String?,
    val codigoMsg: String,
    val descripcionMsg: String,
    val observaciones: String?,
    val clasificaMsg: String
)
//recepcion
@JsonPropertyOrder("ambiente", "idenvio", "version", "tipoDTE", "documento", "codigoGeneracion")
data class RecepcionRequest(
    val ambiente: String,
    val idEnvio: Int,
    val version: Int,
    val tipoDte: String,
    val documento: String,
    val codigoGeneracion: String
)
data class RecepcionResponse(
    val version: Int,
    val ambiente: String,
    val versionApp: Int,
    val estado: String,
    val codigoGeneracion: String,
    val selloRecibido: String,
    val fhProcesamiento: String,
    val clasificaMsg: String,
    val codigoMsg: String,
    val descripcionMsg: String,
    val observaciones: List<String>
)
