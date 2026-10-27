package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    paired: List<TvDevice>,
    discovered: List<TvDevice>,
    clientName: String,
    identityReady: Boolean,
    onOpen: (TvDevice) -> Unit,
    onForget: (TvDevice) -> Unit,
    onManual: (String) -> Unit,
    onRescan: () -> Unit,
    onSettings: () -> Unit,
) {
    var manualHost by rememberSaveable { mutableStateOf("") }
    val pairedIds = paired.map { it.id }.toSet()
    val pairedHosts = paired.map { it.host }.toSet()
    val newDevices = discovered.filter { it.id !in pairedIds && it.host !in pairedHosts }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VEZZ Remote") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Your TVs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (paired.isEmpty()) {
                item {
                    Text(
                        "No TVs paired yet. Pick one below or add its IP address.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(paired, key = { it.id }) { device ->
                    TvCard(
                        device = device,
                        paired = true,
                        onClick = { onOpen(device) },
                        onForget = { onForget(device) },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Found on network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRescan) { Text("Rescan") }
                }
            }
            if (newDevices.isEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            "Searching for _androidtvremote2._tcp…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(newDevices, key = { it.id }) { device ->
                    TvCard(
                        device = device,
                        paired = false,
                        onClick = { onOpen(device) },
                        onForget = null,
                    )
                }
            }

            item {
                Text(
                    "Add by IP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = manualHost,
                        onValueChange = { manualHost = it },
                        label = { Text("TV IP address") },
                        placeholder = { Text("192.168.1.20") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { onManual(manualHost) },
                        enabled = manualHost.isNotBlank() && identityReady,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Pair")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("This phone", style = MaterialTheme.typography.labelMedium)
                        Text(clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (!identityReady) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
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
                            "Pairing is one-time per TV. Other phones pair separately and keep their own pairing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvCard(
    device: TvDevice,
    paired: Boolean,
    onClick: () -> Unit,
    onForget: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (paired) Icons.Default.Tv else Icons.Default.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${device.host}  •  ${if (paired) "Paired" else "Not paired"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (paired && onForget != null) {
                TextButton(onClick = onForget) {
                    Text("Forget")
                }
            } else {
                TextButton(onClick = onClick) {
                    Text(if (paired) "Connect" else "Pair")
                }
            }
        }
    }
}
