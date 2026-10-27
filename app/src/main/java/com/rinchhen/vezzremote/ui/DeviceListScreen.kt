package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.ui.components.GhostButton
import com.rinchhen.vezzremote.ui.components.GlassCard
import com.rinchhen.vezzremote.ui.components.GradientButton
import com.rinchhen.vezzremote.ui.components.SectionHeader
import com.rinchhen.vezzremote.ui.components.StatPill
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.VezzGradients
import com.rinchhen.vezzremote.ui.theme.VezzTeal

@Composable
fun DevicesContent(
    paired: List<TvDevice>,
    discovered: List<TvDevice>,
    activeDevice: TvDevice?,
    remoteState: RemoteClient.State,
    clientName: String,
    identityReady: Boolean,
    onOpen: (TvDevice) -> Unit,
    onForget: (TvDevice) -> Unit,
    onManual: (String) -> Unit,
    onRescan: () -> Unit,
    onOpenRemote: () -> Unit,
) {
    var manualHost by rememberSaveable { mutableStateOf("") }
    val pairedIds = paired.map { it.id }.toSet()
    val pairedHosts = paired.map { it.host }.toSet()
    val newDevices = discovered.filter { it.id !in pairedIds && it.host !in pairedHosts }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "Your TVs",
                subtitle = if (paired.isEmpty()) "Pair once; the TV remembers this phone" else "${paired.size} paired",
            )
        }

        if (activeDevice != null) {
            item {
                ActiveTvCard(
                    device = activeDevice,
                    state = remoteState,
                    onOpenRemote = onOpenRemote,
                    onForget = { onForget(activeDevice) },
                )
            }
        }

        if (paired.isEmpty() && activeDevice == null) {
            item {
                GlassCard(contentPadding = PaddingValues(20.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(VezzGradients.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Text("Pair your TV", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Turn the TV on, keep this phone on the same Wi-Fi, and pair once with the code on screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(paired.filter { it.id != activeDevice?.id }, key = { it.id }) { device ->
                TvCard(
                    device = device,
                    paired = true,
                    active = false,
                    connected = false,
                    onClick = { onOpen(device) },
                    onForget = { onForget(device) },
                )
            }
        }

        item {
            SectionHeader(
                title = "Found on network",
                subtitle = "mDNS · _androidtvremote2._tcp",
                action = {
                    TextButton(onClick = onRescan) { Text("Rescan") }
                },
            )
        }

        if (newDevices.isEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        "Searching for TVs on this Wi-Fi…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(newDevices, key = { it.id }) { device ->
                TvCard(
                    device = device,
                    paired = false,
                    active = false,
                    connected = false,
                    onClick = { onOpen(device) },
                    onForget = null,
                )
            }
        }

        item {
            SectionHeader(
                title = "Add by IP",
                subtitle = "For networks that block mDNS",
            )
        }

        item {
            GlassCard(contentPadding = PaddingValues(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = manualHost,
                        onValueChange = { manualHost = it },
                        label = { Text("TV IP address") },
                        placeholder = { Text("192.168.1.20") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.weight(1f),
                    )
                    GradientButton(
                        text = "Pair",
                        icon = Icons.Default.Add,
                        enabled = manualHost.isNotBlank() && identityReady,
                        onClick = { onManual(manualHost) },
                        modifier = Modifier.width(108.dp),
                    )
                }
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(16.dp)) {
                Text("This phone", style = MaterialTheme.typography.labelMedium)
                Text(
                    clientName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 3.dp),
                )
                if (!identityReady) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text(
                            "Preparing the pairing identity…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    "Each install has a unique certificate name, so pairing another phone does not remove this one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ActiveTvCard(
    device: TvDevice,
    state: RemoteClient.State,
    onOpenRemote: () -> Unit,
    onForget: () -> Unit,
) {
    val connected = state is RemoteClient.State.Connected
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF3A2F7A),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VezzGradients.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(device.name, style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(if (connected) VezzTeal else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (connected) "Connected · ready" else "Paired · offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton(
                    text = "Open remote",
                    onClick = onOpenRemote,
                    modifier = Modifier.weight(1f),
                )
                GhostButton(
                    text = "Forget",
                    onClick = onForget,
                )
            }
        }
    }
}

@Composable
private fun TvCard(
    device: TvDevice,
    paired: Boolean,
    active: Boolean,
    connected: Boolean,
    onClick: () -> Unit,
    onForget: (() -> Unit)?,
) {
    GlassCard(
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (active) VezzGradients.primary
                        else Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (paired) Icons.Default.Tv else Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (paired) {
                        StatusDot(if (connected) VezzTeal else MaterialTheme.colorScheme.onSurfaceVariant, size = 7.dp)
                    }
                    Text(
                        "${device.host}  ·  ${if (connected) "Connected" else if (paired) "Paired" else "Not paired"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (paired && onForget != null) {
                TextButton(onClick = onForget) { Text("Forget") }
            } else {
                TextButton(onClick = onClick) { Text("Pair") }
            }
        }
    }
}
