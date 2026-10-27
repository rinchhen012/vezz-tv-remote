package com.rinchhen.vezzremote

import android.app.Application
import android.content.Context
import com.rinchhen.vezzremote.data.TvStore
import com.rinchhen.vezzremote.discovery.TvDiscovery

class VezzRemoteApp : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val store = TvStore(context)
    val discovery = TvDiscovery(context)
}

/** One screen at a time; the app is small enough that Navigation Compose is unnecessary. */
enum class Screen { Remote, Apps, Devices, Settings }
