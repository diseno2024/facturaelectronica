package com.billsv.signer

import android.content.Context
import android.net.Uri
import java.io.FileNotFoundException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

fun cargarCertificado(context: Context, uri: Uri): X509Certificate {
    // Usa ContentResolver para abrir el InputStream desde la URI
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw FileNotFoundException("No se pudo abrir el archivo desde la URI: $uri")

    // Leer el contenido del archivo como bytes
    val certBytes = inputStream.readBytes()

    return try {
        // Intentar procesar como certificado en formato DER o PEM
        val certFactory = CertificateFactory.getInstance("X.509")
        certFactory.generateCertificate(certBytes.inputStream()) as X509Certificate
    } catch (e: Exception) {
        // Si falla, verificar si es un certificado PEM con encabezados
        val certContent = String(certBytes)
        if (certContent.contains("-----BEGIN CERTIFICATE-----")) {
            val base64Cert = certContent
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\\s+".toRegex(), "")
            val decodedBytes = android.util.Base64.decode(base64Cert, android.util.Base64.DEFAULT)
            val certFactory = CertificateFactory.getInstance("X.509")
            certFactory.generateCertificate(decodedBytes.inputStream()) as X509Certificate
        } else {
            throw IllegalArgumentException("El archivo no es un certificado válido en formato DER o PEM.")
        }
    }
}
