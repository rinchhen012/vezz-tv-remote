package com.rinchhen.vezzremote.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Phone side of the optional TV app catalog.
 *
 * The Android TV Remote protocol does not expose the installed app list or icons.
 * The CleanYourTV launcher (or a small companion service) can serve:
 *
 *   GET http://<tv>:8080/catalog
 *   {"apps":[{"package":"com.netflix.ninja","label":"Netflix","category":"Media"}]}
 *
 *   GET http://<tv>:8080/icon/<package>
 *   PNG/WebP bytes
 *
 * Everything here fails soft: if the endpoint is absent, the bundled AppCatalog is
 * used and the app remains fully usable.
 */
object TvCatalogClient {
    const val PORT = 8080

    suspend fun fetch(host: String, timeoutMs: Int = 2_500): List<TvApp>? = withContext(Dispatchers.IO) {
        val body = get("http://$host:$PORT/catalog", timeoutMs) ?: return@withContext null
        runCatching { parse(body) }.getOrNull()
    }

    suspend fun fetchIcon(host: String, packageName: String, timeoutMs: Int = 3_000): ByteArray? =
        withContext(Dispatchers.IO) {
            val connection = runCatching {
                URL("http://$host:$PORT/icon/$packageName").openConnection() as HttpURLConnection
            }.getOrNull() ?: return@withContext null

            try {
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                if (connection.responseCode !in 200..299) return@withContext null
                connection.inputStream.use { it.readBytes() }
            } catch (_: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }

    private fun get(url: String, timeoutMs: Int): String? {
        val connection = runCatching {
            URL(url).openConnection() as HttpURLConnection
        }.getOrNull() ?: return null

        return try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String): List<TvApp> {
        val root = runCatching { JSONObject(body) }.getOrNull()
        val array = root?.optJSONArray("apps") ?: JSONArray(body)
        val result = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val packageName = item.optString("package")
                if (packageName.isNotBlank()) {
                    val label = item.optString("label").ifBlank { packageName.substringAfterLast('.') }
                    val category = when (item.optString("category").lowercase()) {
                        "tools", "tool", "system" -> AppCategory.Tools
                        else -> AppCategory.Media
                    }
                    add(
                        TvApp(
                            packageName = packageName,
                            label = label,
                            category = category,
                            favorite = AppCatalog.favorites.any { it.packageName == packageName },
                        ),
                    )
                }
            }
        }
        return result.sortedBy { it.label.lowercase() }
    }
}
