# VEZZ Remote — product context

## Product

A local-only Android phone remote for the HKC/VEZZ “4K SA Google TV” (`lakeside`,
Android 14). It speaks the Android TV Remote protocol v2 over TLS and uses the
CleanYourTV launcher’s `market://launch` handler for app shortcuts.

## Platform

Native Android phone app, Kotlin + Jetpack Compose, minSdk 26, targetSdk 35.
Portrait-first, one hand, 360–430 dp width. Android 8+.

## Audience and scene

One or two people in a dim living room, on the couch, phone in one hand. The TV
is across the room. The physical remote is slow and its buttons need a hard press.
The phone is already unlocked; the app must be useful within one second of opening.

## Job to be done

- Move focus and confirm quickly (D-pad, Back, Home, Menu).
- Change volume and mute without looking away from the TV.
- Type searches and passwords on the phone keyboard.
- Open a TV app in one tap.
- Switch HDMI inputs.
- Pair once; never ask for the TV PIN again on later launches.
- Manage/forget TVs and see which phone identity is paired.

## Constraints

- Android TV Remote v2 does not expose the installed app list or icons. Real app
  icons come from the optional TV catalog endpoint; bundled fallback is available.
- Local network only. No accounts, cloud, telemetry, or ads.
- The remote must remain usable when the TV is off (device management/settings).
- Do not use ADB at runtime.
- Keep the existing protocol, pairing, multi-phone, and app-launch behavior.

## Voice

Terse, technical, adult. Controls name their action. Status is factual. No
marketing slogans, no exclamation marks, no “couch” copy. Uppercase mono labels
for status, measurement, and technical metadata; sentence case for prose.

## Mode

**Operate.** Precision and restraint outrank expression. The design should feel
like a well-made physical control surface, not a landing page or a demo.

## Success

- One-thumb D-pad use with no mispresses.
- Readable in a dark room, but not harsh.
- Typing is faster than the TV remote.
- App launch is one tap.
- The interface looks intentional, not like a generated template.

## Assumptions

- Dark theme only for this version; a light theme is not required.
- Real app icons will replace the letter-tile fallback once the TV catalog is on.
- The TV name is “Main TV”; the active device is shown in the header and Devices.
