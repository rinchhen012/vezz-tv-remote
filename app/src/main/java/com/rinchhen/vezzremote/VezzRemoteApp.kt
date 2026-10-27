package com.rinchhen.vezzremote

import android.app.Application
import android.content.Context
import com.rinchhen.vezzremote.data.TvStore
import com.rinchhen.vezzremote.discovery.TvDiscovery
import com.rinchhen.vezzremote.protocol.RemoteClient

class VezzRemoteApp : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val store = TvStore(context)
    val discovery = TvDiscovery(context)
}

/** One screen at a time; the app is small enough that Navigation Compose is unnecessary. */
enum class Screen { Devices, Remote, Settings }

/** The subset of [RemoteClient] state the settings UI needs after a session is gone. */
val RemoteClient.State.isConnected: Boolean
    get() = this is RemoteClient.State.Connected
