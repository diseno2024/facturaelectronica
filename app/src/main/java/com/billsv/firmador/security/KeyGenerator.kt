package com.billsv.firmador.security

import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KeyGenerator {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KeyGenerator::class.java)
        const val RSA = "RSA"
        const val SHA1WITHRSA = "SHA1withRSA"
        const val SHA256WITHRSA = "SHA256withRSA"
        const val keysize = 2048
    }

    fun byteToPrivateKey(bytes: ByteArray, algorithm: String = RSA): PrivateKey? {
        val encode = PKCS8EncodedKeySpec(bytes)
        return try {
            val keyFactory = KeyFactory.getInstance(algorithm)
            keyFactory.generatePrivate(encode)
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Algorithm not found: $algorithm", e)
            null
        } catch (e: InvalidKeySpecException) {
            logger.error("Invalid Key Spec", e)
            null
        }
    }

    fun byteToPublicKey(bytes: ByteArray, algorithm: String = RSA): PublicKey? {
        val encode = X509EncodedKeySpec(bytes)
        return try {
            val keyFactory = KeyFactory.getInstance(algorithm)
            keyFactory.generatePublic(encode)
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Algorithm not found: $algorithm", e)
            null
        } catch (e: InvalidKeySpecException) {
            logger.error("Invalid Key Spec", e)
            null
        }
    }
}