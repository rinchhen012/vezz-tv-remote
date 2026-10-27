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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rinchhen.vezzremote.data.TvDevice
import com.rinchhen.vezzremote.ui.MainViewModel.PairingState
import com.rinchhen.vezzremote.ui.components.AccentButton
import com.rinchhen.vezzremote.ui.components.Hairline
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Danger
import com.rinchhen.vezzremote.ui.theme.Ink1
import com.rinchhen.vezzremote.ui.theme.Ink2
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted

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
            shape = RoundedCornerShape(16.dp),
            color = Ink1,
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (state) {
                    PairingState.Idle -> Unit
                    is PairingState.Connecting -> BusyContent(
                        title = "PAIR TV",
                        message = "STARTING PAIRING WITH ${state.device.name.uppercase()}…",
                        onCancel = onCancel,
                    )

                    is PairingState.NeedsPin -> NeedsPinContent(
                        device = state.device,
                        error = state.error,
                        onSubmit = onSubmitPin,
                        onCancel = onCancel,
                    )

                    is PairingState.Verifying -> BusyContent(
                        title = "PAIR TV",
                        message = "CHECKING THE CODE WITH ${state.device.name.uppercase()}…",
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = PaperMuted)
        Text(subtitle, style = MaterialTheme.typography.headlineSmall, color = Paper)
        Hairline(modifier = Modifier.padding(top = 6.dp))
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
        title = "PAIR TV",
        subtitle = device.name,
    )
    Text(
        "ENTER THE SIX-CHARACTER CODE FROM THE TV",
        style = MaterialTheme.typography.labelSmall,
        color = PaperMuted,
    )
    PinCodeInput(
        value = pin,
        onValueChange = { pin = it },
    )
    if (error != null) {
        Text(
            error.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Danger,
        )
    }
    Text(
        "ONE-TIME PAIRING. THE TV REMEMBERS THIS PHONE.",
        style = MaterialTheme.typography.labelSmall,
        color = PaperMuted,
    )
    AccentButton(
        text = "Pair",
        enabled = pin.length == 6,
        onClick = { onSubmit(pin) },
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
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
            .height(58.dp)
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
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(6) { index ->
                    val character = value.getOrNull(index)?.toString().orEmpty()
                    val active = value.length < 6 && index == value.length
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ink2)
                            .border(
                                width = 1.dp,
                                color = if (active) Acid else Line,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            character,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Paper,
                        )
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
    ) {
        CircularProgressIndicator(modifier = Modifier.size(30.dp), color = Acid, strokeWidth = 2.dp)
    }
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
    }
}

@Composable
private fun FailedContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    PairHeader(title = "PAIRING FAILED", subtitle = message)
    Text(
        "TRY AGAIN TO GET A FRESH CODE FROM THE TV.",
        style = MaterialTheme.typography.labelSmall,
        color = PaperMuted,
    )
    AccentButton(
        text = "Try again",
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(),
    )
    TextButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
    }
}
