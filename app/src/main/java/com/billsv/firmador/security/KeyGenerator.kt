package com.billsv.firmador.security

import org.jvnet.hk2.annotations.Service
import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


@Service
class KeyGenerator {
    @JvmOverloads
    fun ByteToPrivateKey(bytes: ByteArray?, algorithm: String? = RSA): PrivateKey? {
        val encode = PKCS8EncodedKeySpec(bytes)
        val keyFactory: KeyFactory
        var privatekey: PrivateKey? = null
        try {
            keyFactory = KeyFactory.getInstance(algorithm)
            privatekey = keyFactory.generatePrivate(encode)
        } catch (e: NoSuchAlgorithmException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return privatekey
    }

    @JvmOverloads
    fun ByteToPublicKey(bytes: ByteArray?, algorithm: String? = RSA): PublicKey? {
        val encode = X509EncodedKeySpec(bytes)
        val keyFactory: KeyFactory
        var publicKey: PublicKey? = null
        try {
            keyFactory = KeyFactory.getInstance(algorithm)
            publicKey = keyFactory.generatePublic(encode)
        } catch (e: NoSuchAlgorithmException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return publicKey
    }

    companion object {
        val logger = LoggerFactory.getLogger(KeyGenerator::class.java)
        var RSA = "RSA"
        var SHA1WITHRSA = "SHA1withRSA"
        var SHA256WITHRSA = "SHA256withRSA"
        var keysize = 2048
    }
}

