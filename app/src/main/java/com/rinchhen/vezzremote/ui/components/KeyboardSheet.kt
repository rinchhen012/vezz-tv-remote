package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink1
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted

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
        containerColor = Ink1,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "KEYBOARD",
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
            )
            Text(
                "TYPE ON THE TV",
                style = MaterialTheme.typography.headlineMedium,
                color = Paper,
            )
            Text(
                "Focus a text field on the TV first.",
                style = MaterialTheme.typography.bodySmall,
                color = PaperMuted,
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("TEXT", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("Search, password, or message…") },
                shape = RoundedCornerShape(10.dp),
                minLines = 2,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Acid,
                    unfocusedBorderColor = Line,
                    cursorColor = Acid,
                    focusedLabelColor = Acid,
                    unfocusedLabelColor = PaperMuted,
                    focusedTextColor = Paper,
                    unfocusedTextColor = Paper,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccentButton(
                    text = "Send",
                    onClick = {
                        onSend(text)
                        text = ""
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
                OutlineButton(
                    text = "Send + Enter",
                    onClick = {
                        onSendAndEnter(text)
                        text = ""
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                TextButton(onClick = onClear) {
                    Text("CLEAR TV FIELD", style = MaterialTheme.typography.labelSmall, color = PaperMuted)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("DONE", style = MaterialTheme.typography.labelSmall, color = Acid)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
