package com.rinchhen.vezzremote.data

/**
 * A TV that can be controlled over the Android TV Remote v2 protocol.
 *
 * [id] is stable across IP changes (the mDNS service name for discovered TVs, or the
 * host for manually added ones). [serverFingerprint] is the SHA-256 of the TV's TLS
 * certificate, pinned after pairing.
 */
data class TvDevice(
    val id: String,
    val name: String,
    val host: String,
    val pairingPort: Int = DEFAULT_PAIRING_PORT,
    val remotePort: Int = DEFAULT_REMOTE_PORT,
    val serverFingerprint: String? = null,
    val lastConnectedAt: Long = 0L,
) {
    val isPaired: Boolean get() = !serverFingerprint.isNullOrBlank()

    companion object {
        const val DEFAULT_PAIRING_PORT = 6467
        const val DEFAULT_REMOTE_PORT = 6466
    }
}
