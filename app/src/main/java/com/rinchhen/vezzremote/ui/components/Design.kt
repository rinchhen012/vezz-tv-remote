package com.rinchhen.vezzremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinchhen.vezzremote.data.AppCatalog
import com.rinchhen.vezzremote.data.AppIconCache
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.ui.theme.VezzGradients
import com.rinchhen.vezzremote.ui.theme.VezzPrimary
import com.rinchhen.vezzremote.ui.theme.VezzTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                ),
            ),
        )
        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), shape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(
        modifier = clickable.padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}

@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, color.copy(alpha = 0.22f), CircleShape),
    )
}

@Composable
fun StatPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(20.dp)
    val contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .background(if (enabled) VezzGradients.primary else Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
            )))
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = contentColor)
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    gradient: Brush? = null,
    haptics: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "round-button")
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .then(
                if (gradient != null) Modifier.background(gradient) else Modifier.background(container),
            )
            .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
            ) {
                if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(size * 0.42f),
        )
    }
}

@Composable
fun AppIcon(
    app: TvApp?,
    packageName: String?,
    label: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    showRunning: Boolean = false,
) {
    val resolvedApp = app
        ?: AppCatalog.apps.firstOrNull { it.packageName == packageName }
    val resolvedLabel = resolvedApp?.label
        ?: label
        ?: labelFromPackage(packageName)
    val resolvedPackage = resolvedApp?.packageName ?: packageName.orEmpty()
    val colors = appColors(resolvedPackage)
    val shape = RoundedCornerShape(size * 0.28f)
    val context = LocalContext.current
    val iconVersion by AppIconCache.version.collectAsState()
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = resolvedPackage,
        key2 = iconVersion,
    ) {
        value = if (resolvedPackage.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { AppIconCache.load(context, resolvedPackage) }
        }
    }

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(Brush.linearGradient(colors))
                .border(1.dp, Color.White.copy(alpha = 0.14f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                        ),
                    ),
            )
            val bmp = bitmap
            val iconRes = resolvedApp?.iconRes
            when {
                bmp != null -> Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = resolvedLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(size * 0.64f),
                )

                iconRes != null -> Image(
                    painter = painterResource(iconRes),
                    contentDescription = resolvedLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(size * 0.62f),
                )

                else -> Text(
                    text = initials(resolvedLabel),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.32f).sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
        if (showRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.22f)
                    .clip(CircleShape)
                    .background(VezzTeal)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

@Composable
fun AppTile(
    app: TvApp,
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppIcon(
            app = app,
            packageName = app.packageName,
            label = app.label,
            size = 60.dp,
            showRunning = running,
        )
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (running) VezzTeal else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun AppIconLabel(
    app: TvApp,
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AppIcon(app, app.packageName, app.label, size = 50.dp, showRunning = running)
        Text(
            app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (running) VezzTeal else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SmallActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun labelFromPackage(packageName: String?): String {
    if (packageName.isNullOrBlank()) return "TV"
    return when (packageName) {
        "com.netflix.ninja" -> "Netflix"
        "org.smarttube.stable" -> "SmartTube"
        "com.amazon.amazonvideo.livingroom" -> "Prime Video"
        "com.google.android.youtube.tv" -> "YouTube"
        "com.disney.disneyplus" -> "Disney+"
        "org.xbmc.kodi" -> "Kodi"
        "com.android.tv.settings" -> "TV Settings"
        "com.android.vending" -> "Play Store"
        "com.gathin.tvhome" -> "VEZZ Home"
        else -> packageName.substringAfterLast('.').replace('_', ' ').replace('-', ' ')
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}

private fun initials(label: String): String {
    val parts = label.split(' ', '+', '&', '-').filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        parts.take(2).joinToString("") { it.first().uppercase() }
    } else {
        label.take(2).uppercase()
    }
}

private fun appColors(packageName: String): List<Color> = when (packageName) {
    "com.netflix.ninja" -> listOf(Color(0xFFE50914), Color(0xFF7A0008))
    "org.smarttube.stable" -> listOf(Color(0xFFFF0033), Color(0xFF5C0014))
    "com.amazon.amazonvideo.livingroom" -> listOf(Color(0xFF00A8E1), Color(0xFF005F7F))
    "com.google.android.youtube.tv" -> listOf(Color(0xFFFF0033), Color(0xFFA00020))
    "com.disney.disneyplus" -> listOf(Color(0xFF113CCF), Color(0xFF07184F))
    "org.xbmc.kodi" -> listOf(Color(0xFF17B2E8), Color(0xFF0B4F66))
    "com.android.tv.settings" -> listOf(Color(0xFF6B7280), Color(0xFF252A36))
    "com.android.vending" -> listOf(Color(0xFF34A853), Color(0xFF0B4F2A))
    "com.gathin.tvhome" -> listOf(Color(0xFF8B7CFF), Color(0xFF3B2E80))
    else -> {
        val palettes = listOf(
            listOf(Color(0xFF8B7CFF), Color(0xFF3B2E80)),
            listOf(Color(0xFF2DD4BF), Color(0xFF0F5F58)),
            listOf(Color(0xFFFFB86B), Color(0xFF7A4A12)),
            listOf(Color(0xFFFF6B6B), Color(0xFF6E1A1A)),
            listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)),
        )
        palettes[(packageName.hashCode() and Int.MAX_VALUE) % palettes.size]
    }
}
