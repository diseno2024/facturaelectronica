package com.billsv.signer

import android.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.security.PrivateKey
import java.security.Signature

fun firmarDatos(datos: Any, clavePrivada: PrivateKey): String {
    // Firmar los datos con la clave privada
    val signer = Signature.getInstance("SHA512withRSA")
    signer.initSign(clavePrivada)

    // Convertir los datos a bytes
    val mapper = ObjectMapper().registerModule(KotlinModule())
    val datosBytes = mapper.writeValueAsBytes(datos)
    signer.update(datosBytes)

    val firma = signer.sign()
    return Base64.encodeToString(firma, Base64.NO_WRAP)
}