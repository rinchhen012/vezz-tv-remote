package com.rinchhen.vezzremote.data

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.Locale
import javax.security.auth.x500.X500Principal

/**
 * The phone's long-lived pairing identity.
 *
 * The Android TV Remote Service stores one pairing per client-certificate subject.
 * Every install therefore gets a unique subject such as
 * "Vezz Remote (vivo X200 Pro 3F2A)", so pairing a second phone does not invalidate
 * the first one. The private key lives in Android Keystore when possible; the
 * software fallback keeps it in app-private storage.
 */
data class ClientIdentity(
    val clientName: String,
    val privateKey: PrivateKey,
    val certificate: X509Certificate,
    val storage: Storage,
) {
    enum class Storage { ANDROID_KEYSTORE, SOFTWARE }
}

object ClientIdentityProvider {
    private const val PREFS = "vezz_remote_identity"
    private const val KEY_NAME = "client_name"
    private const val KEY_ALIAS = "vezz_remote_client"
    private const val KEY_FILE = "client_key.pk8"
    private const val CERT_FILE = "client_cert.der"

    @Synchronized
    fun getOrCreate(context: Context): ClientIdentity {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        loadKeystore(app, prefs)?.let { return it }
        loadSoftware(app, prefs)?.let { return it }

        val name = prefs.getString(KEY_NAME, null) ?: buildName()
        prefs.edit().putString(KEY_NAME, name).apply()

        generateKeystore(app, name)?.let { identity ->
            prefs.edit().putString(KEY_NAME, identity.clientName).apply()
            return identity
        }

        val identity = generateSoftware(app, name)
        prefs.edit().putString(KEY_NAME, identity.clientName).apply()
        return identity
    }

    @Synchronized
    fun reset(context: Context): ClientIdentity {
        val app = context.applicationContext
        runCatching {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (store.containsAlias(KEY_ALIAS)) store.deleteEntry(KEY_ALIAS)
        }
        File(app.filesDir, KEY_FILE).delete()
        File(app.filesDir, CERT_FILE).delete()
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_NAME).apply()
        return getOrCreate(app)
    }

    private fun loadKeystore(context: Context, prefs: android.content.SharedPreferences): ClientIdentity? {
        return runCatching {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (!store.containsAlias(KEY_ALIAS)) return null
            val key = store.getKey(KEY_ALIAS, null) as? PrivateKey ?: return null
            val cert = store.getCertificate(KEY_ALIAS) as? X509Certificate ?: return null
            val name = prefs.getString(KEY_NAME, null) ?: commonName(cert)
            ClientIdentity(name, key, cert, ClientIdentity.Storage.ANDROID_KEYSTORE)
        }.getOrNull()
    }

    private fun loadSoftware(context: Context, prefs: android.content.SharedPreferences): ClientIdentity? {
        val keyFile = File(context.filesDir, KEY_FILE)
        val certFile = File(context.filesDir, CERT_FILE)
        if (!keyFile.exists() || !certFile.exists()) return null
        return runCatching {
            val key = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(keyFile.readBytes()))
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(certFile.inputStream()) as X509Certificate
            val proof = Signature.getInstance("SHA256withRSA").run {
                initSign(key)
                update(byteArrayOf(1))
                sign()
            }
            check(Signature.getInstance("SHA256withRSA").run {
                initVerify(cert)
                update(byteArrayOf(1))
                verify(proof)
            }) { "Stored key does not match certificate" }
            val name = prefs.getString(KEY_NAME, null) ?: commonName(cert)
            ClientIdentity(name, key, cert, ClientIdentity.Storage.SOFTWARE)
        }.getOrNull()
    }

    private fun generateKeystore(context: Context, name: String): ClientIdentity? {
        return runCatching {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (store.containsAlias(KEY_ALIAS)) store.deleteEntry(KEY_ALIAS)

            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore",
            )
            generator.initialize(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
                )
                    .setKeySize(2048)
                    .setDigests(
                        KeyProperties.DIGEST_NONE,
                        KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA384,
                        KeyProperties.DIGEST_SHA512,
                    )
                    .setSignaturePaddings(
                        KeyProperties.SIGNATURE_PADDING_RSA_PKCS1,
                        KeyProperties.SIGNATURE_PADDING_RSA_PSS,
                    )
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setCertificateSubject(X500Principal("CN=$name, O=Vezz Remote"))
                    .setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
                    .setCertificateNotBefore(Date(System.currentTimeMillis() - 24 * 3600 * 1000L))
                    .setCertificateNotAfter(Date(System.currentTimeMillis() + 10L * 365 * 24 * 3600 * 1000L))
                    .build(),
            )
            generator.generateKeyPair()
            val key = store.getKey(KEY_ALIAS, null) as? PrivateKey ?: return null
            val cert = store.getCertificate(KEY_ALIAS) as? X509Certificate ?: return null
            ClientIdentity(name, key, cert, ClientIdentity.Storage.ANDROID_KEYSTORE)
        }.getOrNull()
    }

    private fun generateSoftware(context: Context, name: String): ClientIdentity {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val pair = generator.generateKeyPair()
        val cert = SoftwareCert.generate(pair, name)
        File(context.filesDir, KEY_FILE).writeBytes(pair.private.encoded)
        File(context.filesDir, CERT_FILE).writeBytes(cert.encoded)
        return ClientIdentity(name, pair.private, cert, ClientIdentity.Storage.SOFTWARE)
    }

    private fun commonName(cert: X509Certificate): String {
        val dn = cert.subjectX500Principal.name
        return dn.substringAfter("CN=", "Vezz Remote").substringBefore(",")
    }

    private fun buildName(): String {
        val raw = "${Build.MANUFACTURER} ${Build.MODEL}"
            .replace(Regex("[^A-Za-z0-9 ._-]"), "")
            .trim()
            .ifBlank { "Android" }
            .take(32)
        val suffix = String.format(Locale.US, "%04X", SecureRandom().nextInt(0x10000))
        return "Vezz Remote ($raw $suffix)"
    }
}
