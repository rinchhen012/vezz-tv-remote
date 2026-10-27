package com.rinchhen.vezzremote.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinchhen.vezzremote.Screen

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

    LaunchedEffect(active?.id) {
        if (active != null) screen = Screen.Remote
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (screen) {
        Screen.Devices -> DeviceListScreen(
            paired = paired,
            discovered = discovered,
            clientName = viewModel.clientName,
            identityReady = identityReady,
            onOpen = viewModel::openDevice,
            onForget = viewModel::forget,
            onManual = viewModel::pairManual,
            onRescan = viewModel::rescan,
            onSettings = { screen = Screen.Settings },
        )

        Screen.Remote -> {
            if (active == null) {
                DeviceListScreen(
                    paired = paired,
                    discovered = discovered,
                    clientName = viewModel.clientName,
                    identityReady = identityReady,
                    onOpen = viewModel::openDevice,
                    onForget = viewModel::forget,
                    onManual = viewModel::pairManual,
                    onRescan = viewModel::rescan,
                    onSettings = { screen = Screen.Settings },
                )
            } else {
                RemoteScreen(
                    device = active,
                    state = remoteState,
                    volume = volume,
                    currentApp = currentApp,
                    isOn = isOn,
                    haptics = haptics,
                    leftHanded = leftHanded,
                    onKey = viewModel::sendKey,
                    onText = viewModel::sendText,
                    onTextAndEnter = viewModel::sendTextAndEnter,
                    onClear = viewModel::clearText,
                    onLaunchPackage = viewModel::launchPackage,
                    onDial = viewModel::launchDial,
                    onSendLink = viewModel::sendAppLink,
                    onReconnect = viewModel::reconnect,
                    onOpenDevices = {
                        viewModel.switchToDevices()
                        screen = Screen.Devices
                    },
                    onOpenSettings = { screen = Screen.Settings },
                )
            }
        }

        Screen.Settings -> SettingsScreen(
            clientName = viewModel.clientName,
            identityStorage = viewModel.identityStorage,
            haptics = haptics,
            leftHanded = leftHanded,
            paired = paired,
            onHaptics = viewModel::setHaptics,
            onLeftHanded = viewModel::setLeftHanded,
            onForget = viewModel::forget,
            onSendLink = viewModel::sendAppLink,
            onBack = { screen = if (active != null) Screen.Remote else Screen.Devices },
        )
    }

    PairingDialog(
        state = pairing,
        onSubmitPin = viewModel::submitPin,
        onRetry = viewModel::retryPairing,
        onCancel = viewModel::cancelPairing,
    )
}
