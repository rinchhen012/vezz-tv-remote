package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.ui.MainViewModel
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink0
import com.rinchhen.vezzremote.ui.theme.Ink1
import com.rinchhen.vezzremote.ui.theme.OnAcid
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSheet(
    state: MainViewModel.VoiceState,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (state is MainViewModel.VoiceState.Idle) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            if (state is MainViewModel.VoiceState.Error) onDismiss() else onStop()
        },
        sheetState = sheetState,
        containerColor = Ink1,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "VOICE SEARCH",
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
                modifier = Modifier.fillMaxWidth(),
            )
            when (state) {
                MainViewModel.VoiceState.Starting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(54.dp),
                        color = Acid,
                        trackColor = Ink0,
                        strokeWidth = 3.dp,
                    )
                    Text(
                        "CONNECTING TO TV…",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Paper,
                    )
                    Text(
                        "The TV will open its voice search in a moment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PaperMuted,
                    )
                }

                MainViewModel.VoiceState.Recording -> {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Acid),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Mic,
                            contentDescription = null,
                            tint = OnAcid,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Text(
                        "LISTENING",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Paper,
                    )
                    Text(
                        "Speak into the phone. Tap Stop when you are done.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PaperMuted,
                    )
                    AccentButton(
                        text = "Stop",
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is MainViewModel.VoiceState.Error -> {
                    Text(
                        "VOICE SEARCH",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Paper,
                    )
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = PaperMuted,
                    )
                    AccentButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                MainViewModel.VoiceState.Idle -> Unit
            }
            Box(modifier = Modifier.height(2.dp))
        }
    }
}
