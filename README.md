# DarkSwitch

**Force dark mode per app on Android — without root.**

DarkSwitch is a small, open-source Android utility that uses Shizuku and foreground-app detection to attempt system-level force-dark behavior for selected applications. Android and OEM implementations differ, so results can be working, partial, or unsupported.

## Highlights

- Android 10+ and Shizuku; no root required
- Select applications and let DarkSwitch toggle force-dark as they enter the foreground
- Automatic strategy fallback with a remembered strategy per package
- Offline compatibility database bundled as JSON
- One-tap restore for all known switches and saved selections
- In-memory diagnostics only; no network, analytics, account, ads, or cloud

## Setup

1. Install and start Shizuku.
2. Grant DarkSwitch Shizuku permission.
3. Enable DarkSwitch's accessibility service. It only observes foreground package names.
4. Grant notification permission where required.
5. Enable the engine and select applications.

## Compatibility database

Confirmed results live at `app/src/main/assets/compatibility_database.json`. Contributions should include the package, tested Android/device information, status, recommended mode, and concise notes. See `CONTRIBUTING.md` and the compatibility issue template.

## Important limitations

Force Dark is not a universal theming engine. Apps using fixed colors, custom rendering, images, WebViews, or explicit force-dark opt-outs may remain light or show visual defects. A successful shell command means the system accepted the operation; it does not prove the target UI rendered correctly.

## Build

Open the project with a current Android Studio installation and Android SDK 36. The included Gradle wrapper requires network access on first use to download Gradle and dependencies.

## License

Apache License 2.0.
