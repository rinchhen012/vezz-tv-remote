# VEZZ Remote

A small, local-only Android remote for the **HKC / VEZZ "4K SA Google TV"** in this house. It uses Google's **Android TV Remote protocol v2** over Wi-Fi — the same protocol as the official Google TV app — so there is no IR blaster, Bluetooth pairing, root, or ADB needed at runtime.

**Download:** [VEZZ Remote 0.3.6 APK](https://github.com/rinchhen012/vezz-tv-remote/releases/tag/v0.3.6) (debug-signed sideload build).

The app was built because the supplied Bluetooth remote is slow to wake and its buttons need a hard press. This remote keeps one TLS connection open, has large touch targets, and lets you type on the phone's keyboard.

## What works

- One-time pairing with the 6-character code shown on the TV.
- Reconnects on app launch and on resume **without asking for the code again**.
- Seamless circular D-pad: touch anywhere in the right quadrant for Right, and so on, with a large 84 dp OK center.
- Seamless volume rocker: volume up, volume down and mute zones in one pill.
- Hold volume up/down for slow, deliberate steps; a tap is exactly one step.
- Back, Home, Menu, media, power, inputs and HDMI 1–3 keys.
- Phone keyboard sheet for the TV's focused text field; it focuses the field and opens the phone IME automatically.
- A full app grid with the TV's real app names and icons, served by the CleanYourTV launcher on the TV.
- Launch any app from the grid; Netflix uses DIAL, the rest use the launcher's `market://launch` handler.
- Current app, TV volume, screen state, and **now-playing title/artist/state** in the remote screen.
- mDNS discovery (`_androidtvremote2._tcp`) plus a manual IP fallback.
- Multiple TVs and multiple phones (see below).
- No ads, analytics, accounts or cloud service.

## Status

Tested against the HKC/VEZZ TV:

- Pairing with the on-screen code, and reconnecting after an app restart **without another code**.
- D-pad, Back/Home/Menu/Input/TV settings, volume and mute.
- Seamless D-pad quadrants and center OK verified; volume tap = 1 step, 2.2 s hold = 2 steps; mute toggles.
- Hold volume up/down for slow steps (a tap = 1 step; 2.2 s ≈ 2 steps).
- Current app, volume and screen state reported by the TV.
- Now-playing title, channel and `PLAYING`/`PAUSED` from the TV's media session, with a progress line.
- SmartTube launch through `market://launch`, and Netflix through DIAL.
- Keyboard text into SmartTube's search field from the Vivo's own IME.
- A single idle session stays connected for 6+ minutes; auto-reconnect recovers if the TV or Wi-Fi drops the socket.
- The TV catalog: 18 launchable apps, labels and real 192 px icons; duplicate installs are collapsed and the launcher itself is hidden.
- mDNS discovery of `Main TV` from both real phones.
- **Vivo X200 Pro + Samsung SC-01M paired to the same TV at the same time**, each on its own certificate and LAN address.
- Updating either phone over an existing install kept its pairing and reconnected without a new PIN.
- Prime Video, Disney+, Kodi and Netflix launched from the Vivo grid; the Samsung drove Home while the Vivo stayed connected.

The Apps tab is a searchable grid with categories, favorites and a refresh action. It reads the full app list and real icons from the TV catalog endpoint described in [`docs/TV_CATALOG.md`](docs/TV_CATALOG.md), caching the icons on the phone. Until that endpoint is installed, it falls back to the bundled list and neutral letter tiles.

0.3.6 keeps the Swiss industrial control surface from 0.3.0 and fixes the daily-use details: the TV's real name (`Main TV`) replaces the `ATVREMOTE/<mac>` certificate name, the catalog removes duplicate HDO Box entries and the awkward `MultiMediaPlayer` label, the keyboard sheet opens with the IME ready, and a dropped link reconnects automatically. The idle link stays up without a client heartbeat: the TV treats client messages as activity and stops pinging, so the app relies on the TV's own ping cadence and no fixed socket read timeout. On the narrower Samsung, the status line is just `CONNECTED` and the TV settings key is `SETUP` so neither wraps. 0.3.6 removes the redundant header icons and horizontal volume bar, replaces the circular direction keys with one seamless quadrant D-pad, merges the volume keys into a single rocker with mute at the bottom, and makes held-volume changes deliberately slow so 5/100 does not jump to a loud level. The design brief and tokens are in [`DESIGN.md`](DESIGN.md); the product context is in [`PRODUCT.md`](PRODUCT.md).

## Requirements

- An Android phone with Android 8.0 (API 26) or newer.
- A Google TV / Android TV device running the **Android TV Remote Service**. This TV has it; the service advertises `_androidtvremote2._tcp`.
- Phone and TV on the same local network. mDNS must be allowed for automatic discovery; otherwise add the TV by IP.
- For the one-tap app shortcuts, the TV needs a launcher that handles `market://launch?id=<package>`. The **CleanYourTV launcher** installed on this TV does (its `AppLaunchActivity`), and so does the stock Google TV launcher.
- For the now-playing line, the TV launcher needs notification-listener access. Grant it once with the command in [`docs/TV_CATALOG.md`](docs/TV_CATALOG.md); without it the rest of the remote still works.

## Install (sideload)

1. Copy `dist/vezz-remote-0.3.6.apk` to the phone (or use `adb install -r`).
2. Open the APK and allow installs from that source when prompted.
3. Turn the TV on and make sure the phone is on the same Wi-Fi.

```bash
adb install -r dist/vezz-remote-0.3.6.apk
```

The APK is signed with the standard Android debug key. That is fine for a personal sideload; it is not a Play Store release.

## Pair once

1. Open VEZZ Remote. It lists TVs found over mDNS; if the TV is not listed, enter its IP address under **Add by IP**.
2. Tap **Pair**. The TV shows a six-character code under the client name `Vezz Remote (… fingerprint …)`.
3. Type the code in the app and tap **Pair**.

Pairing creates a client certificate on the phone and stores it in Android Keystore. The TV remembers the certificate. Later launches use the saved certificate and the pinned TV certificate fingerprint; the PIN is not requested again.

### When do I need to pair again?

- The app is uninstalled or its data is cleared.
- You move to a new phone, or restore the app without its Keystore key.
- The TV is factory reset, its Android TV Remote Service data is cleared, or the phone is removed from the TV's paired devices.

App updates, phone reboots, TV standby and TV reboots do **not** require re-pairing. If the TV is in deep standby it may not be reachable; wake it with the physical remote, then tap **Reconnect** (or just reopen the app).

## Multiple TVs and multiple phones

The TV keeps **one pairing per certificate subject (client name)**. Some remote libraries use the same hard-coded client name on every install, so pairing a second phone knocks the first one off. This app gives every install a unique name, for example:

```
Vezz Remote (vivo X200 Pro 3F2A)
Vezz Remote (samsung SM-N975F 91C0)
```

As a result:

- One phone can pair with several TVs; each TV is stored with its own pinned server certificate.
- Several phones can pair with the same TV and stay paired. Each phone has its own certificate and its own one-time PIN.
- Reinstalling on one phone creates a new identity and needs one new pairing; it does not disturb the other phones.

The TV may impose its own limit on paired clients. If a very old phone needs to be removed, use the TV's remote/device settings or reset the app's identity on that phone and pair again.

## App shortcuts

`Android TV Remote` has no general "start package" command. The practical method on this TV is:

```
market://launch?id=<package>
```

The CleanYourTV launcher's `AppLaunchActivity` resolves that to the app's TV launch intent. The app uses it for the shortcuts above; Netflix uses the TV's DIAL service (`http://<tv>:8008/apps/Netflix`) instead.

If an app does not open:

- Check that the package name is installed on the TV.
- Try opening it once with the TV home screen; some apps only expose a launcher entry after first run.
- Use the D-pad as a fallback.

## Keyboard

The keyboard sheet sends text through the remote protocol's IME messages. **Focus a text field on the TV first** (for example, the search box in SmartTube or YouTube), tap **Keyboard**, then type and press **Send** or **Send + Enter**. The sheet now focuses its own text field and opens the phone IME by itself.

Some TV apps draw their own keyboard and cannot receive text this way. In that case, use the app's own keyboard with the D-pad, or use the TV's voice search. The remote protocol does not expose a universal text field.

## Build

Requirements: JDK 17 and Android SDK platform 35 (build-tools 35 or newer).

```bash
export JAVA_HOME=/path/to/jdk-17
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest
```

`local.properties` must point at the Android SDK. It is intentionally gitignored.

## Layout

```
app/src/main/java/com/rinchhen/vezzremote/
├── data/          Client identity (Android Keystore / software fallback), TV store
├── discovery/     NsdManager mDNS discovery
├── protocol/      TLS, Polo pairing, remote-v2 session, DIAL
└── ui/            Compose screens and remote controls
app/src/main/proto/  Pairing and remote protobuf schemas
docs/                TV app catalog contract
PRODUCT.md           Product context and operating scene
DESIGN.md            Swiss industrial design system and tokens
LICENSES/            Apache-2.0, MIT, and font OFL texts
```

## Security and privacy

- All control traffic is local, over TLS.
- The TV's self-signed certificate is pinned (SHA-256) after pairing.
- The phone's private key is held in Android Keystore when available; the fallback is an app-private software key.
- The app has no internet backend, analytics, ads or accounts.
- DIAL uses plain HTTP on the local network for the Netflix shortcut. The TV exposes that service on port 8008; no credentials are sent.
- The app does not enable or require ADB at runtime. Network ADB on the TV can be turned off after setup.

## Known limitations

- The remote service must be reachable. A TV that powers its Wi-Fi down in deep standby cannot be woken by this app; use the physical remote, HDMI-CEC or Wake-on-LAN.
- App shortcuts depend on the launcher handling `market://launch`. The CleanYourTV launcher does; a third-party launcher may not. Netflix uses DIAL first.
- The catalog endpoint is part of the CleanYourTV launcher distributed alongside this app. Without it, the Apps tab falls back to the bundled shortcuts.
- Some apps ignore IME text; see the keyboard section.
- This app is not affiliated with Google, HKC or VEZZ. Android TV and Google TV are trademarks of Google LLC.

## Credits

See [`NOTICE.md`](NOTICE.md) for protocol references and license notices.
