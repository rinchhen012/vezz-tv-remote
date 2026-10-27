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

## Typefaces

- **Space Grotesk** — SIL Open Font License 1.1, Florian Karsten. Bundled in
  `app/src/main/res/font/space_grotesk.ttf`.
  License: `LICENSES/SpaceGrotesk-OFL.txt`.
- **IBM Plex Mono** — SIL Open Font License 1.1, IBM. Bundled in
  `app/src/main/res/font/ibm_plex_mono_*.ttf`.
  License: `LICENSES/IBMPlexMono-OFL.txt`.

## Design references

These informed the 0.3.0 Swiss industrial design pass; they are not runtime
dependencies or code copied into the app.

- **nextlevelbuilder/ui-ux-pro-max-skill** — MIT License. Used as a local design
  reference for the Swiss/minimal style, Space Grotesk typography guidance and
  48 dp touch-target rules. Its generic indigo/Inter output was deliberately not
  used.
  https://github.com/nextlevelbuilder/ui-ux-pro-max-skill

- **pbakaus/impeccable** — Apache License 2.0. Used for its craft-floor and native
  Android design critique (no glass/gradient decoration, no lazy cards, explicit
  interaction states, 48 dp targets). Its web detector does not lint Compose.
  https://github.com/pbakaus/impeccable

