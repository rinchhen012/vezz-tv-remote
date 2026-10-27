package com.rinchhen.vezzremote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val VezzBackground = Color(0xFF08080C)
val VezzSurface = Color(0xFF121218)
val VezzSurfaceElevated = Color(0xFF1A1A23)
val VezzPrimary = Color(0xFF8B7CFF)
val VezzPrimaryBright = Color(0xFFB6A8FF)
val VezzTeal = Color(0xFF5EEAD4)
val VezzAmber = Color(0xFFFFB86B)
val VezzRed = Color(0xFFFF6B6B)

private val VezzColors = darkColorScheme(
    primary = VezzPrimary,
    onPrimary = Color(0xFF100E1F),
    primaryContainer = Color(0xFF2B2650),
    onPrimaryContainer = Color(0xFFEAE5FF),
    secondary = VezzTeal,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF123B38),
    onSecondaryContainer = Color(0xFFC9FFF8),
    tertiary = VezzAmber,
    onTertiary = Color(0xFF2A1802),
    background = VezzBackground,
    onBackground = Color(0xFFF5F3FF),
    surface = VezzSurface,
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = VezzSurfaceElevated,
    onSurfaceVariant = Color(0xFFB8B4C8),
    outline = Color(0xFF343441),
    outlineVariant = Color(0xFF24242E),
    error = VezzRed,
    onError = Color(0xFF2A0505),
    errorContainer = Color(0xFF3B1418),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val VezzTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.4.sp,
    ),
)

private val VezzShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

object VezzGradients {
    val primary = Brush.linearGradient(listOf(Color(0xFF8B7CFF), Color(0xFFC0B4FF)))
    val primarySoft = Brush.linearGradient(
        listOf(Color(0xFF8B7CFF).copy(alpha = 0.22f), Color(0xFF8B7CFF).copy(alpha = 0.04f)),
    )
    val teal = Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF7CF5E7)))
    val power = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFA36B)))
    val glow = Brush.radialGradient(
        colors = listOf(Color(0xFF8B7CFF).copy(alpha = 0.16f), Color.Transparent),
    )
}

@Composable
fun VezzRemoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VezzColors,
        typography = VezzTypography,
        shapes = VezzShapes,
        content = content,
    )
}
