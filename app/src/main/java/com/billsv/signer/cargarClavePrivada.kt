package com.billsv.signer

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.FileNotFoundException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

fun cargarClavePrivada(context: Context, uri: Uri): PrivateKey {
    // Usar ContentResolver para abrir el InputStream desde la URI
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw FileNotFoundException("No se pudo abrir el archivo desde la URI: $uri")

    // Leer el contenido del archivo
    val keyContent = inputStream.bufferedReader().use { it.readText() }

    // Imprime el contenido en los logs para verificarlo
    //Log.d("CargarClavePrivada", "Contenido del archivo: $keyContent")

    // Determina si es un archivo PEM o KEY
    val base64Key = when {
        keyContent.contains("-----BEGIN PRIVATE KEY-----") -> {
            keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
        }
        keyContent.contains("-----BEGIN RSA PRIVATE KEY-----") -> {
            keyContent
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
        }
        else -> {
            throw IllegalArgumentException("Formato de clave desconocido. El archivo debe contener una clave PEM o KEY válida.")
        }
    }

    //Log.d("CargarClavePrivada", "Base64: $base64Key")

    // Decodifica el contenido Base64
    val keyBytes = Base64.decode(base64Key, Base64.DEFAULT)

    // Crea la clave privada usando PKCS8EncodedKeySpec
    val keySpec = PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(keySpec)
}
