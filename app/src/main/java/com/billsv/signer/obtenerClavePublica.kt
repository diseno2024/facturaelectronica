package com.billsv.signer

import java.security.KeyStore
import java.security.PublicKey

fun obtenerClavePublica(alias: String): PublicKey? {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    val certificado = keyStore.getCertificate(alias)
    return certificado?.publicKey
}