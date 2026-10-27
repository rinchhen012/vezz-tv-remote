package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsInputHdmi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink0
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.LineStrong
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted

@Composable
fun Dpad(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(204.dp)
            .clip(CircleShape)
            .background(Ink0)
            .border(1.dp, LineStrong, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(10.dp)
                .border(1.dp, Line, CircleShape),
        )
        RoundKey(
            icon = Icons.Outlined.KeyboardArrowUp,
            contentDescription = "Up",
            onClick = { onKey(RemoteKey.Up) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            size = 48.dp,
            haptics = haptics,
        )
        RoundKey(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Left",
            onClick = { onKey(RemoteKey.Left) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp),
            size = 48.dp,
            haptics = haptics,
        )
        RoundKey(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Right",
            onClick = { onKey(RemoteKey.Right) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            size = 48.dp,
            haptics = haptics,
        )
        RoundKey(
            icon = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Down",
            onClick = { onKey(RemoteKey.Down) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
            size = 48.dp,
            haptics = haptics,
        )
        RoundKey(
            icon = Icons.Outlined.Check,
            contentDescription = "OK",
            onClick = { onKey(RemoteKey.Ok) },
            modifier = Modifier.align(Alignment.Center),
            size = 76.dp,
            accent = true,
            haptics = haptics,
        )
    }
}

@Composable
fun VolumeRocker(
    volume: RemoteClient.VolumeState,
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    val fraction = if (volume.max > 0) {
        (volume.level.toFloat() / volume.max).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (volume.muted) "MUTE" else "VOL",
            style = MaterialTheme.typography.labelSmall,
            color = if (volume.muted) MaterialTheme.colorScheme.error else PaperMuted,
        )
        RoundKey(
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            contentDescription = "Volume up",
            onClick = { onKey(RemoteKey.VolumeUp) },
            size = 48.dp,
            haptics = haptics,
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(36.dp)
                .background(Line),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .background(Acid),
                )
            }
        }
        RoundKey(
            icon = Icons.AutoMirrored.Outlined.VolumeOff,
            contentDescription = "Mute",
            onClick = { onKey(RemoteKey.Mute) },
            size = 48.dp,
            haptics = haptics,
        )
        RoundKey(
            icon = Icons.AutoMirrored.Outlined.VolumeDown,
            contentDescription = "Volume down",
            onClick = { onKey(RemoteKey.VolumeDown) },
            size = 48.dp,
            haptics = haptics,
        )
    }
}

@Composable
fun ActionGrid(
    onKey: (RemoteKey) -> Unit,
    onPower: () -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Keycap(
                Icons.AutoMirrored.Outlined.ArrowBack,
                "Back",
                { onKey(RemoteKey.Back) },
                Modifier.weight(1f),
                haptics = haptics,
            )
            Keycap(
                Icons.Outlined.Home,
                "Home",
                { onKey(RemoteKey.Home) },
                Modifier.weight(1f),
                haptics = haptics,
            )
            Keycap(
                Icons.Outlined.Menu,
                "Menu",
                { onKey(RemoteKey.Menu) },
                Modifier.weight(1f),
                haptics = haptics,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Keycap(
                Icons.AutoMirrored.Outlined.Input,
                "Inputs",
                { onKey(RemoteKey.Input) },
                Modifier.weight(1f),
                haptics = haptics,
            )
            Keycap(
                Icons.Outlined.Settings,
                "TV settings",
                { onKey(RemoteKey.Settings) },
                Modifier.weight(1f),
                haptics = haptics,
            )
            Keycap(
                Icons.Outlined.PowerSettingsNew,
                "Power",
                onPower,
                Modifier.weight(1f),
                haptics = haptics,
            )
        }
    }
}

@Composable
fun MediaControls(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundKey(
            Icons.Outlined.FastRewind,
            "Rewind",
            { onKey(RemoteKey.Rewind) },
            size = 52.dp,
            haptics = haptics,
        )
        RoundKey(
            Icons.Outlined.PlayArrow,
            "Play or pause",
            { onKey(RemoteKey.PlayPause) },
            size = 68.dp,
            accent = true,
            haptics = haptics,
        )
        RoundKey(
            Icons.Outlined.FastForward,
            "Fast forward",
            { onKey(RemoteKey.FastForward) },
            size = 52.dp,
            haptics = haptics,
        )
    }
}

@Composable
fun FavoriteAppsRow(
    apps: List<TvApp>,
    currentApp: String,
    onLaunch: (TvApp) -> Unit,
    onOpenApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = "Apps") {
            TextButton(onClick = onOpenApps) {
                Text(
                    "SEE ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Acid,
                )
            }
        }
        if (apps.isEmpty()) {
            Text(
                "NO APPS",
                style = MaterialTheme.typography.labelSmall,
                color = PaperMuted,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    AppIconLabel(
                        app = app,
                        running = app.packageName == currentApp,
                        onClick = { onLaunch(app) },
                    )
                }
            }
        }
    }
}

@Composable
fun HdmiRow(
    onKey: (RemoteKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Keycap(
            Icons.Outlined.SettingsInputHdmi,
            "HDMI 1",
            { onKey(RemoteKey.Hdmi1) },
            Modifier.weight(1f),
        )
        Keycap(
            Icons.Outlined.SettingsInputHdmi,
            "HDMI 2",
            { onKey(RemoteKey.Hdmi2) },
            Modifier.weight(1f),
        )
        Keycap(
            Icons.Outlined.SettingsInputHdmi,
            "HDMI 3",
            { onKey(RemoteKey.Hdmi3) },
            Modifier.weight(1f),
        )
    }
}
