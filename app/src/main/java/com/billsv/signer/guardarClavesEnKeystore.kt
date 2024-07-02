package com.billsv.signer

import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.X509Certificate

class guardarClavesEnKeystore {

    fun guardarClavesEnKeystore(keyPair: KeyPair, certificate: X509Certificate, keyAlias: String = "BillSV") {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // Guardar la clave y el certificado en el keystore
        keyStore.setKeyEntry(
            keyAlias,
            keyPair.private,
            null,
            arrayOf(certificate)
        )
    }
}