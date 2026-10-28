package com.rinchhen.vezzremote.ui

import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinchhen.vezzremote.AppContainer
import com.rinchhen.vezzremote.VezzRemoteApp
import com.rinchhen.vezzremote.data.ClientIdentity
import com.rinchhen.vezzremote.data.ClientIdentityProvider
import com.rinchhen.vezzremote.data.AppCatalog
import com.rinchhen.vezzremote.data.AppIconCache
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.data.TvCatalogClient
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.PairingClient
import com.rinchhen.vezzremote.protocol.PairingCrypto
import com.rinchhen.vezzremote.protocol.DialClient
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.proto.remote.RemoteDirection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface PairingState {
        data object Idle : PairingState
        data class Connecting(val device: TvDevice) : PairingState
        data class NeedsPin(val device: TvDevice, val error: String? = null) : PairingState
        data class Verifying(val device: TvDevice) : PairingState
        data class Failed(val device: TvDevice?, val message: String) : PairingState
    }

    sealed interface VoiceState {
        data object Idle : VoiceState
        data object Starting : VoiceState
        data object Recording : VoiceState
        data class Error(val message: String) : VoiceState
    }

    private val container: AppContainer = (application as VezzRemoteApp).container
    private val store = container.store
    private val discovery = container.discovery
    private var identity: ClientIdentity? = null

    private val _identityReady = MutableStateFlow(false)
    val identityReady = _identityReady.asStateFlow()

    private val _pairedDevices = MutableStateFlow(store.devices())
    val pairedDevices = _pairedDevices.asStateFlow()

    val discoveredDevices = discovery.devices

    private val _activeDevice = MutableStateFlow<TvDevice?>(null)
    val activeDevice = _activeDevice.asStateFlow()

    private val _remoteState = MutableStateFlow<RemoteClient.State>(RemoteClient.State.Disconnected)
    val remoteState = _remoteState.asStateFlow()

    private val _reconnecting = MutableStateFlow(false)
    val reconnecting = _reconnecting.asStateFlow()

    private val _volume = MutableStateFlow(RemoteClient.VolumeState())
    val volume = _volume.asStateFlow()

    private val _currentApp = MutableStateFlow("")
    val currentApp = _currentApp.asStateFlow()

    private val _isOn = MutableStateFlow(false)
    val isOn = _isOn.asStateFlow()

    private val _pairing = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairing = _pairing.asStateFlow()

    private val _haptics = MutableStateFlow(store.haptics)
    val haptics = _haptics.asStateFlow()

    private val _leftHanded = MutableStateFlow(store.leftHanded)
    val leftHanded = _leftHanded.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(store.keepScreenOn)
    val keepScreenOn = _keepScreenOn.asStateFlow()

    private val _apps = MutableStateFlow(
        AppCatalog.apps.map { it.copy(favorite = it.packageName in store.favoritePackages) },
    )
    val apps = _apps.asStateFlow()

    private val _recentPackages = MutableStateFlow(store.recentPackages)
    val recentPackages = _recentPackages.asStateFlow()

    private val _nowPlaying = MutableStateFlow<TvCatalogClient.NowPlaying?>(null)
    val nowPlaying = _nowPlaying.asStateFlow()

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState = _voiceState.asStateFlow()

    val clientName: String get() = identity?.clientName ?: "Preparing phone identity…"

    val identityStorage: String
        get() = when (identity?.storage) {
            ClientIdentity.Storage.ANDROID_KEYSTORE -> "Android Keystore"
            ClientIdentity.Storage.SOFTWARE -> "app-private software key"
            null -> "preparing…"
        }

    private var remoteClient: RemoteClient? = null
    private var collectorJob: Job? = null
    private var connectJob: Job? = null
    private var catalogJob: Job? = null
    private var statusJob: Job? = null
    private var pairingJob: Job? = null
    private var pendingPin: CompletableDeferred<String>? = null
    private var pairingDevice: TvDevice? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private var voiceJob: Job? = null
    private var voiceTimeoutJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var voiceSessionId: Int? = null

    init {
        discovery.start()
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = ClientIdentityProvider.getOrCreate(getApplication())
            identity = loaded
            _identityReady.value = true
            withContext(Dispatchers.Main) { autoConnect() }
        }
    }

    private fun autoConnect() {
        store.lastDeviceId
            ?.let { id -> _pairedDevices.value.firstOrNull { it.id == id } }
            ?.takeIf { it.isPaired }
            ?.let { connect(it) }
    }

    fun openDevice(device: TvDevice) {
        if (device.isPaired) connect(device) else pair(device)
    }

    fun rescan() {
        discovery.stop()
        discovery.start()
    }

    fun pair(device: TvDevice) {
        if (pairingJob?.isActive == true) return
        val id = identity ?: return
        pairingDevice = device
        pairingJob = viewModelScope.launch {
            _pairing.value = PairingState.Connecting(device)
            val deferred = CompletableDeferred<String>()
            pendingPin = deferred
            try {
                val result = PairingClient().pair(device, id) {
                    _pairing.value = PairingState.NeedsPin(device)
                    withTimeout(120_000) { deferred.await() }
                }
                val serverName = result.serverName
                    .substringAfter("CN=", "")
                    .substringBefore(",")
                    .trim()
                // Android TV's self-signed cert carries ATVREMOTE/<mac>, which is not a
                // name anyone wants to see. Keep the discovered/manual name until the
                // catalog reports the TV's real device name ("Main TV").
                val friendlyName = serverName.takeUnless {
                    it.isBlank() ||
                        it.startsWith("ATVREMOTE", ignoreCase = true) ||
                        it.matches(Regex("^[0-9A-Fa-f:]{17}$"))
                } ?: device.name
                val paired = device.copy(
                    name = friendlyName,
                    serverFingerprint = result.serverFingerprint,
                    lastConnectedAt = System.currentTimeMillis(),
                )
                store.upsert(paired)
                store.lastDeviceId = paired.id
                refreshPaired()
                _pairing.value = PairingState.Idle
                connect(paired)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _pairing.value = PairingState.Failed(
                    device,
                    e.message ?: "Pairing failed. Try again to get a new code from the TV.",
                )
            } finally {
                pendingPin = null
            }
        }
    }

    fun submitPin(pin: String) {
        val state = _pairing.value
        if (state !is PairingState.NeedsPin) return
        if (!PairingCrypto.validatePin(pin)) {
            _pairing.value = state.copy(error = "Use the six hexadecimal characters shown on the TV")
            return
        }
        _pairing.value = PairingState.Verifying(state.device)
        pendingPin?.complete(PairingCrypto.normalizePin(pin))
    }

    fun cancelPairing() {
        pairingJob?.cancel()
        pairingJob = null
        pendingPin = null
        pairingDevice = null
        _pairing.value = PairingState.Idle
    }

    fun retryPairing() {
        val device = pairingDevice ?: return
        pairingJob = null
        pair(device)
    }

    fun pairManual(host: String) {
        val clean = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        if (clean.isBlank()) return
        // mDNS already knows the user-facing name and keeps the same identity if the
        // TV's IP changes; use it when the address matches.
        val discovered = discoveredDevices.value.firstOrNull { it.host == clean }
        pair(
            discovered ?: TvDevice(
                id = "manual:$clean",
                name = "Android TV",
                host = clean,
            ),
        )
    }

    fun connect(device: TvDevice) {
        val id = identity ?: return
        if (connectJob?.isActive == true && _activeDevice.value?.id == device.id) return
        stopVoice()
        reconnectJob?.cancel()
        reconnectJob = null
        _reconnecting.value = false
        statusJob?.cancel()
        statusJob = null
        _nowPlaying.value = null
        connectJob?.cancel()
        connectJob = null
        remoteClient?.disconnect()
        collectorJob?.cancel()
        _activeDevice.value = device
        store.lastDeviceId = device.id
        store.touch(device.id)
        refreshPaired()

        val client = RemoteClient(viewModelScope, id, device)
        remoteClient = client
        collectorJob?.cancel()
        collectorJob = viewModelScope.launch {
            launch {
                client.state.collect { state ->
                    _remoteState.value = state
                    when (state) {
                        is RemoteClient.State.Connected -> {
                            reconnectAttempt = 0
                            _reconnecting.value = false
                        }
                        is RemoteClient.State.Error ->
                            if (_activeDevice.value?.id == device.id) scheduleReconnect(device)
                        else -> Unit
                    }
                }
            }
            launch { client.volume.collect { _volume.value = it } }
            launch { client.currentApp.collect { _currentApp.value = it } }
            launch { client.isOn.collect { _isOn.value = it } }
        }
        connectJob = viewModelScope.launch {
            if (client.connect()) {
                refreshCatalog(device.host)
                startNowPlayingPolling(device.host)
            }
        }
    }

    private fun startNowPlayingPolling(host: String) {
        statusJob?.cancel()
        statusJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val now = TvCatalogClient.fetchNowPlaying(host)
                withContext(Dispatchers.Main) { _nowPlaying.value = now }
                delay(5_000)
            }
        }
    }

    private fun scheduleReconnect(device: TvDevice) {
        if (reconnectJob?.isActive == true) return
        _reconnecting.value = true
        reconnectJob = viewModelScope.launch {
            val index = reconnectAttempt.coerceAtMost(RECONNECT_DELAYS.lastIndex)
            reconnectAttempt = (index + 1).coerceAtMost(RECONNECT_DELAYS.lastIndex)
            delay(RECONNECT_DELAYS[index])
            if (_activeDevice.value?.id != device.id) return@launch
            if (_remoteState.value is RemoteClient.State.Connected) return@launch
            // Let a still-running connect attempt finish before starting another one.
            connectJob?.join()
            if (_activeDevice.value?.id != device.id) return@launch
            if (_remoteState.value is RemoteClient.State.Connected) return@launch
            reconnectJob = null
            connect(device)
        }
    }

    fun reconnect() {
        reconnectAttempt = 0
        reconnectJob?.cancel()
        reconnectJob = null
        connectJob?.cancel()
        connectJob = null
        remoteClient?.disconnect()
        _activeDevice.value?.let { connect(it) }
    }

    fun onResume() {
        if (!_identityReady.value) return
        val device = _activeDevice.value ?: return
        when (_remoteState.value) {
            is RemoteClient.State.Disconnected, is RemoteClient.State.Error -> connect(device)
            else -> Unit
        }
    }

    fun disconnect() {
        reconnectAttempt = 0
        _reconnecting.value = false
        stopVoice()
        reconnectJob?.cancel()
        reconnectJob = null
        statusJob?.cancel()
        statusJob = null
        _nowPlaying.value = null
        connectJob?.cancel()
        connectJob = null
        remoteClient?.disconnect()
        remoteClient = null
        collectorJob?.cancel()
        collectorJob = null
        _remoteState.value = RemoteClient.State.Disconnected
    }

    fun switchToDevices() {
        disconnect()
        _activeDevice.value = null
    }

    fun sendKey(key: RemoteKey) {
        remoteClient?.sendKey(key)
    }

    fun sendKeyLongStart(key: RemoteKey) {
        remoteClient?.sendKey(key, RemoteDirection.START_LONG)
    }

    fun sendKeyLongEnd(key: RemoteKey) {
        remoteClient?.sendKey(key, RemoteDirection.END_LONG)
    }

    fun toggleFavorite(app: TvApp) {
        val favorites = store.toggleFavorite(app.packageName)
        _apps.value = _apps.value.map {
            it.copy(favorite = it.packageName in favorites)
        }
    }

    fun sendText(text: String) {
        remoteClient?.sendText(text)
    }

    fun sendTextAndEnter(text: String) {
        remoteClient?.sendText(text)
        remoteClient?.sendKey(RemoteKey.Enter)
    }

    fun clearText() {
        repeat(40) { remoteClient?.sendKey(RemoteKey.Delete) }
    }

    fun startVoiceSearch() {
        val client = remoteClient ?: run {
            _voiceState.value = VoiceState.Error("Connect to the TV first")
            return
        }
        if (_voiceState.value is VoiceState.Starting || _voiceState.value is VoiceState.Recording) return
        stopVoice()
        _voiceState.value = VoiceState.Starting
        voiceJob = viewModelScope.launch {
            val session = client.startVoiceSession()
            if (session == null) {
                _voiceState.value = VoiceState.Error("VOICE SEARCH NOT AVAILABLE")
                return@launch
            }
            val record = try {
                createAudioRecord()
            } catch (_: Exception) {
                client.endVoice(session)
                _voiceState.value = VoiceState.Error("MICROPHONE UNAVAILABLE")
                return@launch
            }
            audioRecord = record
            voiceSessionId = session
            try {
                record.startRecording()
                if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                    throw IllegalStateException("Microphone did not start")
                }
                _voiceState.value = VoiceState.Recording
                voiceTimeoutJob = viewModelScope.launch {
                    delay(20_000)
                    stopVoice()
                }
                withContext(Dispatchers.IO) {
                    val buffer = ByteArray(VOICE_CHUNK_SIZE)
                    while (isActive) {
                        val read = record.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            client.sendVoiceChunk(buffer.copyOf(read), session)
                        } else if (read < 0) {
                            break
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _voiceState.value = VoiceState.Error("MICROPHONE UNAVAILABLE")
            } finally {
                releaseAudio()
                if (voiceSessionId == session) voiceSessionId = null
                client.endVoice(session)
                if (_voiceState.value !is VoiceState.Error) _voiceState.value = VoiceState.Idle
            }
        }
    }

    fun stopVoice() {
        voiceTimeoutJob?.cancel()
        voiceTimeoutJob = null
        val record = audioRecord
        runCatching {
            if (record?.recordingState == AudioRecord.RECORDSTATE_RECORDING) record.stop()
        }
        voiceJob?.cancel()
        voiceJob = null
        _voiceState.value = VoiceState.Idle
    }

    fun clearVoiceError() {
        if (_voiceState.value is VoiceState.Error) _voiceState.value = VoiceState.Idle
    }

    private fun createAudioRecord(): AudioRecord {
        val min = AudioRecord.getMinBufferSize(
            8_000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val record = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(8_000)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(min, 16_384))
            .build()
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            throw IllegalStateException("AudioRecord not initialized")
        }
        return record
    }

    private fun releaseAudio() {
        val record = audioRecord ?: return
        audioRecord = null
        runCatching {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) record.stop()
        }
        runCatching { record.release() }
    }

    fun launchPackage(packageName: String) {
        remoteClient?.launchPackage(packageName)
    }

    fun launchApp(app: TvApp) {
        _recentPackages.value = store.recordRecent(app.packageName)
        if (app.packageName == "com.netflix.ninja") {
            launchDial("Netflix")
        } else {
            launchPackage(app.packageName)
        }
    }

    fun refreshCatalog() {
        refreshCatalog(_activeDevice.value?.host)
    }

    private fun refreshCatalog(host: String?) {
        if (host.isNullOrBlank()) return
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch(Dispatchers.IO) {
            // A TV that just booted or just had its launcher updated may not be listening
            // for the first second; one retry avoids showing the bundled fallback.
            val catalog = TvCatalogClient.fetch(host)
                ?: run {
                    delay(1_500)
                    TvCatalogClient.fetch(host)
                }
                ?: return@launch
            val remote = catalog.apps
            val merged = remote.map { app ->
                app.copy(
                    favorite = app.packageName in store.favoritePackages,
                )
            }
            val context = getApplication<Application>().applicationContext
            withContext(Dispatchers.Main) {
                _apps.value = merged
                catalog.deviceName?.let { applyDeviceName(host, it) }
            }
            merged.forEach { app ->
                AppIconCache.ensure(context, host, app.packageName)
            }
        }
    }

    private fun applyDeviceName(host: String, name: String) {
        val current = _activeDevice.value ?: return
        if (current.host != host || current.name == name) return
        val renamed = current.copy(name = name)
        store.upsert(renamed)
        _activeDevice.value = renamed
        refreshPaired()
    }

    fun launchDial(appName: String) {
        val host = _activeDevice.value?.host ?: return
        viewModelScope.launch {
            DialClient.launch(host, appName)
        }
    }

    fun sendAppLink(url: String) {
        remoteClient?.sendAppLink(url)
    }

    fun forget(device: TvDevice) {
        if (_activeDevice.value?.id == device.id) switchToDevices()
        store.forget(device.id)
        refreshPaired()
    }

    fun setHaptics(enabled: Boolean) {
        _haptics.value = enabled
        store.haptics = enabled
    }

    fun setLeftHanded(enabled: Boolean) {
        _leftHanded.value = enabled
        store.leftHanded = enabled
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        store.keepScreenOn = enabled
    }

    override fun onCleared() {
        discovery.stop()
        catalogJob?.cancel()
        statusJob?.cancel()
        reconnectJob?.cancel()
        stopVoice()
        remoteClient?.disconnect()
        super.onCleared()
    }

    private fun refreshPaired() {
        _pairedDevices.value = store.devices()
    }

    private companion object {
        val RECONNECT_DELAYS = longArrayOf(1_000L, 2_000L, 5_000L, 10_000L, 20_000L, 30_000L)
        const val VOICE_CHUNK_SIZE = 8 * 1024
    }
}
