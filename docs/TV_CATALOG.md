# TV app catalog endpoint

The Android TV Remote v2 protocol can launch an app by package
(`market://launch?id=<package>`) but it does **not** expose the installed app list or
icons. To show every app with its real icon, VEZZ Remote reads a tiny read-only catalog
from the TV over the local network.

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
- The endpoint is read-only: it lists apps and returns icons. It cannot press keys or
  change settings.
- Optionally reject non-private source addresses (check `InetAddress.isSiteLocalAddress`).
- The phone sends no credentials; the TV's app list is not sensitive, but keep the port
  LAN-only.

## Phone side

- `TvCatalogClient` fetches `/catalog` and `/icon/<package>`.
- `AppIconCache` keeps icons on disk and in memory.
- `MainViewModel.refreshCatalog()` runs after a successful remote connection and from
  the Apps tab refresh button.
- If the endpoint is missing or times out, the bundled `AppCatalog` and gradient letter
  tiles are used and the remote remains fully functional.
