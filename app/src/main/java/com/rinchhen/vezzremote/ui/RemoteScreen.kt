package com.rinchhen.vezzremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.components.ActionGrid
import com.rinchhen.vezzremote.ui.components.AppIcon
import com.rinchhen.vezzremote.ui.components.Dpad
import com.rinchhen.vezzremote.ui.components.FavoriteAppsRow
import com.rinchhen.vezzremote.ui.components.GhostButton
import com.rinchhen.vezzremote.ui.components.GlassCard
import com.rinchhen.vezzremote.ui.components.GradientButton
import com.rinchhen.vezzremote.ui.components.HdmiRow
import com.rinchhen.vezzremote.ui.components.KeyboardSheet
import com.rinchhen.vezzremote.ui.components.MediaControls
import com.rinchhen.vezzremote.ui.components.SectionHeader
import com.rinchhen.vezzremote.ui.components.StatPill
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.components.VolumeCard
import com.rinchhen.vezzremote.ui.theme.VezzGradients
import com.rinchhen.vezzremote.ui.theme.VezzTeal

@Composable
fun RemoteContent(
    device: TvDevice?,
    state: RemoteClient.State,
    volume: RemoteClient.VolumeState,
    currentApp: String,
    isOn: Boolean,
    haptics: Boolean,
    leftHanded: Boolean,
    apps: List<TvApp>,
    onKey: (RemoteKey) -> Unit,
    onText: (String) -> Unit,
    onTextAndEnter: (String) -> Unit,
    onClear: () -> Unit,
    onLaunchApp: (TvApp) -> Unit,
    onOpenApps: () -> Unit,
    onReconnect: () -> Unit,
) {
    var showKeyboard by rememberSaveable { mutableStateOf(false) }
    var showPowerConfirm by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val connected = state is RemoteClient.State.Connected
    val current = apps.firstOrNull { it.packageName == currentApp }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 4.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            NowPlayingCard(
                device = device,
                state = state,
                volume = volume,
                current = current,
                currentApp = currentApp,
                isOn = isOn,
                onReconnect = onReconnect,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leftHanded) {
                    Dpad(onKey = onKey, haptics = haptics)
                    VolumeCard(volume = volume, onKey = onKey, haptics = haptics)
                } else {
                    VolumeCard(volume = volume, onKey = onKey, haptics = haptics)
                    Dpad(onKey = onKey, haptics = haptics)
                }
            }
        }

        item {
            ActionGrid(
                onKey = onKey,
                onPower = { showPowerConfirm = true },
                haptics = haptics,
            )
        }

        item {
            MediaControls(onKey = onKey, haptics = haptics)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GradientButton(
                    text = "Keyboard",
                    icon = Icons.Default.Keyboard,
                    onClick = { showKeyboard = true },
                    modifier = Modifier.weight(1f),
                )
                GhostButton(
                    text = "Search",
                    icon = Icons.Default.Search,
                    onClick = {
                        onKey(RemoteKey.Search)
                        showKeyboard = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            FavoriteAppsRow(
                apps = apps.filter { it.favorite },
                currentApp = currentApp,
                onLaunch = onLaunchApp,
                onOpenApps = onOpenApps,
            )
        }

        item {
            SectionHeader(
                title = "Inputs",
                subtitle = "Jump straight to a source",
            )
        }

        item {
            HdmiRow(onKey = onKey)
        }
    }

    if (showKeyboard) {
        KeyboardSheet(
            onDismiss = { showKeyboard = false },
            onSend = onText,
            onSendAndEnter = onTextAndEnter,
            onClear = onClear,
        )
    }

    if (showPowerConfirm) {
        AlertDialog(
            onDismissRequest = { showPowerConfirm = false },
            title = { Text("Turn off the TV?") },
            text = { Text("This sends the power key. Wake-on-LAN is not enabled in this build.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPowerConfirm = false
                        onKey(RemoteKey.Power)
                    },
                ) { Text("Power off") }
            },
            dismissButton = {
                TextButton(onClick = { showPowerConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun NowPlayingCard(
    device: TvDevice?,
    state: RemoteClient.State,
    volume: RemoteClient.VolumeState,
    current: TvApp?,
    currentApp: String,
    isOn: Boolean,
    onReconnect: () -> Unit,
) {
    val connected = state is RemoteClient.State.Connected
    val error = state as? RemoteClient.State.Error
    val disconnected = state is RemoteClient.State.Disconnected
    val label = current?.label ?: appLabel(currentApp)
    val fraction = if (volume.max > 0) (volume.level.toFloat() / volume.max).coerceIn(0f, 1f) else 0f

    GlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (current == null && currentApp.isBlank()) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VezzGradients.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            } else {
                AppIcon(
                    app = current,
                    packageName = currentApp,
                    label = null,
                    size = 62.dp,
                    showRunning = connected,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedContent(
                    targetState = label,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "current-app",
                ) { animatedLabel ->
                    Text(
                        animatedLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusDot(
                        color = when {
                            connected -> VezzTeal
                            error != null -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        when {
                            connected -> if (isOn) "Connected · screen on" else "Connected · standby"
                            error != null -> "Offline"
                            disconnected -> "Not connected"
                            else -> "Connecting…"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (volume.muted) "Muted" else if (volume.level >= 0) "${volume.level}/${volume.max}" else "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (volume.muted) MaterialTheme.colorScheme.error else VezzTeal,
                )
                Text(
                    "volume",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (volume.level >= 0) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                color = VezzTeal,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatPill(device?.name ?: "TV")
            StatPill(if (isOn) "Screen on" else "Standby")
            Spacer(modifier = Modifier.weight(1f))
            if (error != null || disconnected) {
                TextButton(onClick = onReconnect) { Text("Reconnect") }
            }
        }

        if (error != null) {
            Text(
                error.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun appLabel(packageName: String): String = when (packageName) {
    "" -> "Ready"
    "com.netflix.ninja" -> "Netflix"
    "org.smarttube.stable" -> "SmartTube"
    "com.google.android.youtube.tv" -> "YouTube"
    "com.amazon.amazonvideo.livingroom" -> "Prime Video"
    "com.disney.disneyplus" -> "Disney+"
    "org.xbmc.kodi" -> "Kodi"
    "com.android.tv.settings" -> "TV settings"
    "com.gathin.tvhome" -> "VEZZ home"
    else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
}
