package com.rinchhen.vezzremote.protocol

import java.math.BigInteger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey

/**
 * The Polo pairing secret.
 *
 * SHA-256(client modulus || client exponent || server modulus || server exponent ||
 * last four PIN characters). The first two PIN characters are a checksum of the
 * first hash byte, so a typo is caught before the secret is sent to the TV.
 */
object PairingCrypto {

    fun normalizePin(pin: String): String =
        pin.trim().replace(" ", "").replace("-", "").uppercase()

    fun validatePin(pin: String): Boolean {
        val clean = normalizePin(pin)
        return clean.length == 6 && clean.all { it in '0'..'9' || it in 'A'..'F' }
    }

    fun computeSecret(
        clientCert: X509Certificate,
        serverCert: X509Certificate,
        pin: String,
    ): ByteArray {
        val cleanPin = normalizePin(pin)
        require(validatePin(cleanPin)) { "PIN must be six hexadecimal characters" }

        val client = clientCert.publicKey as? RSAPublicKey
            ?: error("Client certificate does not contain an RSA key")
        val server = serverCert.publicKey as? RSAPublicKey
            ?: error("TV certificate does not contain an RSA key")

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(unsignedBytes(client.modulus))
        digest.update(formattedExponent(client.publicExponent))
        digest.update(unsignedBytes(server.modulus))
        digest.update(formattedExponent(server.publicExponent))
        digest.update(hexToBytes(cleanPin.substring(2)))
        val result = digest.digest()

        val expected = cleanPin.substring(0, 2).toInt(16)
        val actual = result[0].toInt() and 0xFF
        require(expected == actual) {
            "PIN checksum mismatch: TV code was not entered correctly"
        }
        return result
    }

    fun fingerprint(cert: X509Certificate): String = sha256Hex(cert.encoded)

    fun sha256Hex(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data)
            .joinToString("") { "%02x".format(it) }

    fun hexToBytes(hex: String): ByteArray {
        val clean = if (hex.length % 2 == 0) hex else "0$hex"
        return ByteArray(clean.length / 2) { index ->
            clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    private fun unsignedBytes(value: BigInteger): ByteArray {
        var hex = value.toString(16).uppercase()
        if (hex.length % 2 != 0) hex = "0$hex"
        return hexToBytes(hex)
    }

    private fun formattedExponent(value: BigInteger): ByteArray {
        var hex = value.toString(16).uppercase()
        if (hex.length % 2 != 0) hex = "0$hex"
        return hexToBytes(hex)
    }
}
