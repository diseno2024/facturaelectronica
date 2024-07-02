package com.billsv.signer

import java.security.KeyStore
import java.security.PrivateKey

fun obtenerClavePrivada(alias: String = "BillSV"): PrivateKey? {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    return keyStore.getKey(alias, null) as? PrivateKey
}