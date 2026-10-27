package com.rinchhen.vezzremote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VezzColors = darkColorScheme(
    primary = Color(0xFF8B7CFF),
    onPrimary = Color(0xFF0B0B10),
    primaryContainer = Color(0xFF2B2650),
    onPrimaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    background = Color(0xFF0B0B10),
    onBackground = Color(0xFFF4F2FF),
    surface = Color(0xFF15151D),
    onSurface = Color(0xFFF4F2FF),
    surfaceVariant = Color(0xFF23232E),
    onSurfaceVariant = Color(0xFFB9B5CC),
    outline = Color(0xFF4A475C),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0505),
)

@Composable
fun VezzRemoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VezzColors,
        content = content,
    )
}
