package com.billsv.firmador.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Cryptographic {

    companion object {
        const val SHA256 = "SHA-256"
        const val SHA512 = "SHA-512"
    }

    @Throws(NoSuchAlgorithmException::class)
    fun encrypt(p: String): String {
        return encrypt(p, SHA256)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun encrypt(p: String, sha: String): String {
        val digest = MessageDigest.getInstance(sha)
        val encodedhash = digest.digest(p.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(encodedhash)
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder()
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
