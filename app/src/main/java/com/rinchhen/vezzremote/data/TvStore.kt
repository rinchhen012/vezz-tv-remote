package com.rinchhen.vezzremote.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small synchronous store for paired TVs and preferences.
 *
 * The list is tiny, so SharedPreferences + JSON keeps the app dependency-free and
 * makes the state easy to inspect while developing.
 */
class TvStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun devices(): List<TvDevice> = synchronized(this) {
        val raw = prefs.getString(KEY_DEVICES, null) ?: return emptyList()
        runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    add(obj.toDevice())
                }
            }.sortedByDescending { it.lastConnectedAt }
        }.getOrDefault(emptyList())
    }

    fun upsert(device: TvDevice): List<TvDevice> = synchronized(this) {
        val current = devices()
        val existing = current.firstOrNull { it.id == device.id }
        val merged = device.copy(
            // Discovery does not know the fingerprint; keep the paired one.
            serverFingerprint = device.serverFingerprint ?: existing?.serverFingerprint,
            lastConnectedAt = maxOf(device.lastConnectedAt, existing?.lastConnectedAt ?: 0L),
        )
        val updated = current.filterNot { it.id == device.id } + merged
        write(updated)
        updated.sortedByDescending { it.lastConnectedAt }
    }

    fun markPaired(id: String, fingerprint: String): TvDevice? = synchronized(this) {
        val current = devices()
        val device = current.firstOrNull { it.id == id } ?: return null
        val updated = device.copy(serverFingerprint = fingerprint, lastConnectedAt = System.currentTimeMillis())
        write(current.filterNot { it.id == id } + updated)
        updated
    }

    fun touch(id: String) {
        synchronized(this) {
            val current = devices()
            val device = current.firstOrNull { it.id == id } ?: return
            write(current.filterNot { it.id == id } + device.copy(lastConnectedAt = System.currentTimeMillis()))
        }
    }

    fun forget(id: String) {
        synchronized(this) {
            write(devices().filterNot { it.id == id })
            if (prefs.getString(KEY_LAST_DEVICE, null) == id) {
                prefs.edit().remove(KEY_LAST_DEVICE).apply()
            }
        }
    }

    var lastDeviceId: String?
        get() = prefs.getString(KEY_LAST_DEVICE, null)
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_LAST_DEVICE) else putString(KEY_LAST_DEVICE, value)
            }.apply()
        }

    var haptics: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var leftHanded: Boolean
        get() = prefs.getBoolean(KEY_LEFT_HANDED, false)
        set(value) = prefs.edit().putBoolean(KEY_LEFT_HANDED, value).apply()

    var repeatDelayMs: Long
        get() = prefs.getLong(KEY_REPEAT_DELAY, 90L)
        set(value) = prefs.edit().putLong(KEY_REPEAT_DELAY, value.coerceIn(45L, 250L)).apply()

    private fun write(devices: List<TvDevice>) {
        val array = JSONArray()
        devices.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_DEVICES, array.toString()).apply()
    }

    private fun TvDevice.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("host", host)
        put("pairingPort", pairingPort)
        put("remotePort", remotePort)
        put("serverFingerprint", serverFingerprint ?: JSONObject.NULL)
        put("lastConnectedAt", lastConnectedAt)
    }

    private fun JSONObject.toDevice(): TvDevice = TvDevice(
        id = optString("id"),
        name = optString("name", optString("host")),
        host = optString("host"),
        pairingPort = optInt("pairingPort", TvDevice.DEFAULT_PAIRING_PORT),
        remotePort = optInt("remotePort", TvDevice.DEFAULT_REMOTE_PORT),
        serverFingerprint = if (isNull("serverFingerprint")) null else optString("serverFingerprint").ifBlank { null },
        lastConnectedAt = optLong("lastConnectedAt", 0L),
    )

    private companion object {
        const val PREFS = "vezz_remote"
        const val KEY_DEVICES = "devices"
        const val KEY_LAST_DEVICE = "last_device"
        const val KEY_HAPTICS = "haptics"
        const val KEY_LEFT_HANDED = "left_handed"
        const val KEY_REPEAT_DELAY = "repeat_delay"
    }
}
