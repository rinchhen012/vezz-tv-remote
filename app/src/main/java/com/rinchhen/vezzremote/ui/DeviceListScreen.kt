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
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.ui.components.AccentButton
import com.rinchhen.vezzremote.ui.components.Hairline
import com.rinchhen.vezzremote.ui.components.MonoTag
import com.rinchhen.vezzremote.ui.components.OutlineButton
import com.rinchhen.vezzremote.ui.components.PageTitle
import com.rinchhen.vezzremote.ui.components.Panel
import com.rinchhen.vezzremote.ui.components.SectionLabel
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink2
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperFaint
import com.rinchhen.vezzremote.ui.theme.PaperMuted

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageTitle(
                title = "Devices",
                subtitle = "${paired.size} paired",
            )
        }

        if (activeDevice != null) {
            item {
                ActiveTvPanel(
                    device = activeDevice,
                    state = remoteState,
                    onOpenRemote = onOpenRemote,
                    onForget = { onForget(activeDevice) },
                )
            }
        }

        if (activeDevice == null) {
            item {
                SectionLabel(text = "Your TVs")
            }
        }

        if (paired.isEmpty() && activeDevice == null) {
            item {
                Panel(contentPadding = PaddingValues(18.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, Line, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Tv, contentDescription = null, tint = PaperMuted, modifier = Modifier.size(20.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("PAIR YOUR TV", style = MaterialTheme.typography.labelLarge, color = Paper)
                            Text(
                                "Turn the TV on, keep this phone on the same Wi-Fi, then pair once with the code on screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PaperMuted,
                            )
                        }
                    }
                }
            }
        } else {
            items(paired.filter { it.id != activeDevice?.id }, key = { it.id }) { device ->
                TvRow(
                    device = device,
                    paired = true,
                    active = false,
                    connected = false,
                    onClick = { onOpen(device) },
                    onForget = { onForget(device) },
                )
                Hairline()
            }
        }

        item {
            SectionLabel(text = "Found on network") {
                TextButton(onClick = onRescan) {
                    Text("RESCAN", style = MaterialTheme.typography.labelSmall, color = Acid)
                }
            }
        }

        if (newDevices.isEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PaperFaint)
                    Text(
                        "SEARCHING…",
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMuted,
                    )
                }
            }
        } else {
            items(newDevices, key = { it.id }) { device ->
                TvRow(
                    device = device,
                    paired = false,
                    active = false,
                    connected = false,
                    onClick = { onOpen(device) },
                    onForget = null,
                )
                Hairline()
            }
        }

        item {
            SectionLabel(text = "Add by IP")
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = manualHost,
                    onValueChange = { manualHost = it },
                    label = { Text("TV IP", style = MaterialTheme.typography.labelSmall) },
                    placeholder = { Text("192.168.1.20") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Acid,
                        unfocusedBorderColor = Line,
                        cursorColor = Acid,
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                    ),
                )
                AccentButton(
                    text = "Pair",
                    enabled = manualHost.isNotBlank() && identityReady,
                    onClick = { onManual(manualHost) },
                    modifier = Modifier.width(92.dp),
                )
            }
        }

        item {
            SectionLabel(text = "This phone")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(clientName, style = MaterialTheme.typography.bodyLarge, color = Paper)
                if (!identityReady) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = PaperFaint)
                        Text("PREPARING IDENTITY", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
                    }
                }
                Text(
                    "Each install has a unique certificate name, so pairing another phone does not remove this one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaperFaint,
                )
            }
        }
    }
}

@Composable
private fun ActiveTvPanel(
    device: TvDevice,
    state: RemoteClient.State,
    onOpenRemote: () -> Unit,
    onForget: () -> Unit,
) {
    val connected = state is RemoteClient.State.Connected
    Panel(contentPadding = PaddingValues(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(device.name, style = MaterialTheme.typography.headlineSmall, color = Paper)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(if (connected) Acid else PaperFaint)
                    Text(
                        if (connected) "CONNECTED" else "PAIRED · OFFLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMuted,
                    )
                }
                Text(
                    device.host,
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperFaint,
                )
            }
            MonoTag(if (connected) "LIVE" else "IDLE", accent = connected)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AccentButton(
                text = "Open remote",
                onClick = onOpenRemote,
                modifier = Modifier.weight(1f),
            )
            OutlineButton(
                text = "Forget",
                onClick = onForget,
            )
        }
    }
}

@Composable
private fun TvRow(
    device: TvDevice,
    paired: Boolean,
    active: Boolean,
    connected: Boolean,
    onClick: () -> Unit,
    onForget: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, if (active) Acid else Line, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (paired) Icons.Outlined.Tv else Icons.Outlined.Wifi,
                contentDescription = null,
                tint = if (active) Acid else PaperMuted,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                device.name,
                style = MaterialTheme.typography.titleMedium,
                color = Paper,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "${device.host}  ·  ${if (connected) "CONNECTED" else if (paired) "PAIRED" else "NOT PAIRED"}",
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
            )
        }
        if (paired && onForget != null) {
            TextButton(onClick = onForget) {
                Text("FORGET", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
            }
        } else {
            TextButton(onClick = onClick) {
                Text("PAIR", style = MaterialTheme.typography.labelSmall, color = Acid)
            }
        }
    }
}
