package com.rinchhen.vezzremote.data

import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Minimal self-signed X.509 v1 certificate builder.
 *
 * Android Keystore can create a certificate for a generated key on API 26+, but some
 * OEM Keystores reject the TLS-specific key authorizations. This is the software
 * fallback: a 2048-bit RSA key kept in app-private storage and a small DER certificate.
 *
 * Adapted from the MIT-licensed Vestel Remote certificate helper; see NOTICE.md.
 */
object SoftwareCert {

    fun generate(keyPair: KeyPair, commonName: String): X509Certificate {
        val der = createDer(keyPair, commonName)
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(der)) as X509Certificate
    }

    private fun createDer(keyPair: KeyPair, commonName: String): ByteArray {
        val now = System.currentTimeMillis()
        val notBefore = Date(now - 24 * 3600 * 1000L)
        val notAfter = Date(now + 10L * 365 * 24 * 3600 * 1000L)
        val serial = BigInteger.valueOf(now)

        val sigAlgOid = byteArrayOf(
            0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(),
            0xF7.toByte(), 0x0D, 0x01, 0x01, 0x0B, 0x05, 0x00,
        )
        val sigAlgSeq = derSequence(sigAlgOid)
        val serialDer = derInteger(serial)
        val dnDer = createX500NameDer(commonName)
        val validityDer = createValidityDer(notBefore, notAfter)

        val tbsComponents = byteArrayOf(
            *serialDer,
            *sigAlgSeq,
            *dnDer,
            *validityDer,
            *dnDer,
            *keyPair.public.encoded,
        )
        val tbsBytes = derSequence(tbsComponents)

        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(tbsBytes)
        val signature = signer.sign()

        return derSequence(tbsBytes + sigAlgSeq + derBitString(signature))
    }

    private fun derSequence(content: ByteArray): ByteArray =
        byteArrayOf(0x30) + derLength(content.size) + content

    private fun derInteger(value: BigInteger): ByteArray {
        val bytes = value.toByteArray()
        return byteArrayOf(0x02) + derLength(bytes.size) + bytes
    }

    private fun derBitString(bytes: ByteArray): ByteArray {
        val content = byteArrayOf(0x00) + bytes
        return byteArrayOf(0x03) + derLength(content.size) + content
    }

    private fun derLength(length: Int): ByteArray = when {
        length < 128 -> byteArrayOf(length.toByte())
        length < 256 -> byteArrayOf(0x81.toByte(), length.toByte())
        else -> byteArrayOf(0x82.toByte(), (length shr 8).toByte(), (length and 0xFF).toByte())
    }

    private fun createX500NameDer(commonName: String): ByteArray {
        val cnOid = byteArrayOf(0x06, 0x03, 0x55, 0x04, 0x03)
        val cnValue = commonName.toByteArray(Charsets.UTF_8)
        val utf8String = byteArrayOf(0x0C) + derLength(cnValue.size) + cnValue
        val atv = derSequence(cnOid + utf8String)
        val rdn = byteArrayOf(0x31) + derLength(atv.size) + atv
        return derSequence(rdn)
    }

    private fun createValidityDer(notBefore: Date, notAfter: Date): ByteArray {
        val formatter = SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val nbBytes = formatter.format(notBefore).toByteArray(Charsets.US_ASCII)
        val naBytes = formatter.format(notAfter).toByteArray(Charsets.US_ASCII)
        val nbDer = byteArrayOf(0x17) + derLength(nbBytes.size) + nbBytes
        val naDer = byteArrayOf(0x17) + derLength(naBytes.size) + naBytes
        return derSequence(nbDer + naDer)
    }
}
