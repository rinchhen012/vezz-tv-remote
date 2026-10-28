package com.rinchhen.vezzremote.protocol

import android.os.Build
import android.util.Log
import com.rinchhen.vezzremote.BuildConfig
import com.rinchhen.vezzremote.data.ClientIdentity
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.proto.remote.RemoteAppLinkLaunchRequest
import com.rinchhen.vezzremote.proto.remote.RemoteConfigure
import com.rinchhen.vezzremote.proto.remote.RemoteDeviceInfo
import com.rinchhen.vezzremote.proto.remote.RemoteDirection
import com.rinchhen.vezzremote.proto.remote.RemoteEditInfo
import com.rinchhen.vezzremote.proto.remote.RemoteImeBatchEdit
import com.rinchhen.vezzremote.proto.remote.RemoteImeObject
import com.rinchhen.vezzremote.proto.remote.RemoteKeyInject
import com.rinchhen.vezzremote.proto.remote.RemoteMessage
import com.rinchhen.vezzremote.proto.remote.RemotePingResponse
import com.rinchhen.vezzremote.proto.remote.RemoteSetActive
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket

/**
 * A live remote-v2 session on port 6466.
 *
 * The connection is persistent: a writer coroutine drains a channel so UI taps never
 * block, and a reader coroutine answers the TV's pings and tracks volume/current app.
 */
class RemoteClient(
    private val scope: CoroutineScope,
    private val identity: ClientIdentity,
    private val device: TvDevice,
) {
    sealed interface State {
        data object Disconnected : State
        data class Connecting(val device: String) : State
        data class Connected(val device: String) : State
        data class Error(val message: String) : State
    }

    data class VolumeState(
        val level: Int = -1,
        val max: Int = -1,
        val muted: Boolean = false,
    )

    private val _state = kotlinx.coroutines.flow.MutableStateFlow<State>(State.Disconnected)
    val state = _state.asStateFlow()

    private val _volume = kotlinx.coroutines.flow.MutableStateFlow(VolumeState())
    val volume = _volume.asStateFlow()

    private val _currentApp = kotlinx.coroutines.flow.MutableStateFlow("")
    val currentApp = _currentApp.asStateFlow()

    private val _isOn = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isOn = _isOn.asStateFlow()

    private var socket: SSLSocket? = null
    private var output: OutputStream? = null
    private var writerJob: Job? = null
    private var readerJob: Job? = null
    private var channel: Channel<RemoteMessage>? = null
    private var ready: CompletableDeferred<Unit>? = null

    @Volatile
    private var manualClose = false

    @Volatile
    private var activeFeatures = 0

    private var imeCounter = 0
    private var fieldCounter = 0

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        disconnect()
        manualClose = false
        _state.value = State.Connecting(device.name)

        try {
            val fingerprint = device.serverFingerprint
                ?: throw IllegalStateException("This TV is not paired yet")
            val factory = TlsFactory.socketFactory(identity, fingerprint)
            val sslSocket = factory.createSocket() as SSLSocket
            sslSocket.tcpNoDelay = true
            sslSocket.keepAlive = true
            // No read timeout: the TV's ping cadence changes with its screen/app
            // state, and a client heartbeat makes the TV stop pinging altogether.
            // A real close still ends the read; auto-reconnect handles that.
            sslSocket.soTimeout = 0
            // Publish the socket before the blocking connect so disconnect() can close it.
            socket = sslSocket
            Log.d(TAG, "TCP connecting to ${device.host}:${device.remotePort}")
            sslSocket.connect(InetSocketAddress(device.host, device.remotePort), 10_000)
            Log.d(TAG, "TCP connected; TLS handshake")
            sslSocket.startHandshake()
            Log.d(TAG, "TLS handshake complete: ${sslSocket.session.protocol} ${sslSocket.session.cipherSuite}")

            output = sslSocket.outputStream
            val input = sslSocket.inputStream
            val queue = Channel<RemoteMessage>(Channel.UNLIMITED)
            channel = queue
            val sessionReady = CompletableDeferred<Unit>()
            ready = sessionReady

            writerJob = scope.launch(Dispatchers.IO) {
                try {
                    for (message in queue) {
                        val out = output ?: break
                        if (message.hasRemoteAppLinkLaunchRequest()) {
                            Log.d(TAG, "writing appLink ${message.remoteAppLinkLaunchRequest.appLink}")
                        }
                        synchronized(out) {
                            message.writeDelimitedTo(out)
                            out.flush()
                        }
                    }
                } catch (_: Exception) {
                    if (!manualClose) fail("The TV closed the connection")
                }
            }

            readerJob = scope.launch(Dispatchers.IO) {
                readLoop(input)
            }

            val configured = withTimeoutOrNull(15_000) { sessionReady.await() }
            if (configured == null) {
                fail("The TV did not answer the remote configuration")
                false
            } else {
                Log.d(TAG, "Remote session configured")
                _state.value = State.Connected(device.name)
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Remote connect failed", e)
            fail("Could not connect: ${describe(e)}")
            false
        }
    }

    fun sendKey(key: RemoteKey, direction: RemoteDirection = RemoteDirection.SHORT) {
        enqueue(
            RemoteMessage.newBuilder()
                .setRemoteKeyInject(
                    RemoteKeyInject.newBuilder()
                        .setKeyCode(key.code)
                        .setDirection(direction),
                )
                .build(),
        )
    }

    fun sendText(text: String) {
        if (text.isEmpty()) return
        Log.d(TAG, "sendText ${text.length} chars ime=$imeCounter field=$fieldCounter")
        val cursor = (text.length - 1).coerceAtLeast(0)
        val textField = RemoteImeObject.newBuilder()
            .setStart(cursor)
            .setEnd(cursor)
            .setValue(text)
        val edit = RemoteEditInfo.newBuilder()
            .setInsert(1)
            .setTextFieldStatus(textField)
        val batch = RemoteImeBatchEdit.newBuilder()
            .setImeCounter(imeCounter)
            .setFieldCounter(fieldCounter)
            .addEditInfo(edit)
        enqueue(RemoteMessage.newBuilder().setRemoteImeBatchEdit(batch).build())
    }

    fun sendAppLink(url: String) {
        Log.d(TAG, "sendAppLink $url")
        enqueue(
            RemoteMessage.newBuilder()
                .setRemoteAppLinkLaunchRequest(
                    RemoteAppLinkLaunchRequest.newBuilder().setAppLink(url),
                )
                .build(),
        )
    }

    fun launchPackage(packageName: String) {
        sendAppLink("market://launch?id=$packageName")
    }

    fun disconnect() {
        manualClose = true
        ready = null
        readerJob?.cancel()
        readerJob = null
        writerJob?.cancel()
        writerJob = null
        channel?.close()
        channel = null
        runCatching { socket?.close() }
        socket = null
        output = null
        _state.value = State.Disconnected
    }

    private suspend fun readLoop(input: InputStream) {
        try {
            while (currentCoroutineContext().isActive) {
                val message = RemoteMessage.parseDelimitedFrom(input) ?: break
                handle(message)
            }
            if (!manualClose) fail("The TV closed the connection")
        } catch (e: Exception) {
            if (!manualClose) fail(e.message ?: "Connection lost")
        } finally {
            if (!manualClose) {
                runCatching { socket?.close() }
            }
        }
    }

    private fun handle(message: RemoteMessage) {
        when {
            message.hasRemoteConfigure() -> {
                val tvFeatures = message.remoteConfigure.code1
                activeFeatures = tvFeatures and REQUESTED_FEATURES
                Log.d(TAG, "TV features=$tvFeatures active=$activeFeatures")
                if (activeFeatures and FEATURE_KEY == 0) {
                    fail("The TV does not support remote key injection")
                    return
                }
                val info = RemoteDeviceInfo.newBuilder()
                    .setModel(Build.MODEL ?: "Android")
                    .setVendor(Build.MANUFACTURER ?: "Android")
                    .setUnknown1(1)
                    .setUnknown2("1")
                    .setPackageName(BuildConfig.APPLICATION_ID)
                    .setAppVersion(BuildConfig.VERSION_NAME)
                val configure = RemoteConfigure.newBuilder()
                    .setCode1(activeFeatures)
                    .setDeviceInfo(info)
                enqueue(RemoteMessage.newBuilder().setRemoteConfigure(configure).build())
                ready?.complete(Unit)
            }

            message.hasRemoteSetActive() -> {
                enqueue(
                    RemoteMessage.newBuilder()
                        .setRemoteSetActive(RemoteSetActive.newBuilder().setActive(activeFeatures))
                        .build(),
                )
            }

            message.hasRemotePingRequest() -> {
                if (BuildConfig.DEBUG) Log.v(TAG, "ping ${message.remotePingRequest.val1}")
                enqueue(
                    RemoteMessage.newBuilder()
                        .setRemotePingResponse(
                            RemotePingResponse.newBuilder()
                                .setVal1(message.remotePingRequest.val1),
                        )
                        .build(),
                )
            }

            message.hasRemoteStart() -> {
                _isOn.value = message.remoteStart.started
                ready?.complete(Unit)
            }

            message.hasRemoteSetVolumeLevel() -> {
                val value = message.remoteSetVolumeLevel
                _volume.value = VolumeState(
                    level = value.volumeLevel,
                    max = value.volumeMax,
                    muted = value.volumeMuted,
                )
            }

            message.hasRemoteImeKeyInject() -> {
                val packageName = message.remoteImeKeyInject.appInfo.appPackage
                Log.d(TAG, "active app $packageName")
                if (packageName.isNotBlank()) _currentApp.value = packageName
            }

            message.hasRemoteImeBatchEdit() -> {
                imeCounter = message.remoteImeBatchEdit.imeCounter
                fieldCounter = message.remoteImeBatchEdit.fieldCounter
                Log.d(TAG, "IME batch edit ime=$imeCounter field=$fieldCounter")
            }

            message.hasRemoteImeShowRequest() -> {
                fieldCounter = message.remoteImeShowRequest.remoteTextFieldStatus.counterField
                Log.d(TAG, "IME show request field=$fieldCounter")
            }

            message.hasRemoteError() -> {
                fail("The TV reported a remote protocol error")
            }
        }
    }

    private fun enqueue(message: RemoteMessage) {
        channel?.trySend(message)
    }

    private fun describe(error: Throwable): String {
        val messages = generateSequence(error as Throwable?) { it.cause }
            .mapNotNull { it.message }
            .filter { it.isNotBlank() }
            .toList()
        return messages.firstOrNull() ?: error.javaClass.simpleName
    }

    private fun fail(message: String) {
        if (manualClose) return
        Log.w(TAG, "Remote session failed: $message")
        manualClose = true
        ready?.completeExceptionally(IllegalStateException(message))
        readerJob?.cancel()
        writerJob?.cancel()
        channel?.close()
        channel = null
        runCatching { socket?.close() }
        socket = null
        output = null
        _state.value = State.Error(message)
    }

    private companion object {
        const val TAG = "VezzRemote"
        const val FEATURE_PING = 1
        const val FEATURE_KEY = 2
        const val FEATURE_IME = 4
        const val FEATURE_POWER = 32
        const val FEATURE_VOLUME = 64
        const val FEATURE_APP_LINK = 512
        const val REQUESTED_FEATURES =
            FEATURE_PING or FEATURE_KEY or FEATURE_IME or FEATURE_POWER or FEATURE_VOLUME or FEATURE_APP_LINK
    }
}
