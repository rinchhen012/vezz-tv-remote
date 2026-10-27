package com.rinchhen.vezzremote.data

enum class AppCategory(val title: String) {
    Media("Media"),
    Tools("Tools"),
}

/**
 * One launchable TV app.
 *
 * [iconRes] is filled from the bundled catalog for now. A future TV catalog endpoint
 * can return packages + labels + icon PNG/WebP bytes, which will replace the fallback
 * letter tiles without changing this model.
 */
data class TvApp(
    val packageName: String,
    val label: String,
    val category: AppCategory = AppCategory.Media,
    val favorite: Boolean = false,
    val iconRes: Int? = null,
)

/**
 * Bundled fallback catalog. The full TV app list and real icons are loaded from the TV
 * catalog when it is available; these entries keep the app usable offline.
 */
object AppCatalog {
    val apps: List<TvApp> = listOf(
        TvApp("com.netflix.ninja", "Netflix", AppCategory.Media, favorite = true),
        TvApp("org.smarttube.stable", "SmartTube", AppCategory.Media, favorite = true),
        TvApp("com.amazon.amazonvideo.livingroom", "Prime Video", AppCategory.Media, favorite = true),
        TvApp("com.google.android.youtube.tv", "YouTube", AppCategory.Media, favorite = true),
        TvApp("com.disney.disneyplus", "Disney+", AppCategory.Media, favorite = true),
        TvApp("org.xbmc.kodi", "Kodi", AppCategory.Media, favorite = true),
        TvApp("com.android.tv.settings", "TV Settings", AppCategory.Tools),
        TvApp("com.android.vending", "Play Store", AppCategory.Tools),
        TvApp("com.gathin.tvhome", "VEZZ Home", AppCategory.Tools),
    )

    val favorites: List<TvApp> = apps.filter { it.favorite }
}
