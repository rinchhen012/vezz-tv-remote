package com.rinchhen.vezzremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink0
import com.rinchhen.vezzremote.ui.theme.Ink1
import com.rinchhen.vezzremote.ui.theme.Ink2
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.LineStrong
import com.rinchhen.vezzremote.ui.theme.OnAcid
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperFaint
import com.rinchhen.vezzremote.ui.theme.PaperMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, Line, shape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(
        modifier = clickable.padding(contentPadding),
        content = content,
    )
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Line),
    )
}

@Composable
fun PageTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = Paper,
        )
        Text(
            subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = PaperMuted,
        )
        Hairline(modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
                modifier = Modifier.weight(1f),
            )
            action?.invoke(this)
        }
        Hairline()
    }
}

@Composable
fun MonoTag(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    danger: Boolean = false,
) {
    val color = when {
        danger -> MaterialTheme.colorScheme.error
        accent -> Acid
        else -> PaperMuted
    }
    Text(
        text.uppercase(),
        modifier = modifier
            .border(1.dp, if (accent) Acid.copy(alpha = 0.55f) else Line, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
    )
}

@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    haptics: Boolean = true,
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(12.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, animationSpec = tween(120), label = "accent-button")
    val background = if (enabled) Acid else Ink2
    val content = if (enabled) OnAcid else PaperFaint

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            }
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    haptics: Boolean = true,
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(12.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, animationSpec = tween(120), label = "outline-button")
    val border by animateColorAsState(if (pressed) Acid else LineStrong, animationSpec = tween(120), label = "outline-border")
    val content = if (enabled) Paper else PaperFaint

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(1.dp, border, shape)
            .background(Ink1)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            }
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun Keycap(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    haptics: Boolean = true,
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(10.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val background by animateColorAsState(if (pressed) Ink1 else Ink2, animationSpec = tween(120), label = "keycap-bg")
    val border by animateColorAsState(if (pressed) Acid else Line, animationSpec = tween(120), label = "keycap-border")
    val content = if (enabled) Paper else PaperFaint

    Row(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(17.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RoundKey(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    accent: Boolean = false,
    haptics: Boolean = true,
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, animationSpec = tween(120), label = "round-key")
    val background = if (accent) Acid else Ink2
    val content = if (accent) OnAcid else Paper
    val border = if (accent) Acid else LineStrong

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
            ) {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = content,
            modifier = Modifier.size(size * 0.4f),
        )
    }
}

@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 6.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
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
    val resolvedApp = app ?: AppCatalog.apps.firstOrNull { it.packageName == packageName }
    val resolvedLabel = resolvedApp?.label ?: label ?: labelFromPackage(packageName)
    val resolvedPackage = resolvedApp?.packageName ?: packageName.orEmpty()
    val shape = RoundedCornerShape(12.dp)
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
        val bmp = bitmap
        val iconRes = resolvedApp?.iconRes
        when {
            bmp != null -> Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = resolvedLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size)
                    .clip(shape),
            )

            iconRes != null -> Image(
                painter = painterResource(iconRes),
                contentDescription = resolvedLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size)
                    .clip(shape),
            )

            else -> Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .background(Ink2)
                    .border(1.dp, Line, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials(resolvedLabel),
                    color = PaperMuted,
                    fontFamily = com.rinchhen.vezzremote.ui.theme.PlexMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.value * 0.28f).sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
        if (showRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.18f)
                    .clip(CircleShape)
                    .background(Acid)
                    .border(2.dp, Ink0, CircleShape),
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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        AppIcon(
            app = app,
            packageName = app.packageName,
            label = app.label,
            size = 58.dp,
            showRunning = running,
        )
        Text(
            text = app.label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (running) Acid else Paper,
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
            .width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        AppIcon(app, app.packageName, app.label, size = 44.dp, showRunning = running)
        Text(
            app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (running) Acid else PaperMuted,
        )
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
