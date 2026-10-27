package com.rinchhen.vezzremote.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.rinchhen.vezzremote.data.TvDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Finds TVs advertising Google's Remote v2 service: _androidtvremote2._tcp.
 *
 * NsdManager resolves one service at a time on some Android versions, so resolves are
 * queued and de-duplicated. Manual IP entry in the UI covers networks that block mDNS.
 */
class TvDiscovery(context: Context) {
    private val appContext = context.applicationContext
    private val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _devices = MutableStateFlow<List<TvDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private var multicastLock: WifiManager.MulticastLock? = null
    private var listener: NsdManager.DiscoveryListener? = null
    private val resolving = mutableSetOf<String>()

    @Synchronized
    fun start() {
        if (listener != null) return
        val manager = nsdManager ?: return

        multicastLock = wifiManager?.createMulticastLock("vezz-remote-mdns")?.apply {
            setReferenceCounted(false)
            acquire()
        }

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                stop()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                val name = serviceInfo.serviceName ?: return
                if (name in resolving || _devices.value.any { it.id == idFor(name) }) return
                resolving += name
                runCatching {
                    manager.resolveService(
                        serviceInfo,
                        object : NsdManager.ResolveListener {
                            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                                resolving -= name
                            }

                            override fun onServiceResolved(info: NsdServiceInfo) {
                                resolving -= name
                                val host = info.host?.hostAddress ?: return
                                val remotePort = info.port.takeIf { it > 0 }
                                    ?: TvDevice.DEFAULT_REMOTE_PORT
                                upsert(
                                    TvDevice(
                                        id = idFor(info.serviceName),
                                        name = info.serviceName,
                                        host = host,
                                        remotePort = remotePort,
                                        pairingPort = remotePort + 1,
                                    ),
                                )
                            }
                        },
                    )
                }.onFailure {
                    resolving -= name
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                val name = serviceInfo.serviceName ?: return
                _devices.value = _devices.value.filterNot { it.id == idFor(name) }
            }
        }

        listener = discoveryListener
        runCatching {
            manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }.onFailure {
            stop()
        }
    }

    @Synchronized
    fun stop() {
        listener?.let { active ->
            runCatching { nsdManager?.stopServiceDiscovery(active) }
        }
        listener = null
        resolving.clear()
        multicastLock?.let {
            runCatching { if (it.isHeld) it.release() }
        }
        multicastLock = null
    }

    private fun upsert(device: TvDevice) {
        val current = _devices.value
        _devices.value = (current.filterNot { it.id == device.id } + device)
            .sortedBy { it.name.lowercase() }
    }

    private fun idFor(serviceName: String): String = "mdns:$serviceName"

    private companion object {
        const val SERVICE_TYPE = "_androidtvremote2._tcp"
    }
}
