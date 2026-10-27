package com.rinchhen.vezzremote.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Disk + memory cache for TV app icons fetched from the catalog endpoint.
 * Files are per-install app-private data and survive app updates.
 */
object AppIconCache {
    private val memory = ConcurrentHashMap<String, Bitmap>()
    val version = MutableStateFlow(0)

    fun load(context: Context, packageName: String): Bitmap? {
        memory[packageName]?.let { return it }
        val file = file(context, packageName)
        if (!file.exists()) return null
        val bitmap = runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() ?: return null
        memory[packageName] = bitmap
        return bitmap
    }

    suspend fun ensure(context: Context, host: String, packageName: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = file(context, packageName)
            if (file.exists() && file.length() > 0) return@withContext true
            val bytes = TvCatalogClient.fetchIcon(host, packageName) ?: return@withContext false
            runCatching {
                file.parentFile?.mkdirs()
                file.writeBytes(bytes)
                memory.remove(packageName)
                version.value += 1
            }.isSuccess
        }

    fun invalidate() {
        memory.clear()
        version.value += 1
    }

    private fun file(context: Context, packageName: String): File {
        val safe = packageName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(File(context.applicationContext.filesDir, "app_icons"), "$safe.png")
    }
}
