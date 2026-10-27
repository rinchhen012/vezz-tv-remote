package com.rinchhen.vezzremote.protocol

import com.rinchhen.vezzremote.data.ClientIdentity
import android.util.Log
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager

/**
 * TLS for the Android TV Remote Service.
 *
 * The TV asks for a client certificate without naming a trusted issuer, so a standard
 * KeyManager would conclude that no suitable certificate exists. This KeyManager always
 * presents the single Vezz Remote identity. During pairing the server certificate is
 * accepted and captured; afterwards its SHA-256 fingerprint is pinned.
 */
object TlsFactory {

    fun socketFactory(
        identity: ClientIdentity,
        expectedServerFingerprint: String? = null,
        onServerCertificate: (X509Certificate) -> Unit = {},
    ): SSLSocketFactory {
        val keyManager = object : X509ExtendedKeyManager() {
            override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> =
                if (keyType == null || keyType.contains("RSA")) arrayOf(ALIAS) else emptyArray()

            override fun chooseClientAlias(
                keyType: Array<out String>?,
                issuers: Array<out Principal>?,
                socket: Socket?,
            ): String? {
                Log.d(TAG, "TLS client alias requested keyType=${keyType?.joinToString()}")
                return if (keyType == null || keyType.any { it.contains("RSA") }) ALIAS else null
            }

            override fun getCertificateChain(alias: String?): Array<X509Certificate> =
                arrayOf(identity.certificate)

            override fun getPrivateKey(alias: String?): PrivateKey = identity.privateKey

            override fun getServerAliases(
                keyType: String?,
                issuers: Array<out Principal>?,
            ): Array<String> = emptyArray()

            override fun chooseServerAlias(
                keyType: String?,
                issuers: Array<out Principal>?,
                socket: Socket?,
            ): String? = null
        }

        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                val certificate = chain?.firstOrNull()
                    ?: throw SSLException("TV sent no certificate")
                onServerCertificate(certificate)
                val actual = PairingCrypto.fingerprint(certificate)
                Log.d(TAG, "TV certificate sha256=$actual expected=${expectedServerFingerprint ?: "<trust-on-first-use>"}")
                if (expectedServerFingerprint != null) {
                    if (!actual.equals(expectedServerFingerprint, ignoreCase = true)) {
                        Log.w(TAG, "TV certificate pin mismatch")
                        throw SSLException("TV certificate does not match the paired device")
                    }
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }

        val context = SSLContext.getInstance("TLS")
        context.init(arrayOf(keyManager), arrayOf(trustManager), SecureRandom())
        return context.socketFactory
    }

    private const val ALIAS = "vezz-remote-client"
    private const val TAG = "VezzRemote"
}
