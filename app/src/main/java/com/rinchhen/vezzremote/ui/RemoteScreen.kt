package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.data.TvCatalogClient
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.components.AccentButton
import com.rinchhen.vezzremote.ui.components.ActionGrid
import com.rinchhen.vezzremote.ui.components.AppIcon
import com.rinchhen.vezzremote.ui.components.Dpad
import com.rinchhen.vezzremote.ui.components.HdmiRow
import com.rinchhen.vezzremote.ui.components.KeyboardSheet
import com.rinchhen.vezzremote.ui.components.MediaControls
import com.rinchhen.vezzremote.ui.components.MonoTag
import com.rinchhen.vezzremote.ui.components.OutlineButton
import com.rinchhen.vezzremote.ui.components.Panel
import com.rinchhen.vezzremote.ui.components.SectionLabel
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.components.VolumeRocker
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Danger
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperFaint
import com.rinchhen.vezzremote.ui.theme.PaperMuted
import com.rinchhen.vezzremote.ui.theme.PlexMono

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
    nowPlaying: TvCatalogClient.NowPlaying?,
    onKey: (RemoteKey) -> Unit,
    onText: (String) -> Unit,
    onTextAndEnter: (String) -> Unit,
    onClear: () -> Unit,
    onReconnect: () -> Unit,
) {
    var showKeyboard by rememberSaveable { mutableStateOf(false) }
    var showPowerConfirm by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val current = apps.firstOrNull { it.packageName == currentApp }
    val connected = state is RemoteClient.State.Connected
    val error = state as? RemoteClient.State.Error
    val disconnected = state is RemoteClient.State.Disconnected

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            StatusStrip(
                device = device,
                state = state,
                volume = volume,
                current = current,
                currentApp = currentApp,
                isOn = isOn,
                nowPlaying = nowPlaying,
                onReconnect = onReconnect,
            )
        }

        item {
            Panel(contentPadding = PaddingValues(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leftHanded) {
                        Dpad(onKey = onKey, haptics = haptics)
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(220.dp)
                                .background(Line),
                        )
                        VolumeRocker(volume = volume, onKey = onKey, haptics = haptics)
                    } else {
                        VolumeRocker(volume = volume, onKey = onKey, haptics = haptics)
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(220.dp)
                                .background(Line),
                        )
                        Dpad(onKey = onKey, haptics = haptics)
                    }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AccentButton(
                    text = "Keyboard",
                    icon = Icons.Outlined.Keyboard,
                    onClick = { showKeyboard = true },
                    modifier = Modifier.weight(1f),
                    haptics = haptics,
                )
                OutlineButton(
                    text = "Search",
                    icon = Icons.Outlined.Search,
                    onClick = {
                        onKey(RemoteKey.Search)
                        showKeyboard = true
                    },
                    modifier = Modifier.weight(1f),
                    haptics = haptics,
                )
            }
        }

        item {
            SectionLabel(text = "Inputs")
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
            title = { Text("POWER OFF", style = MaterialTheme.typography.labelLarge) },
            text = { Text("Send the power key to the TV?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPowerConfirm = false
                        onKey(RemoteKey.Power)
                    },
                ) {
                    Text("POWER OFF", color = Danger, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPowerConfirm = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelMedium)
                }
            },
        )
    }
}

@Composable
private fun StatusStrip(
    device: TvDevice?,
    state: RemoteClient.State,
    volume: RemoteClient.VolumeState,
    current: TvApp?,
    currentApp: String,
    isOn: Boolean,
    nowPlaying: TvCatalogClient.NowPlaying?,
    onReconnect: () -> Unit,
) {
    val connected = state is RemoteClient.State.Connected
    val error = state as? RemoteClient.State.Error
    val disconnected = state is RemoteClient.State.Disconnected
    val label = current?.label ?: appLabel(currentApp)
    val now = nowPlaying?.title?.takeIf { it.isNotBlank() }
    val nowFraction = if (nowPlaying != null && nowPlaying.durationMs > 0L) {
        (nowPlaying.positionMs.toFloat() / nowPlaying.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    Panel(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (current == null && currentApp.isBlank()) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Line, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Tv, contentDescription = null, tint = PaperMuted, modifier = Modifier.size(22.dp))
                }
            } else {
                AppIcon(
                    app = current,
                    packageName = currentApp,
                    label = null,
                    size = 46.dp,
                    showRunning = connected,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusDot(
                        color = when {
                            connected -> Acid
                            error != null -> Danger
                            else -> PaperFaint
                        },
                    )
                    Text(
                        when {
                            connected -> "CONNECTED"
                            error != null -> "OFFLINE"
                            disconnected -> "NOT CONNECTED"
                            else -> "CONNECTING…"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMuted,
                        maxLines = 1,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    if (volume.level >= 0) "%02d".format(volume.level) else "—",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PlexMono),
                    color = if (volume.muted) Danger else Paper,
                )
                Text(
                    if (volume.muted) "MUTED" else if (volume.max > 0) "VOL / ${volume.max}" else "VOL",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonoTag(device?.name ?: "TV")
            MonoTag(if (isOn) "SCREEN ON" else "STANDBY")
            Spacer(modifier = Modifier.weight(1f))
            if (error != null || disconnected) {
                TextButton(onClick = onReconnect) {
                    Text("RECONNECT", style = MaterialTheme.typography.labelSmall, color = Acid)
                }
            }
        }

        if (error != null) {
            Text(
                friendlyError(error.message),
                style = MaterialTheme.typography.labelSmall,
                color = Danger,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (now != null) {
            val playAccent = if (nowPlaying.state == "PLAYING") Acid else PaperMuted
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(1.dp)
                    .background(Line),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    nowPlaying.state,
                    style = MaterialTheme.typography.labelSmall,
                    color = playAccent,
                    maxLines = 1,
                )
            }
            Text(
                now,
                style = MaterialTheme.typography.bodyLarge,
                color = Paper,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            nowPlaying.artist?.takeIf { it.isNotBlank() }?.let { artist ->
                Text(
                    artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaperMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (nowPlaying.durationMs > 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(2.dp)
                        .background(Line),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(nowFraction)
                            .height(2.dp)
                            .background(playAccent),
                    )
                }
            }
        }
    }
}

private fun friendlyError(message: String): String = when {
    message.contains("failed to connect", ignoreCase = true) ||
        message.contains("timed out", ignoreCase = true) ||
        message.contains("unreachable", ignoreCase = true) ->
        "TV UNREACHABLE — CHECK POWER AND WI-FI"
    message.contains("certificate", ignoreCase = true) ->
        "TV CERTIFICATE CHANGED — FORGET AND PAIR AGAIN"
    else -> message.uppercase()
}

private fun appLabel(packageName: String): String = when (packageName) {
    "" -> "READY"
    "com.netflix.ninja" -> "Netflix"
    "org.smarttube.stable" -> "SmartTube"
    "com.google.android.youtube.tv" -> "YouTube"
    "com.amazon.amazonvideo.livingroom" -> "Prime Video"
    "com.disney.disneyplus" -> "Disney+"
    "org.xbmc.kodi" -> "Kodi"
    "com.android.tv.settings" -> "TV Settings"
    "com.gathin.tvhome" -> "VEZZ Home"
    else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
}
