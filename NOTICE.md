# Notices and third-party references

VEZZ Remote was written for a personal HKC/VEZZ Google TV. The Android TV
Remote v2 protocol is not officially documented; the following open-source
projects were used as references. No code is copied wholesale except where
noted.

## Protocol and implementation references

- **tronikos/androidtvremote2** — Apache License 2.0. Python implementation of
  the Android TV Remote protocol v2. Used as the source of truth for mDNS,
  pairing, remote-configure, IME and app-link behaviour.
  https://github.com/tronikos/androidtvremote2

- **souravkl11/android-tv-remote** — Apache License 2.0. Kotlin/Compose remote
  with the same protocol. Used as a structural reference for the Kotlin
  pairing/session implementation.
  https://github.com/souravkl11/android-tv-remote

- **louis49/androidtv-remote** — MIT License. Node implementation of the v2
  protocol; referenced through the projects above.
  https://github.com/louis49/androidtv-remote

- **Google Android Open Source Project** — `external/google-tv-pairing-protocol`
  and the Android TV Remote Service. The pairing messages and `remotemessage.proto`
  schema originate from the Android TV Remote protocol (Apache License 2.0).

## Multi-phone pairing

- **iamnbutler/open-tv-remote** — MIT License. The observation that the TV keeps
  one pairing per certificate subject, and the fix of giving every install a
  unique certificate name, guided the per-install identity in this app.
  https://github.com/iamnbutler/open-tv-remote

## Certificate helper

- **Terabithia1572/Vestel-Remote-Android** — MIT License. The small DER X.509
  certificate generator in `data/SoftwareCert.kt` is adapted from this project's
  certificate helper (which in turn credits Gabriel Greco's LGwebOS-menubar).
  Copyright (c) 2024-2026 Gabriel Greco and LGwebOS-menubar contributors.
  Copyright (c) 2026 Yunus İNAN (Android application development and Vestel
  Android TV adaptation).
  https://github.com/Terabithia1572/Vestel-Remote-Android

The full MIT license text for the adapted certificate helper is in
`LICENSES/Vestel-Remote-MIT.txt`.

## Ideas

- **maxhayim/webmote** — MIT License. TV-side accessibility text injection and
  app-list ideas; not shipped in this app.
  https://github.com/maxhayim/webmote

- **maxhayim/cleanyourtv** (Gathin23/cleanyourtv) — MIT License. The CleanYourTV
  launcher's `AppLaunchActivity` handles `market://launch?id=<package>`, which
  makes one-tap app shortcuts possible.
  https://github.com/Gathin23/cleanyourtv
