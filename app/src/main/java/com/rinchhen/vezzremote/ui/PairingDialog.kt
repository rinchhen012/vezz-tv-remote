package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinchhen.vezzremote.ui.MainViewModel.PairingState

@Composable
fun PairingDialog(
    state: PairingState,
    onSubmitPin: (String) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        PairingState.Idle -> Unit
        is PairingState.Connecting -> AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Pairing") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text("Starting pairing with ${state.device.name}…")
                }
            },
            confirmButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
        )

        is PairingState.NeedsPin -> {
            var pin by rememberSaveable(state.device.id) { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onCancel,
                title = { Text("Pair with ${state.device.name}") },
                text = {
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Enter the six-character code shown on the TV.")
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { value ->
                                pin = value
                                    .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                                    .uppercase()
                                    .take(6)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = state.error != null,
                            supportingText = state.error?.let { { Text(it) } },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                letterSpacing = 8.sp,
                                textAlign = TextAlign.Center,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSubmitPin(pin) },
                        enabled = pin.length == 6,
                    ) { Text("Pair") }
                },
                dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
            )
        }

        is PairingState.Verifying -> AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Pairing") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text("Checking the code with ${state.device.name}…")
                }
            },
            confirmButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
        )

        is PairingState.Failed -> AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Pairing failed") },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(state.message)
                    Text(
                        "Tap “Try again” to start a new pairing. The TV will show a fresh code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = { TextButton(onClick = onRetry) { Text("Try again") } },
            dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
        )
    }
}
