package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardSheet(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onSendAndEnter: (String) -> Unit,
    onClear: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = FocusRequester()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Keyboard", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Focus a text field on the TV first, then type here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Text to send") },
                minLines = 2,
                maxLines = 5,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSend(text)
                        text = ""
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Send")
                }
                FilledTonalButton(
                    onClick = {
                        onSendAndEnter(text)
                        text = ""
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Send + Enter")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Clear TV field")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Done")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
