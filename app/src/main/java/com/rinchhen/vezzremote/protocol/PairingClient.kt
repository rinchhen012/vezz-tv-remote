package com.rinchhen.vezzremote.protocol

import com.google.protobuf.ByteString
import com.rinchhen.vezzremote.data.ClientIdentity
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.proto.pairing.PairingConfiguration
import com.rinchhen.vezzremote.proto.pairing.PairingEncoding
import com.rinchhen.vezzremote.proto.pairing.PairingMessage
import com.rinchhen.vezzremote.proto.pairing.PairingOption
import com.rinchhen.vezzremote.proto.pairing.PairingRequest
import com.rinchhen.vezzremote.proto.pairing.PairingSecret
import com.rinchhen.vezzremote.proto.pairing.RoleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket

/**
 * One-time pairing on port 6467. The TV displays a six-character hex PIN after the
 * configuration exchange; [onPin] is suspending so the UI can show its dialog while
 * the TLS connection stays open.
 */
class PairingClient {

    data class Result(
        val serverFingerprint: String,
        val serverName: String,
    )

    suspend fun pair(
        device: TvDevice,
        identity: ClientIdentity,
        onPin: suspend () -> String,
    ): Result = withContext(Dispatchers.IO) {
        var serverCertificate: X509Certificate? = null
        val factory = TlsFactory.socketFactory(identity) { serverCertificate = it }
        val socket = factory.createSocket() as SSLSocket
        socket.tcpNoDelay = true
        socket.keepAlive = true
        socket.soTimeout = 15_000

        try {
            socket.connect(InetSocketAddress(device.host, device.pairingPort), 10_000)
            socket.startHandshake()

            val server = serverCertificate
                ?: (socket.session.peerCertificates.firstOrNull() as? X509Certificate)
                ?: throw IOException("The TV did not present a certificate")

            val output = socket.outputStream
            val input = socket.inputStream

            fun send(message: PairingMessage) {
                message.writeDelimitedTo(output)
                output.flush()
            }

            fun receive(): PairingMessage =
                PairingMessage.parseDelimitedFrom(input)
                    ?: throw IOException("The TV closed the pairing connection")

            fun message(block: PairingMessage.Builder.() -> Unit): PairingMessage =
                PairingMessage.newBuilder()
                    .setProtocolVersion(2)
                    .setStatus(PairingMessage.Status.STATUS_OK)
                    .apply(block)
                    .build()

            fun encoding(): PairingEncoding = PairingEncoding.newBuilder()
                .setType(PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
                .setSymbolLength(6)
                .build()

            send(
                message {
                    setPairingRequest(
                        PairingRequest.newBuilder()
                            .setClientName(identity.clientName)
                            .setServiceName("atvremote"),
                    )
                },
            )
            val requestAck = receive()
            check(requestAck.status == PairingMessage.Status.STATUS_OK && requestAck.hasPairingRequestAck()) {
                "Unexpected PairingRequestAck: ${requestAck.status}"
            }

            send(
                message {
                    setPairingOption(
                        PairingOption.newBuilder()
                            .setPreferredRole(RoleType.ROLE_TYPE_INPUT)
                            .addInputEncodings(encoding()),
                    )
                },
            )
            val tvOption = receive()
            check(tvOption.status == PairingMessage.Status.STATUS_OK && tvOption.hasPairingOption()) {
                "Unexpected PairingOption: ${tvOption.status}"
            }
            check(
                tvOption.pairingOption.outputEncodingsList.any {
                    it.type == PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL &&
                        it.symbolLength == 6
                },
            ) { "The TV does not offer a six-character hexadecimal pairing code" }

            send(
                message {
                    setPairingConfiguration(
                        PairingConfiguration.newBuilder()
                            .setClientRole(RoleType.ROLE_TYPE_INPUT)
                            .setEncoding(encoding()),
                    )
                },
            )
            val configurationAck = receive()
            check(
                configurationAck.status == PairingMessage.Status.STATUS_OK &&
                    configurationAck.hasPairingConfigurationAck(),
            ) { "Unexpected PairingConfigurationAck: ${configurationAck.status}" }

            val pin = withTimeout(120_000) { onPin() }
            if (!PairingCrypto.validatePin(pin)) {
                throw IllegalArgumentException("Enter the six hexadecimal characters shown on the TV")
            }
            val secret = PairingCrypto.computeSecret(identity.certificate, server, pin)
            try {
                send(
                    message {
                        setPairingSecret(
                            PairingSecret.newBuilder().setSecret(ByteString.copyFrom(secret)),
                        )
                    },
                )
            } finally {
                secret.fill(0)
            }

            val secretAck = receive()
            check(
                secretAck.status == PairingMessage.Status.STATUS_OK &&
                    secretAck.hasPairingSecretAck() &&
                    secretAck.pairingSecretAck.secret.size() == 32,
            ) { "The TV rejected the pairing code" }

            Result(
                serverFingerprint = PairingCrypto.fingerprint(server),
                serverName = server.subjectX500Principal.name,
            )
        } finally {
            runCatching { socket.close() }
        }
    }
}
