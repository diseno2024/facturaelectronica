package com.billsv.signer

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec

fun cargarClavePrivada(clave: String): PrivateKey {
    val base64Key = clave
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("-----BEGIN RSA PRIVATE KEY-----", "")
        .replace("-----END RSA PRIVATE KEY-----", "")
        .replace("\\s+".toRegex(), "")

    val keyBytes = Base64.decode(base64Key, Base64.DEFAULT)

    return try {
        // Intenta primero PKCS#8
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        keyFactory.generatePrivate(keySpec)
    } catch (e: Exception) {
        Log.w("CargarClavePrivada", "Fallo con PKCS#8, intentando PKCS#1", e)
        // Si falla, intenta PKCS#1
        cargarClavePrivadaPKCS1(keyBytes)
    }
}

fun cargarClavePrivadaPKCS1(keyBytes: ByteArray): PrivateKey {
    try {
        val asn1Sequence = ASN1InputStream(ByteArrayInputStream(keyBytes)).use { asn1 ->
            asn1.readObject() as ASN1Sequence
        }

        // Extraer valores directamente usando el índice
        val modulus = (asn1Sequence.getObjectAt(1) as ASN1Integer).positiveValue
        val publicExponent = (asn1Sequence.getObjectAt(2) as ASN1Integer).positiveValue
        val privateExponent = (asn1Sequence.getObjectAt(3) as ASN1Integer).positiveValue
        val prime1 = (asn1Sequence.getObjectAt(4) as ASN1Integer).positiveValue
        val prime2 = (asn1Sequence.getObjectAt(5) as ASN1Integer).positiveValue
        val exponent1 = (asn1Sequence.getObjectAt(6) as ASN1Integer).positiveValue
        val exponent2 = (asn1Sequence.getObjectAt(7) as ASN1Integer).positiveValue
        val coefficient = (asn1Sequence.getObjectAt(8) as ASN1Integer).positiveValue

        val keySpec = RSAPrivateCrtKeySpec(
            modulus, publicExponent, privateExponent,
            prime1, prime2, exponent1, exponent2, coefficient
        )

        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    } catch (e: Exception) {
        throw IllegalArgumentException("Error al cargar clave PKCS#1: ${e.message}", e)
    }
}
