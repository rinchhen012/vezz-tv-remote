package com.rinchhen.vezzremote.protocol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * DIAL launch on port 8008. Netflix registers a DIAL app on this TV; the remote
 * protocol's app-link request does not accept market:// package launches.
 */
object DialClient {

    suspend fun launch(host: String, appName: String, timeoutMs: Int = 4_000): Boolean =
        withContext(Dispatchers.IO) {
            val connection = runCatching {
                URL("http://$host:8008/apps/$appName").openConnection() as HttpURLConnection
            }.getOrNull() ?: return@withContext false

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                connection.doOutput = true
                connection.outputStream.use { it.write(ByteArray(0)) }
                connection.responseCode in 200..299
            } catch (_: Exception) {
                false
            } finally {
                connection.disconnect()
            }
        }
}
