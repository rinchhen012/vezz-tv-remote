package com.rinchhen.vezzremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.theme.VezzGradients
import com.rinchhen.vezzremote.ui.theme.VezzTeal

@Composable
fun Dpad(
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(224.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF211E32), Color(0xFF0D0D14)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(9.dp)
                .border(1.dp, Color.White.copy(alpha = 0.04f), CircleShape),
        )
        RoundIconButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Up",
            onClick = { onKey(RemoteKey.Up) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 13.dp),
            size = 52.dp,
            container = Color.White.copy(alpha = 0.045f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            haptics = haptics,
        )
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Left",
            onClick = { onKey(RemoteKey.Left) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 13.dp),
            size = 52.dp,
            container = Color.White.copy(alpha = 0.045f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            haptics = haptics,
        )
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Right",
            onClick = { onKey(RemoteKey.Right) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 13.dp),
            size = 52.dp,
            container = Color.White.copy(alpha = 0.045f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            haptics = haptics,
        )
        RoundIconButton(
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Down",
            onClick = { onKey(RemoteKey.Down) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 13.dp),
            size = 52.dp,
            container = Color.White.copy(alpha = 0.045f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            haptics = haptics,
        )
        RoundIconButton(
            icon = Icons.Default.Check,
            contentDescription = "OK",
            onClick = { onKey(RemoteKey.Ok) },
            modifier = Modifier.align(Alignment.Center),
            size = 82.dp,
            gradient = VezzGradients.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            haptics = haptics,
        )
    }
}

@Composable
fun VolumeCard(
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
    GlassCard(
        modifier = modifier.width(76.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (volume.muted) "MUTE" else "VOL",
                style = MaterialTheme.typography.labelSmall,
                color = if (volume.muted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            RoundIconButton(
                Icons.AutoMirrored.Filled.VolumeUp,
                "Volume up",
                { onKey(RemoteKey.VolumeUp) },
                size = 48.dp,
                haptics = haptics,
            )
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .clip(CircleShape)
                            .background(VezzTeal),
                    )
                }
            }
            RoundIconButton(
                Icons.AutoMirrored.Filled.VolumeOff,
                "Mute",
                { onKey(RemoteKey.Mute) },
                size = 48.dp,
                haptics = haptics,
            )
            RoundIconButton(
                Icons.AutoMirrored.Filled.VolumeDown,
                "Volume down",
                { onKey(RemoteKey.VolumeDown) },
                size = 48.dp,
                haptics = haptics,
            )
        }
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
            SmallActionButton(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                { onKey(RemoteKey.Back) },
                Modifier.weight(1f),
            )
            SmallActionButton(
                Icons.Default.Home,
                "Home",
                { onKey(RemoteKey.Home) },
                Modifier.weight(1f),
            )
            SmallActionButton(
                Icons.Default.Menu,
                "Menu",
                { onKey(RemoteKey.Menu) },
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(
                Icons.AutoMirrored.Filled.Input,
                "Inputs",
                { onKey(RemoteKey.Input) },
                Modifier.weight(1f),
            )
            SmallActionButton(
                Icons.Default.Settings,
                "TV settings",
                { onKey(RemoteKey.Settings) },
                Modifier.weight(1f),
            )
            SmallActionButton(
                Icons.Default.PowerSettingsNew,
                "Power",
                onPower,
                Modifier.weight(1f),
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
        RoundIconButton(
            Icons.Default.FastRewind,
            "Rewind",
            { onKey(RemoteKey.Rewind) },
            size = 56.dp,
            haptics = haptics,
        )
        RoundIconButton(
            Icons.Default.PlayArrow,
            "Play or pause",
            { onKey(RemoteKey.PlayPause) },
            size = 72.dp,
            gradient = VezzGradients.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            haptics = haptics,
        )
        RoundIconButton(
            Icons.Default.FastForward,
            "Fast forward",
            { onKey(RemoteKey.FastForward) },
            size = 56.dp,
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "Favorites",
            subtitle = "One tap from the couch",
            action = {
                TextButton(onClick = onOpenApps) { Text("See all") }
            },
        )
        if (apps.isEmpty()) {
            Text(
                "No apps yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
        SmallActionButton(
            Icons.AutoMirrored.Filled.Input,
            "HDMI 1",
            { onKey(RemoteKey.Hdmi1) },
            Modifier.weight(1f),
        )
        SmallActionButton(
            Icons.AutoMirrored.Filled.Input,
            "HDMI 2",
            { onKey(RemoteKey.Hdmi2) },
            Modifier.weight(1f),
        )
        SmallActionButton(
            Icons.AutoMirrored.Filled.Input,
            "HDMI 3",
            { onKey(RemoteKey.Hdmi3) },
            Modifier.weight(1f),
        )
    }
}
