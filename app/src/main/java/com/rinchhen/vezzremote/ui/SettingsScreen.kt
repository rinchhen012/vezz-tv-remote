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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.BuildConfig
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.ui.components.Hairline
import com.rinchhen.vezzremote.ui.components.AccentButton
import com.rinchhen.vezzremote.ui.components.PageTitle
import com.rinchhen.vezzremote.ui.components.Panel
import com.rinchhen.vezzremote.ui.components.SectionLabel
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.OnAcid
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperFaint
import com.rinchhen.vezzremote.ui.theme.PaperMuted

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageTitle(
                title = "Settings",
                subtitle = "Local only",
            )
        }

        item {
            SectionLabel(text = "Device")
        }
        item {
            Panel(contentPadding = PaddingValues(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            activeDevice?.name ?: "NO TV",
                            style = MaterialTheme.typography.titleMedium,
                            color = Paper,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(
                                if (remoteState is RemoteClient.State.Connected) Acid else PaperFaint,
                            )
                            Text(
                                when (remoteState) {
                                    is RemoteClient.State.Connected -> "CONNECTED"
                                    is RemoteClient.State.Connecting -> "CONNECTING"
                                    is RemoteClient.State.Error -> "OFFLINE"
                                    RemoteClient.State.Disconnected -> "NOT CONNECTED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = PaperMuted,
                            )
                        }
                    }
                    TextButton(onClick = onOpenDevices) {
                        Text("MANAGE", style = MaterialTheme.typography.labelSmall, color = Acid)
                    }
                }
            }
        }

        item {
            SectionLabel(text = "This phone")
        }
        item {
            Panel(contentPadding = PaddingValues(16.dp)) {
                Text(clientName, style = MaterialTheme.typography.bodyLarge, color = Paper)
                Text(
                    "PAIRING IDENTITY · $identityStorage",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    "Each install gets a unique certificate name. The TV keeps one pairing per name, so pairing another phone does not remove this one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaperFaint,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }

        item {
            SectionLabel(text = "Appearance")
        }
        item {
            Panel(contentPadding = PaddingValues(0.dp)) {
                SwitchRow(
                    title = "Haptic feedback",
                    subtitle = "Light tick on every key",
                    checked = haptics,
                    onCheckedChange = onHaptics,
                )
                Hairline()
                SwitchRow(
                    title = "Left-handed layout",
                    subtitle = "Volume on the right",
                    checked = leftHanded,
                    onCheckedChange = onLeftHanded,
                )
            }
        }

        item {
            SectionLabel(text = "Paired TVs")
        }
        if (paired.isEmpty()) {
            item {
                Text(
                    "NO PAIRED TVS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                )
            }
        } else {
            items(paired, key = { it.id }) { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium, color = Paper)
                        Text(
                            device.host,
                            style = MaterialTheme.typography.labelSmall,
                            color = PaperMuted,
                        )
                    }
                    TextButton(onClick = { onForget(device) }) {
                        Text("FORGET", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
                    }
                }
                Hairline()
            }
        }

        item {
            SectionLabel(text = "Developer")
        }
        item {
            Panel(contentPadding = PaddingValues(16.dp)) {
                Text(
                    "APP LINK",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                )
                OutlinedTextField(
                    value = testLink,
                    onValueChange = { testLink = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("URI", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Acid,
                        unfocusedBorderColor = Line,
                        cursorColor = Acid,
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                    ),
                )
                AccentButton(
                    text = "Send link",
                    onClick = { onSendLink(testLink) },
                    enabled = testLink.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }

        item {
            SectionLabel(text = "About")
        }
        item {
            Panel(contentPadding = PaddingValues(16.dp)) {
                Text("VEZZ REMOTE ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelLarge, color = Paper)
                Text(
                    "Android TV Remote protocol v2 over TLS. No accounts, analytics, ads or cloud service.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaperFaint,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Paper)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnAcid,
                checkedTrackColor = Acid,
                uncheckedThumbColor = PaperMuted,
                uncheckedTrackColor = Line,
                uncheckedBorderColor = Line,
            ),
        )
    }
}
