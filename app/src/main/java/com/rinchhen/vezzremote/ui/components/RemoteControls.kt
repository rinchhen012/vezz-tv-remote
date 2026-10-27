package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.protocol.RemoteKey

@Composable
fun RemoteIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    haptics: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable {
                if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

@Composable
fun Dpad(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RemoteIconButton(Icons.Default.KeyboardArrowUp, "Up", { onKey(RemoteKey.Up) }, haptics = haptics)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RemoteIconButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Left", { onKey(RemoteKey.Left) }, haptics = haptics)
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKey(RemoteKey.Ok)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "OK",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(34.dp),
                )
            }
            RemoteIconButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Right", { onKey(RemoteKey.Right) }, haptics = haptics)
        }
        RemoteIconButton(Icons.Default.KeyboardArrowDown, "Down", { onKey(RemoteKey.Down) }, haptics = haptics)
    }
}

@Composable
fun VolumeColumn(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("VOL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        RemoteIconButton(Icons.AutoMirrored.Filled.VolumeUp, "Volume up", { onKey(RemoteKey.VolumeUp) }, haptics = haptics)
        RemoteIconButton(Icons.AutoMirrored.Filled.VolumeOff, "Mute", { onKey(RemoteKey.Mute) }, haptics = haptics)
        RemoteIconButton(Icons.AutoMirrored.Filled.VolumeDown, "Volume down", { onKey(RemoteKey.VolumeDown) }, haptics = haptics)
    }
}

@Composable
fun ActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    haptics: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ActionRow(
    onKey: (RemoteKey) -> Unit,
    onSettings: () -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionPill(Icons.AutoMirrored.Filled.ArrowBack, "Back", { onKey(RemoteKey.Back) }, Modifier.weight(1f), haptics)
        ActionPill(Icons.Default.Home, "Home", { onKey(RemoteKey.Home) }, Modifier.weight(1f), haptics)
        ActionPill(Icons.Default.Menu, "Menu", { onKey(RemoteKey.Menu) }, Modifier.weight(1f), haptics)
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionPill(Icons.AutoMirrored.Filled.Input, "Inputs", { onKey(RemoteKey.Input) }, Modifier.weight(1f), haptics)
        ActionPill(Icons.Default.Settings, "TV settings", { onKey(RemoteKey.Settings) }, Modifier.weight(1f), haptics)
        ActionPill(Icons.Default.Menu, "App settings", onSettings, Modifier.weight(1f), haptics)
    }
}

@Composable
fun MediaRow(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteIconButton(Icons.Default.FastRewind, "Rewind", { onKey(RemoteKey.Rewind) }, haptics = haptics)
        RemoteIconButton(
            Icons.Default.PlayArrow,
            "Play or pause",
            { onKey(RemoteKey.PlayPause) },
            size = 78.dp,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            haptics = haptics,
        )
        RemoteIconButton(Icons.Default.FastForward, "Fast forward", { onKey(RemoteKey.FastForward) }, haptics = haptics)
    }
}

private data class Shortcut(
    val label: String,
    val packageName: String? = null,
    val dialName: String? = null,
    val deepLink: String? = null,
)

private val shortcuts = listOf(
    Shortcut("Netflix", dialName = "Netflix"),
    Shortcut("SmartTube", packageName = "org.smarttube.stable"),
    Shortcut("Prime", packageName = "com.amazon.amazonvideo.livingroom"),
    Shortcut("YouTube", packageName = "com.google.android.youtube.tv"),
    Shortcut("Disney+", packageName = "com.disney.disneyplus"),
    Shortcut("Kodi", packageName = "org.xbmc.kodi"),
)

@Composable
fun AppShortcuts(
    onLaunchPackage: (String) -> Unit,
    onDial: (String) -> Unit,
    onSendLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(shortcuts) { shortcut ->
            AssistChip(
                onClick = {
                    shortcut.dialName?.let(onDial)
                        ?: shortcut.deepLink?.let(onSendLink)
                        ?: shortcut.packageName?.let(onLaunchPackage)
                },
                label = { Text(shortcut.label, fontWeight = FontWeight.Medium) },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            )
        }
    }
}
