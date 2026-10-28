package com.rinchhen.vezzremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.protocol.RemoteKey
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink0
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.LineStrong
import com.rinchhen.vezzremote.ui.theme.OnAcid
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun Dpad(
    onKey: (RemoteKey) -> Unit,
    onLongStart: (RemoteKey) -> Unit,
    onLongEnd: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var pressed by remember { mutableStateOf<DpadZone?>(null) }
    val okScale by animateFloatAsState(
        if (pressed == DpadZone.Ok) 0.94f else 1f,
        animationSpec = tween(110),
        label = "dpad-ok",
    )

    Box(
        modifier = modifier
            .size(232.dp)
            .clip(CircleShape)
            .background(Ink0)
            .border(1.dp, LineStrong, CircleShape)
            .pointerInput(Unit) {
                val okRadius = with(density) { 42.dp.toPx() }
                val outerRadius = minOf(size.width, size.height) / 2f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val zone = dpadZone(down.position, size, okRadius, outerRadius)
                    if (zone == null) return@awaitEachGesture
                    pressed = zone
                    var dragged = false
                    var longStarted = false
                    val longPressDeadline =
                        System.currentTimeMillis() + viewConfiguration.longPressTimeoutMillis
                    while (true) {
                        val event = if (zone == DpadZone.Ok && !longStarted) {
                            val remaining = longPressDeadline - System.currentTimeMillis()
                            if (remaining > 0) {
                                withTimeoutOrNull(remaining) { awaitPointerEvent() }
                            } else {
                                null
                            }
                        } else {
                            awaitPointerEvent()
                        }
                        if (event == null) {
                            longStarted = true
                            if (haptics) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                            onLongStart(RemoteKey.Ok)
                            continue
                        }
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) {
                            dragged = true
                            break
                        }
                        if (!change.pressed) {
                            if (longStarted) {
                                onLongEnd(RemoteKey.Ok)
                            } else if (!dragged) {
                                change.consume()
                                if (haptics) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                onKey(zone.key)
                            }
                            break
                        }
                        if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                            dragged = true
                            break
                        }
                    }
                    if (dragged && longStarted) onLongEnd(RemoteKey.Ok)
                    pressed = null
                }
            }
            .semantics {
                contentDescription = "Directional pad"
                customActions = listOf(
                    CustomAccessibilityAction("Up") { onKey(RemoteKey.Up); true },
                    CustomAccessibilityAction("Right") { onKey(RemoteKey.Right); true },
                    CustomAccessibilityAction("Down") { onKey(RemoteKey.Down); true },
                    CustomAccessibilityAction("Left") { onKey(RemoteKey.Left); true },
                    CustomAccessibilityAction("OK") { onKey(RemoteKey.Ok); true },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val outer = size.minDimension / 2f
            val inner = 42.dp.toPx()
            val active = pressed
            if (active != null && active != DpadZone.Ok) {
                val start = when (active) {
                    DpadZone.Right -> -45f
                    DpadZone.Down -> 45f
                    DpadZone.Left -> 135f
                    DpadZone.Up -> 225f
                    DpadZone.Ok -> 0f
                }
                val rOuter = outer - 2.dp.toPx()
                val rInner = inner + 3.dp.toPx()
                val sector = Path().apply {
                    arcTo(
                        Rect(center.x - rOuter, center.y - rOuter, center.x + rOuter, center.y + rOuter),
                        start,
                        90f,
                        true,
                    )
                    arcTo(
                        Rect(center.x - rInner, center.y - rInner, center.x + rInner, center.y + rInner),
                        start + 90f,
                        -90f,
                        false,
                    )
                    close()
                }
                drawPath(sector, Acid.copy(alpha = 0.14f))
            }
            drawCircle(Line, radius = outer - 10.dp.toPx(), style = Stroke(1.dp.toPx()))
        }

        Icon(
            Icons.Outlined.KeyboardArrowUp,
            contentDescription = null,
            tint = if (pressed == DpadZone.Up) Acid else Paper,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
                .size(32.dp),
        )
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = if (pressed == DpadZone.Right) Acid else Paper,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(32.dp),
        )
        Icon(
            Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = if (pressed == DpadZone.Down) Acid else Paper,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
                .size(32.dp),
        )
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = null,
            tint = if (pressed == DpadZone.Left) Acid else Paper,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .size(32.dp),
        )

        Box(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer {
                    scaleX = okScale
                    scaleY = okScale
                }
                .clip(CircleShape)
                .background(Acid)
                .border(1.dp, Acid, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = OnAcid,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

private enum class DpadZone(val key: RemoteKey) {
    Up(RemoteKey.Up),
    Right(RemoteKey.Right),
    Down(RemoteKey.Down),
    Left(RemoteKey.Left),
    Ok(RemoteKey.Ok),
}

private fun dpadZone(offset: Offset, size: IntSize, okRadius: Float, outerRadius: Float): DpadZone? {
    val dx = offset.x - size.width / 2f
    val dy = offset.y - size.height / 2f
    val distance = hypot(dx, dy)
    if (distance > outerRadius) return null
    if (distance <= okRadius) return DpadZone.Ok
    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
    return when {
        angle >= -45.0 && angle < 45.0 -> DpadZone.Right
        angle >= 45.0 && angle < 135.0 -> DpadZone.Down
        angle >= 135.0 || angle < -135.0 -> DpadZone.Left
        else -> DpadZone.Up
    }
}

@Composable
fun VolumeRocker(
    volume: RemoteClient.VolumeState,
    onKey: (RemoteKey) -> Unit,
    haptics: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf<VolumeZone?>(null) }
    val fraction = if (volume.max > 0) {
        (volume.level.toFloat() / volume.max).coerceIn(0f, 1f)
    } else {
        0f
    }
    val shape = RoundedCornerShape(32.dp)

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
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(232.dp)
                .clip(shape)
                .background(Ink0)
                .border(1.dp, LineStrong, shape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val zone = when {
                            down.position.y < size.height / 3f -> VolumeZone.Up
                            down.position.y < size.height * 2f / 3f -> VolumeZone.Down
                            else -> VolumeZone.Mute
                        }
                        pressed = zone
                        var dragged = false
                        var sentByHold = false
                        val repeat = if (zone != VolumeZone.Mute) {
                            scope.launch {
                                // Slow, deliberate repeat: 5 is already loud on this TV.
                                delay(900)
                                while (isActive && !dragged) {
                                    sentByHold = true
                                    if (haptics) {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    }
                                    onKey(zone.key)
                                    delay(700)
                                }
                            }
                        } else {
                            null
                        }
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.isConsumed) {
                                dragged = true
                                break
                            }
                            if (!change.pressed) {
                                if (!dragged && !sentByHold) {
                                    change.consume()
                                    if (haptics) {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    }
                                    onKey(zone.key)
                                }
                                break
                            }
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                dragged = true
                                break
                            }
                        }
                        repeat?.cancel()
                        pressed = null
                    }
                }
                .semantics {
                    contentDescription = "Volume rocker"
                    customActions = listOf(
                        CustomAccessibilityAction("Volume up") { onKey(RemoteKey.VolumeUp); true },
                        CustomAccessibilityAction("Volume down") { onKey(RemoteKey.VolumeDown); true },
                        CustomAccessibilityAction("Mute") { onKey(RemoteKey.Mute); true },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .align(Alignment.BottomCenter)
                        .background(Acid.copy(alpha = 0.13f)),
                )
            }
            Column(Modifier.fillMaxSize()) {
                VolumeZoneBox(
                    zone = VolumeZone.Up,
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    pressed = pressed,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line),
                )
                VolumeZoneBox(
                    zone = VolumeZone.Down,
                    icon = Icons.AutoMirrored.Outlined.VolumeDown,
                    pressed = pressed,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line),
                )
                VolumeZoneBox(
                    zone = VolumeZone.Mute,
                    icon = Icons.AutoMirrored.Outlined.VolumeOff,
                    pressed = pressed,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private enum class VolumeZone(val key: RemoteKey) {
    Up(RemoteKey.VolumeUp),
    Down(RemoteKey.VolumeDown),
    Mute(RemoteKey.Mute),
}

@Composable
private fun VolumeZoneBox(
    zone: VolumeZone,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    pressed: VolumeZone?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (pressed == zone) Acid.copy(alpha = 0.10f) else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (pressed == zone) Acid else Paper,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
fun ActionGrid(
    onKey: (RemoteKey) -> Unit,
    onLongStart: (RemoteKey) -> Unit,
    onLongEnd: (RemoteKey) -> Unit,
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
                onLongStart = { onLongStart(RemoteKey.Back) },
                onLongEnd = { onLongEnd(RemoteKey.Back) },
            )
            Keycap(
                Icons.Outlined.Home,
                "Home",
                { onKey(RemoteKey.Home) },
                Modifier.weight(1f),
                haptics = haptics,
                onLongStart = { onLongStart(RemoteKey.Home) },
                onLongEnd = { onLongEnd(RemoteKey.Home) },
            )
            Keycap(
                Icons.Outlined.Menu,
                "Menu",
                { onKey(RemoteKey.Menu) },
                Modifier.weight(1f),
                haptics = haptics,
                onLongStart = { onLongStart(RemoteKey.Menu) },
                onLongEnd = { onLongEnd(RemoteKey.Menu) },
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
                "System",
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
