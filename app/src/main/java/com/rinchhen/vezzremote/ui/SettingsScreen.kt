package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.rinchhen.vezzremote.ui.components.GhostButton
import com.rinchhen.vezzremote.ui.components.GlassCard
import com.rinchhen.vezzremote.ui.components.SectionHeader
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.VezzTeal

@Composable
fun SettingsContent(
    clientName: String,
    identityStorage: String,
    haptics: Boolean,
    leftHanded: Boolean,
    paired: List<TvDevice>,
    activeDevice: TvDevice?,
    remoteState: RemoteClient.State,
    onHaptics: (Boolean) -> Unit,
    onLeftHanded: (Boolean) -> Unit,
    onForget: (TvDevice) -> Unit,
    onOpenDevices: () -> Unit,
    onSendLink: (String) -> Unit,
) {
    var testLink by rememberSaveable {
        mutableStateOf("market://launch?id=org.smarttube.stable")
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Device", style = MaterialTheme.typography.labelMedium)
                        Text(
                            activeDevice?.name ?: "No TV connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(
                                if (remoteState is RemoteClient.State.Connected) VezzTeal
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                when (remoteState) {
                                    is RemoteClient.State.Connected -> "Connected"
                                    is RemoteClient.State.Connecting -> "Connecting…"
                                    is RemoteClient.State.Error -> "Offline"
                                    RemoteClient.State.Disconnected -> "Not connected"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    GhostButton(
                        text = "Manage",
                        icon = Icons.Default.Tv,
                        onClick = onOpenDevices,
                    )
                }
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                Text("This phone", style = MaterialTheme.typography.labelMedium)
                Text(
                    clientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Pairing identity: $identityStorage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Each install gets a unique certificate name. The TV keeps one pairing per name, so pairing another phone does not remove this one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(0.dp)) {
                SwitchRow(
                    title = "Haptic feedback",
                    subtitle = "A light tap on every remote key",
                    checked = haptics,
                    onCheckedChange = onHaptics,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SwitchRow(
                    title = "Left-handed layout",
                    subtitle = "Put the volume column on the right",
                    checked = leftHanded,
                    onCheckedChange = onLeftHanded,
                )
            }
        }

        item {
            SectionHeader(
                title = "Paired TVs",
                subtitle = if (paired.isEmpty()) "No TVs yet" else "${paired.size} saved",
            )
        }

        if (paired.isEmpty()) {
            item {
                Text(
                    "Pair a TV from the Devices tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(paired, key = { it.id }) { device ->
                GlassCard(contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(device.name, fontWeight = FontWeight.Medium)
                            Text(
                                device.host,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { onForget(device) }) { Text("Forget") }
                    }
                }
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                Text("Developer: app link", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Test what this TV accepts from the remote protocol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                OutlinedTextField(
                    value = testLink,
                    onValueChange = { testLink = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    label = { Text("App link") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Button(
                    onClick = { onSendLink(testLink) },
                    enabled = testLink.isNotBlank(),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    Text("Send link")
                }
            }
        }

        item {
            GlassCard(contentPadding = PaddingValues(18.dp)) {
                Text("About", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("VEZZ Remote 0.2.0", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Android TV Remote protocol v2 over TLS. No accounts, analytics, ads or cloud service.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
