# TV catalog and media status endpoint

The Android TV Remote v2 protocol can launch an app by package
(`market://launch?id=<package>`) but it does **not** expose the installed app list,
icons or now-playing metadata. To show every app with its real icon and the current
media title, VEZZ Remote reads a tiny read-only service from the TV over the local
network.

## Endpoint contract

Port **8080**, plain HTTP, local network only.

### `GET /catalog`

```json
{
  "name": "Main TV",
  "apps": [
    { "package": "com.netflix.ninja", "label": "Netflix", "category": "Media" },
    { "package": "com.android.tv.settings", "label": "Settings", "category": "Tools" }
  ]
}
```

- `name` is the TV's user-facing device name (the phone uses it for the saved device
  instead of the `ATVREMOTE/<mac>` certificate name). Optional on older servers.
- Include only apps with a leanback or launcher entry (apps the user can actually open).
- `category` is `Media` or `Tools`; anything else is treated as `Media`.
- The CleanYourTV implementation removes its own launcher entry, collapses duplicate
  labels (keeping the most recently updated package), and shortens two long labels
  (`MultiMediaPlayer` → `Media Player`, `U-NEXT for TV` → `U-NEXT`).
- The phone matches known favorites by package name, so favorites stay favorites.

### `GET /status`

```json
{
  "title": "I Got BLACKOUT DRUNK on My FIRST Day in Nepal",
  "artist": "TheMancAbroad",
  "state": "PLAYING",
  "package": "org.smarttube.stable",
  "positionMs": 768069,
  "durationMs": 1064000
}
```

- `title`, `artist`, `state`, `package`, `positionMs` and `durationMs` are optional;
  `title` is `null` when there is no active media session.
- The CleanYourTV implementation reads `MediaSessionManager.getActiveSessions()` and
  picks the first playing/paused/buffering session with a title. It needs notification
  listener access:
  `adb shell cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaStatusService`
- Without that grant, `/status` returns `{"title":null,...}` and the phone simply hides
  the now-playing line.

### `GET /icon/<package>`

Returns PNG or WebP bytes for the app icon, ideally 192–256 px square. The phone caches
each icon under its app-private `files/app_icons/` directory, so the TV is only asked
once per package.

### `GET /health`

Returns `ok`; useful while bringing the service up.

## Where to implement it

Preferred: add the server to the CleanYourTV launcher (`vezz-tv-cleanup`), inside the
already-running `HomeKeyService` accessibility service. The launcher already knows every
installed app, its label and its icon. A small `ServerSocket` thread in that service
costs almost nothing and needs no extra notification or TV-side app.

Alternative: a tiny `tv-catalog` companion app with a foreground service (or an
accessibility service) that serves `PackageManager` data. This is more isolated but adds
another APK to the TV.

## Safety

- Bind to the local network; do not expose the port to the internet.
- The endpoint is read-only: it lists apps, returns icons and reports the current media
  session. It cannot press keys or change settings.
- Optionally reject non-private source addresses (check `InetAddress.isSiteLocalAddress`).
- The phone sends no credentials; the TV's app list and now-playing title stay on the
  LAN, but keep the port LAN-only.

## Phone side

- `TvCatalogClient` fetches `/catalog`, `/status` and `/icon/<package>`.
- `AppIconCache` keeps icons on disk and in memory.
- `MainViewModel.refreshCatalog()` runs after a successful remote connection and from
  the Apps tab refresh button. It retries once if the TV has just booted.
- `MainViewModel` polls `/status` every 5 seconds while connected and clears the
  now-playing line when the endpoint returns no title.
- When `/catalog` returns `name`, the phone renames its saved device from the
  `ATVREMOTE/<mac>` certificate name to the TV's real name (`Main TV`).
- If the endpoint is missing or times out, the bundled `AppCatalog` and neutral letter
  tiles are used and the remote remains fully functional.
