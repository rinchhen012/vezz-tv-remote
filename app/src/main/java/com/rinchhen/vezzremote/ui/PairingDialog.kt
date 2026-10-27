package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.ui.MainViewModel.PairingState
import com.rinchhen.vezzremote.ui.components.GhostButton
import com.rinchhen.vezzremote.ui.components.GradientButton
import com.rinchhen.vezzremote.ui.theme.VezzGradients

@Composable
fun PairingDialog(
    state: PairingState,
    onSubmitPin: (String) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    if (state == PairingState.Idle) return

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                when (state) {
                    PairingState.Idle -> Unit
                    is PairingState.Connecting -> BusyContent(
                        title = "Pairing",
                        message = "Starting pairing with ${state.device.name}…",
                        onCancel = onCancel,
                    )

                    is PairingState.NeedsPin -> NeedsPinContent(
                        device = state.device,
                        error = state.error,
                        onSubmit = onSubmitPin,
                        onCancel = onCancel,
                    )

                    is PairingState.Verifying -> BusyContent(
                        title = "Pairing",
                        message = "Checking the code with ${state.device.name}…",
                        onCancel = onCancel,
                    )

                    is PairingState.Failed -> FailedContent(
                        message = state.message,
                        onRetry = onRetry,
                        onCancel = onCancel,
                    )
                }
            }
        }
    }
}

@Composable
private fun PairHeader(title: String, subtitle: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(VezzGradients.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NeedsPinContent(
    device: TvDevice,
    error: String?,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var pin by rememberSaveable(device.id) { mutableStateOf("") }

    PairHeader(
        title = "Pair with ${device.name}",
        subtitle = "Enter the six-character code shown on the TV.",
    )

    PinCodeInput(
        value = pin,
        onValueChange = { pin = it },
    )

    if (error != null) {
        Text(
            error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Text(
        "This is a one-time pairing. The TV remembers this phone afterwards.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    GradientButton(
        text = "Pair",
        enabled = pin.length == 6,
        onClick = { onSubmit(pin) },
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Cancel")
    }
}

@Composable
private fun PinCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BasicTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(
                input
                    .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                    .uppercase()
                    .take(6),
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Characters,
        ),
        textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(6) { index ->
                    val character = value.getOrNull(index)?.toString().orEmpty()
                    val active = value.length < 6 && index == value.length
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) MaterialTheme.colorScheme.primary
                                else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(16.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (character.isNotEmpty()) {
                            Text(
                                character,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        } else if (active) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                        ),
                                    )),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun BusyContent(
    title: String,
    message: String,
    onCancel: () -> Unit,
) {
    PairHeader(title = title, subtitle = message)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(34.dp))
    }
    GhostButton(
        text = "Cancel",
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FailedContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    PairHeader(
        title = "Pairing failed",
        subtitle = message,
    )
    Text(
        "Tap Try again to start a fresh pairing. The TV will show a new code.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GradientButton(
        text = "Try again",
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Cancel")
    }
}
