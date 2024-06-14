package com.billsv.firmador.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class Cryptographic {
    @JvmOverloads
    @Throws(NoSuchAlgorithmException::class)
    fun encrypt(p: String, sha: String? = SHA256): String {
        val diget = MessageDigest.getInstance(sha)
        val encodedhash = diget.digest(p.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(encodedhash)
    }

    companion object {
        const val SHA256 = "SHA-256"
        const val SHA512 = "SHA-512"
        private fun bytesToHex(hash: ByteArray): String {
            val hexString = StringBuffer()
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        }
    }
}


