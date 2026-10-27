# VEZZ Remote — design system

## Direction

**Swiss industrial control surface.** Matte graphite, warm off-white, one signal
lime accent, hairline rules, typographic hierarchy, tactile keys. This replaces the
0.2.0 glass/purple look; treat that version as an anti-reference.

Principles:

1. One accent. Lime means “interactive” or “live”; red means power/fault only.
2. Flat surfaces. No gradients, glass, blur, glows, or decorative shadows.
3. Hairlines and space do the grouping. Cards are not the default container.
4. The D-pad is the hero: physical, matte, precise, with a light haptic tick.
5. Type carries the brand. No icon tile above every heading.
6. Copy is a control label or a fact. No slogans.
7. Real app icons carry the color. Do not tint them or put them on colored tiles.

## Tokens

### Color

| Token | Value | Use |
|---|---|---|
| `ink-0` | `#0A0A0B` | App background |
| `ink-1` | `#121211` | Panels, navigation |
| `ink-2` | `#171716` | Raised keys/inputs |
| `line` | `#2A2A27` | Default hairline |
| `line-strong` | `#3A3A35` | Focused/active hairline |
| `paper` | `#ECEAE3` | Primary text |
| `paper-muted` | `#9A978C` | Secondary text |
| `paper-faint` | `#88867D` | Tertiary/metadata |
| `acid` | `#C7F464` | Accent, live, primary action |
| `on-acid` | `#10120A` | Text on accent |
| `danger` | `#FF5C4D` | Power, offline, destructive |
| `warning` | `#FFB454` | Caution |

No purple, indigo, blue gradient, or tinted glass.

### Type

- **Display/body:** Space Grotesk (variable; weights 400–700).
- **Labels/status/measurement:** IBM Plex Mono (400/500/600).
- Labels: 10–11 sp, uppercase, tracking +0.08 em.
- Body: 14–16 sp, line-height 1.45–1.55.
- Page title: 30–34 sp, bold, tracking −0.02 em.
- Controls: 12–13 sp mono or 15 sp Space Grotesk Medium.

Material roles are mapped to these families; no per-screen ad-hoc sizes.

### Space and shape

- 4 dp base; 8 dp rhythm; 20 dp screen margin; 12/16/24 dp section gaps.
- Radius: 8 dp keys, 12 dp inputs, 16 dp panels, 999 dp circles.
- One radius per role. No 28 dp glass cards.

### Motion

- 120–180 ms, ease-out; press scale 0.96; no bounce, no elastic, no infinite
  pulse. One authored moment per screen is enough.
- Honor the system animation scale where practical; no decorative motion.

## Components

- **Header:** `VEZZ` wordmark (Space Grotesk bold) + `REMOTE` mono label; a live
  status line (`MAIN TV` + dot + `CONNECTED`/`OFFLINE`); two 40 dp square outline
  actions (TVs, settings). No gradient icon tile.
- **Bottom navigation:** Material `NavigationBar` (4 destinations) with a hairline
  top, transparent indicator, lime selected icon/label, mono labels. Keep Android
  back behavior and edge-to-edge insets.
- **Status strip:** current app icon, app name, mono `NOW PLAYING`/`READY`, volume
  number + mono `VOL`, thin 2 dp level line. One panel, not nested cards.
- **Control deck:** one panel split by a 1 dp vertical rule: volume rocker + D-pad.
- **D-pad:** 208 dp matte circle, 1 dp ring, thin arrow keys, 78 dp OK key with
  lime fill and `on-acid` check. Press = darken + 0.96 scale + haptic tick.
- **Volume rocker:** `VOL` mono, +/mute/− keys, 2 dp vertical level line.
- **Action keys:** 52 dp-high keycaps (`BACK`, `HOME`, `MENU`, `INPUTS`, `TV SETTINGS`,
  `POWER`) in two rows; 1 dp line; accent on press.
- **Media:** three 54/70 dp round keys; play is lime.
- **Keyboard/Search:** full-width 52 dp `KEYBOARD` accent key and `SEARCH` outline key.
- **Apps:** real icons 60 dp, 12 dp radius, label 11 sp, no tile behind the icon;
  running app gets a 6 dp lime dot. Letter fallback is a neutral graphite tile.
- **Lists:** hairline-separated rows inside one panel; no card-in-card.
- **Pairing:** modal panel, mono `PAIR TV`, six 48×56 dp code boxes, active box
  lime hairline, `PAIR` accent key, `CANCEL` text.
- **Errors:** one-line mono message + `RECONNECT`; name the recovery, not the stack trace.

## Anti-patterns

Gradients, glass/blur, purple or indigo, rounded-square icon tile above headings,
letter tiles once real icons exist, nested cards, pill-shaped bottom nav indicators,
marketing copy, emoji icons, hard offset shadows, colored side borders >1 dp,
monospace used as decoration rather than data/status, and every element in its own card.

## Platform rules

- Material 3 governs navigation, system back, dialogs, switches, and edge-to-edge
  insets; brand is expressed through color roles, type, shape and motion.
- 48×48 dp minimum touch targets, 8 dp gaps; 4.5:1 text contrast.
- Use sp for type; verify at font scale 1.3.
- Dark theme is the designed theme; no quick inversion.
- Test on the emulator and the physical phones; screenshots from the device, not a browser.

## Verification

- Screenshot the emulator at 360–430 dp, font scale 1.0 and 1.3.
- Check contrast, overflow, 48 dp targets, focus/pressed/disabled states.
- Check the D-pad with one hand; check volume and keyboard reach.
- Impeccable craft-floor: no gradient text, no glass, no lazy cards, no eyebrow/kicker.
- UI UX Pro Max: Swiss style (monochrome, grid, no shadows), 48 dp targets, 8 dp gaps,
  Space Grotesk display, mono labels. Ignore its generic indigo/Inter output.
