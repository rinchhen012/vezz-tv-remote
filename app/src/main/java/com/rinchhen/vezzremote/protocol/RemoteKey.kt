package com.rinchhen.vezzremote.protocol

import com.rinchhen.vezzremote.proto.remote.RemoteKeyCode

/** The keys the remote screen uses. Values come from the protocol's RemoteKeyCode enum. */
enum class RemoteKey(val code: RemoteKeyCode) {
    Up(RemoteKeyCode.KEYCODE_DPAD_UP),
    Down(RemoteKeyCode.KEYCODE_DPAD_DOWN),
    Left(RemoteKeyCode.KEYCODE_DPAD_LEFT),
    Right(RemoteKeyCode.KEYCODE_DPAD_RIGHT),
    Ok(RemoteKeyCode.KEYCODE_DPAD_CENTER),
    Back(RemoteKeyCode.KEYCODE_BACK),
    Home(RemoteKeyCode.KEYCODE_HOME),
    Menu(RemoteKeyCode.KEYCODE_MENU),
    Search(RemoteKeyCode.KEYCODE_SEARCH),
    Settings(RemoteKeyCode.KEYCODE_SETTINGS),
    Input(RemoteKeyCode.KEYCODE_TV_INPUT),
    Guide(RemoteKeyCode.KEYCODE_GUIDE),
    Info(RemoteKeyCode.KEYCODE_INFO),
    VolumeUp(RemoteKeyCode.KEYCODE_VOLUME_UP),
    VolumeDown(RemoteKeyCode.KEYCODE_VOLUME_DOWN),
    Mute(RemoteKeyCode.KEYCODE_VOLUME_MUTE),
    Power(RemoteKeyCode.KEYCODE_POWER),
    WakeUp(RemoteKeyCode.KEYCODE_WAKEUP),
    Sleep(RemoteKeyCode.KEYCODE_SLEEP),
    PlayPause(RemoteKeyCode.KEYCODE_MEDIA_PLAY_PAUSE),
    Rewind(RemoteKeyCode.KEYCODE_MEDIA_REWIND),
    FastForward(RemoteKeyCode.KEYCODE_MEDIA_FAST_FORWARD),
    Next(RemoteKeyCode.KEYCODE_MEDIA_NEXT),
    Previous(RemoteKeyCode.KEYCODE_MEDIA_PREVIOUS),
    Delete(RemoteKeyCode.KEYCODE_DEL),
    Enter(RemoteKeyCode.KEYCODE_ENTER),
    Space(RemoteKeyCode.KEYCODE_SPACE),
    Assist(RemoteKeyCode.KEYCODE_ASSIST),
    Hdmi1(RemoteKeyCode.KEYCODE_TV_INPUT_HDMI_1),
    Hdmi2(RemoteKeyCode.KEYCODE_TV_INPUT_HDMI_2),
    Hdmi3(RemoteKeyCode.KEYCODE_TV_INPUT_HDMI_3),
    Hdmi4(RemoteKeyCode.KEYCODE_TV_INPUT_HDMI_4),
}
