package com.rinchhen.vezzremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinchhen.vezzremote.Screen
import com.rinchhen.vezzremote.protocol.RemoteClient
import com.rinchhen.vezzremote.ui.components.StatusDot
import com.rinchhen.vezzremote.ui.theme.VezzGradients
import com.rinchhen.vezzremote.ui.theme.VezzTeal

@Composable
fun VezzRemoteRoot(viewModel: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.Devices) }

    val paired by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discovered by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val active by viewModel.activeDevice.collectAsStateWithLifecycle()
    val remoteState by viewModel.remoteState.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val currentApp by viewModel.currentApp.collectAsStateWithLifecycle()
    val isOn by viewModel.isOn.collectAsStateWithLifecycle()
    val pairing by viewModel.pairing.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val leftHanded by viewModel.leftHanded.collectAsStateWithLifecycle()
    val identityReady by viewModel.identityReady.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()

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
                screen = screen,
                active = active,
                state = remoteState,
                appCount = apps.size,
                onOpenDevices = { screen = Screen.Devices },
                onOpenSettings = { screen = Screen.Settings },
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
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "main-screen",
            modifier = Modifier.padding(padding),
        ) { target ->
            when (target) {
                Screen.Remote -> RemoteContent(
                    device = active,
                    state = remoteState,
                    volume = volume,
                    currentApp = currentApp,
                    isOn = isOn,
                    haptics = haptics,
                    leftHanded = leftHanded,
                    apps = apps,
                    onKey = viewModel::sendKey,
                    onText = viewModel::sendText,
                    onTextAndEnter = viewModel::sendTextAndEnter,
                    onClear = viewModel::clearText,
                    onLaunchApp = viewModel::launchApp,
                    onOpenApps = { screen = Screen.Apps },
                    onReconnect = viewModel::reconnect,
                )

                Screen.Apps -> AppsContent(
                    apps = apps,
                    currentApp = currentApp,
                    onLaunchApp = viewModel::launchApp,
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
                    paired = paired,
                    activeDevice = active,
                    remoteState = remoteState,
                    onHaptics = viewModel::setHaptics,
                    onLeftHanded = viewModel::setLeftHanded,
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
    screen: Screen,
    active: com.rinchhen.vezzremote.data.TvDevice?,
    state: RemoteClient.State,
    appCount: Int,
    onOpenDevices: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val connected = state is RemoteClient.State.Connected
    val title = when (screen) {
        Screen.Remote -> active?.name ?: "VEZZ Remote"
        Screen.Apps -> "Apps"
        Screen.Devices -> "Devices"
        Screen.Settings -> "Settings"
    }
    val subtitle = when (screen) {
        Screen.Remote -> when (state) {
            is RemoteClient.State.Connected -> "Connected · ready"
            is RemoteClient.State.Connecting -> "Connecting…"
            is RemoteClient.State.Error -> "Offline"
            RemoteClient.State.Disconnected -> "Not connected"
        }
        Screen.Apps -> "$appCount apps on ${active?.name ?: "the TV"}"
        Screen.Devices -> if (active != null) "Main TV · ${if (connected) "connected" else "offline"}" else "Pair a TV to begin"
        Screen.Settings -> "Local only · no cloud"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(VezzGradients.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusDot(
                    color = when {
                        connected -> VezzTeal
                        state is RemoteClient.State.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    size = 7.dp,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        HeaderAction(Icons.Default.Tv, "TVs", onOpenDevices)
        HeaderAction(Icons.Default.Settings, "Settings", onOpenSettings)
    }
}

@Composable
private fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.clip(RoundedCornerShape(15.dp)),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun VezzBottomBar(
    current: Screen,
    onSelect: (Screen) -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            BottomItem(Screen.Remote, "Remote", Icons.Default.SettingsRemote, current, onSelect)
            BottomItem(Screen.Apps, "Apps", Icons.Default.Apps, current, onSelect)
            BottomItem(Screen.Devices, "Devices", Icons.Default.Tv, current, onSelect)
            BottomItem(Screen.Settings, "Settings", Icons.Default.Settings, current, onSelect)
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    screen: Screen,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: Screen,
    onSelect: (Screen) -> Unit,
) {
    NavigationBarItem(
        selected = current == screen,
        onClick = { onSelect(screen) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
