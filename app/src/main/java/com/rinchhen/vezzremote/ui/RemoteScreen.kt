package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.components.ActionRow
import com.rinchhen.vezzremote.ui.components.AppShortcuts
import com.rinchhen.vezzremote.ui.components.Dpad
import com.rinchhen.vezzremote.ui.components.KeyboardSheet
import com.rinchhen.vezzremote.ui.components.MediaRow
import com.rinchhen.vezzremote.ui.components.VolumeColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    device: TvDevice?,
    state: RemoteClient.State,
    volume: RemoteClient.VolumeState,
    currentApp: String,
    isOn: Boolean,
    haptics: Boolean,
    leftHanded: Boolean,
    onKey: (RemoteKey) -> Unit,
    onText: (String) -> Unit,
    onTextAndEnter: (String) -> Unit,
    onClear: () -> Unit,
    onLaunchPackage: (String) -> Unit,
    onDial: (String) -> Unit,
    onSendLink: (String) -> Unit,
    onReconnect: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showKeyboard by rememberSaveable { mutableStateOf(false) }
    var showPowerConfirm by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(device?.name ?: "Remote", style = MaterialTheme.typography.titleMedium)
                        Text(
                            connectionLabel(state),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (state) {
                                is RemoteClient.State.Error -> MaterialTheme.colorScheme.error
                                is RemoteClient.State.Connected -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenDevices) {
                        Icon(Icons.Default.Tv, contentDescription = "Choose TV")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state is RemoteClient.State.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Connection problem", fontWeight = FontWeight.SemiBold)
                        Text(state.message, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = onReconnect) { Text("Reconnect") }
                    }
                }
            } else if (state is RemoteClient.State.Disconnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Not connected", modifier = Modifier.weight(1f))
                        Button(onClick = onReconnect) { Text("Connect") }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    appLabel(currentApp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (volume.level >= 0) {
                    Text(
                        if (volume.muted) "Muted" else "Vol ${volume.level}/${volume.max}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (isOn) "Screen on" else "Screen off",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leftHanded) {
                    Dpad(onKey = onKey, haptics = haptics)
                    VolumeColumn(onKey = onKey, haptics = haptics)
                } else {
                    VolumeColumn(onKey = onKey, haptics = haptics)
                    Dpad(onKey = onKey, haptics = haptics)
                }
            }

            ActionRow(
                onKey = onKey,
                onSettings = onOpenSettings,
                haptics = haptics,
            )

            MediaRow(onKey = onKey, haptics = haptics)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { showKeyboard = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null)
                    Text("Keyboard")
                }
                FilledTonalButton(
                    onClick = {
                        onKey(RemoteKey.Search)
                        showKeyboard = true
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Text("Search")
                }
            }

            Text(
                "Apps",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            AppShortcuts(
                onLaunchPackage = onLaunchPackage,
                onDial = onDial,
                onSendLink = onSendLink,
            )

            OutlinedButton(
                onClick = { showPowerConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                Text("Power")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChipButton("HDMI 1", Modifier.weight(1f)) { onKey(RemoteKey.Hdmi1) }
                AssistChipButton("HDMI 2", Modifier.weight(1f)) { onKey(RemoteKey.Hdmi2) }
                AssistChipButton("HDMI 3", Modifier.weight(1f)) { onKey(RemoteKey.Hdmi3) }
            }
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
private fun AssistChipButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun connectionLabel(state: RemoteClient.State): String = when (state) {
    is RemoteClient.State.Connected -> "Connected"
    is RemoteClient.State.Connecting -> "Connecting…"
    is RemoteClient.State.Error -> "Offline"
    RemoteClient.State.Disconnected -> "Not connected"
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
    else -> packageName
}
