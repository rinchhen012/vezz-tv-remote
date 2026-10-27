package com.rinchhen.vezzremote

import com.rinchhen.vezzremote.protocol.PairingCrypto
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date

class PairingCryptoTest {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    @Test
    fun `pin validation accepts six hex characters`() {
        assertTrue(PairingCrypto.validatePin("A1B2C3"))
        assertTrue(PairingCrypto.validatePin("a1b2c3"))
        assertFalse(PairingCrypto.validatePin("A1B2C"))
        assertFalse(PairingCrypto.validatePin("A1B2CG"))
        assertEquals("A1B2C3", PairingCrypto.normalizePin(" a1 b2-c3 "))
    }

    @Test
    fun `exactly one checksum prefix produces a pairing secret`() {
        val client = certificate("client")
        val server = certificate("server")
        val matches = mutableListOf<String>()

        for (prefix in 0..0xFF) {
            val pin = "%02X1234".format(prefix)
            try {
                val secret = PairingCrypto.computeSecret(client, server, pin)
                assertEquals(32, secret.size)
                matches += pin
            } catch (_: IllegalArgumentException) {
                // The other 255 prefixes correctly fail the checksum.
            }
        }

        assertEquals(1, matches.size)
    }

    @Test
    fun `a wrong checksum is rejected`() {
        val client = certificate("client")
        val server = certificate("server")
        try {
            PairingCrypto.computeSecret(client, server, "001234")
            fail("A wrong checksum should throw")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("checksum", ignoreCase = true))
        }
    }

    @Test
    fun `fingerprint is a sha256 hex string`() {
        val fingerprint = PairingCrypto.fingerprint(certificate("client"))
        assertEquals(64, fingerprint.length)
        assertTrue(fingerprint.all { it in '0'..'9' || it in 'a'..'f' })
    }

    private fun certificate(name: String): X509Certificate {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val pair: KeyPair = generator.generateKeyPair()
        val issuer = X500Name("CN=$name")
        val builder = JcaX509v3CertificateBuilder(
            issuer,
            BigInteger.valueOf(System.nanoTime()),
            Date(System.currentTimeMillis() - 60_000),
            Date(System.currentTimeMillis() + 3_600_000),
            issuer,
            SubjectPublicKeyInfo.getInstance(pair.public.encoded),
        )
        val signer = JcaContentSignerBuilder("SHA256withRSA").build(pair.private)
        return JcaX509CertificateConverter().getCertificate(builder.build(signer))
    }
}
