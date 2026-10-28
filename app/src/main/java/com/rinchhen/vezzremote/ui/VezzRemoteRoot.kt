package com.rinchhen.vezzremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinchhen.vezzremote.Screen
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.ui.components.Hairline
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Ink1
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperFaint
import com.rinchhen.vezzremote.ui.theme.PaperMuted

@Composable
fun VezzRemoteRoot(viewModel: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.Devices) }

    val paired by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discovered by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val active by viewModel.activeDevice.collectAsStateWithLifecycle()
    val remoteState by viewModel.remoteState.collectAsStateWithLifecycle()
    val reconnecting by viewModel.reconnecting.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val currentApp by viewModel.currentApp.collectAsStateWithLifecycle()
    val isOn by viewModel.isOn.collectAsStateWithLifecycle()
    val pairing by viewModel.pairing.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val leftHanded by viewModel.leftHanded.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val identityReady by viewModel.identityReady.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val recentPackages by viewModel.recentPackages.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()

    LaunchedEffect(active?.id) {
        if (active != null && screen == Screen.Devices) screen = Screen.Remote
        if (active == null && screen == Screen.Remote) screen = Screen.Devices
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                active = active,
                state = remoteState,
                reconnecting = reconnecting,
            )
        },
        bottomBar = {
            VezzBottomBar(
                current = screen,
                onSelect = { target ->
                    screen = if (target == Screen.Remote && active == null) Screen.Devices else target
                },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
            label = "main-screen",
            modifier = Modifier.padding(padding),
        ) { target ->
            when (target) {
                Screen.Remote -> RemoteContent(
                    state = remoteState,
                    volume = volume,
                    currentApp = currentApp,
                    isOn = isOn,
                    haptics = haptics,
                    leftHanded = leftHanded,
                    keepScreenOn = keepScreenOn,
                    reconnecting = reconnecting,
                    apps = apps,
                    nowPlaying = nowPlaying,
                    voiceState = voiceState,
                    onKey = viewModel::sendKey,
                    onKeyLongStart = viewModel::sendKeyLongStart,
                    onKeyLongEnd = viewModel::sendKeyLongEnd,
                    onText = viewModel::sendText,
                    onTextAndEnter = viewModel::sendTextAndEnter,
                    onClear = viewModel::clearText,
                    onVoiceStart = viewModel::startVoiceSearch,
                    onVoiceStop = viewModel::stopVoice,
                    onVoiceDismiss = viewModel::clearVoiceError,
                    onReconnect = viewModel::reconnect,
                )

                Screen.Apps -> AppsContent(
                    apps = apps,
                    currentApp = currentApp,
                    recentPackages = recentPackages,
                    onLaunchApp = viewModel::launchApp,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRefresh = viewModel::refreshCatalog,
                )

                Screen.Devices -> DevicesContent(
                    paired = paired,
                    discovered = discovered,
                    activeDevice = active,
                    remoteState = remoteState,
                    clientName = viewModel.clientName,
                    identityReady = identityReady,
                    onOpen = viewModel::openDevice,
                    onForget = viewModel::forget,
                    onManual = viewModel::pairManual,
                    onRescan = viewModel::rescan,
                    onOpenRemote = { screen = Screen.Remote },
                )

                Screen.Settings -> SettingsContent(
                    clientName = viewModel.clientName,
                    identityStorage = viewModel.identityStorage,
                    haptics = haptics,
                    leftHanded = leftHanded,
                    keepScreenOn = keepScreenOn,
                    paired = paired,
                    activeDevice = active,
                    remoteState = remoteState,
                    onHaptics = viewModel::setHaptics,
                    onLeftHanded = viewModel::setLeftHanded,
                    onKeepScreenOn = viewModel::setKeepScreenOn,
                    onForget = viewModel::forget,
                    onOpenDevices = { screen = Screen.Devices },
                    onSendLink = viewModel::sendAppLink,
                )
            }
        }
    }

    PairingDialog(
        state = pairing,
        onSubmitPin = viewModel::submitPin,
        onRetry = viewModel::retryPairing,
        onCancel = viewModel::cancelPairing,
    )
}

@Composable
private fun AppHeader(
    active: com.rinchhen.vezzremote.data.TvDevice?,
    state: RemoteClient.State,
    reconnecting: Boolean,
) {
    val connected = state is RemoteClient.State.Connected
    Column(modifier = Modifier.statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Bottom) {
                    Text(
                        "VEZZ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = Paper,
                    )
                    Text(
                        "REMOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMuted,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(
                        color = when {
                            connected -> Acid
                            state is RemoteClient.State.Error -> MaterialTheme.colorScheme.error
                            else -> PaperFaint
                        },
                        size = 5.dp,
                    )
                    Text(
                        buildString {
                            append(active?.name?.uppercase() ?: "NO TV")
                            append("  ·  ")
                            append(
                                when (state) {
                                    is RemoteClient.State.Connected -> "CONNECTED"
                                    is RemoteClient.State.Connecting -> "CONNECTING"
                                    is RemoteClient.State.Error ->
                                        if (reconnecting) "RECONNECTING" else "OFFLINE"
                                    RemoteClient.State.Disconnected -> "NOT CONNECTED"
                                },
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMuted,
                        maxLines = 1,
                    )
                }
            }
        }
        Hairline()
    }
}

@Composable
private fun VezzBottomBar(
    current: Screen,
    onSelect: (Screen) -> Unit,
) {
    Column {
        HorizontalDivider(color = Line)
        NavigationBar(
            containerColor = Ink1,
            tonalElevation = 0.dp,
        ) {
            BottomItem(Screen.Remote, "Remote", Icons.Outlined.TouchApp, current, onSelect)
            BottomItem(Screen.Apps, "Apps", Icons.Outlined.Apps, current, onSelect)
            BottomItem(Screen.Devices, "Devices", Icons.Outlined.Tv, current, onSelect)
            BottomItem(Screen.Settings, "Settings", Icons.Outlined.Settings, current, onSelect)
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    screen: Screen,
    label: String,
    icon: ImageVector,
    current: Screen,
    onSelect: (Screen) -> Unit,
) {
    NavigationBarItem(
        selected = current == screen,
        onClick = { onSelect(screen) },
        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
        label = { Text(label.uppercase(), style = MaterialTheme.typography.labelSmall) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Acid,
            selectedTextColor = Acid,
            indicatorColor = Color.Transparent,
            unselectedIconColor = PaperFaint,
            unselectedTextColor = PaperFaint,
        ),
    )
}
