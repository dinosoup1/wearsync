<div align="center">

# ⌚ WearSync

**Find out which of your phone apps have a Wear OS version — and install them in one tap.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/dinosoup1/wearsync)](https://github.com/dinosoup1/wearsync/releases/latest)

*The app Google Play should have built.*

[**⬇️ Download Latest APK**](https://github.com/dinosoup1/wearsync/releases/latest)

</div>

---

## The Problem

You just got a Wear OS watch. Now what? The Play Store has no way to tell you which of your existing phone apps have a watch companion. You either stumble across them by accident or just miss them forever.

WearSync fixes that.

## What It Does

1. **Scans your phone** — reads your installed apps
2. **Asks your watch** — finds out what's already installed
3. **Checks the Play Store** — discovers which missing apps have Wear OS versions
4. **Shows you the list** — tap any app to open its Play Store listing and install

That's it. No accounts, no servers, no BS.

## Screenshots

| Scanning | Found Wear OS Apps | Already on Watch |
|----------|-------------------|-----------------|
| *coming soon* | *coming soon* | *coming soon* |

## Install

### Option 1: Direct APK (sideload)

1. [Download the latest release](https://github.com/dinosoup1/wearsync/releases/latest)
2. Install `app-release.apk` on your **phone**
3. Install `wear-release.apk` on your **watch** (use [Wear Installer 2](https://play.google.com/store/apps/details?id=org.freepoc.wearinstaller2) from the Play Store)
4. Open WearSync on your phone

### Option 2: Google Play *(coming soon)*

We're submitting to the Play Store. When published, the watch companion will install automatically alongside the phone app — no sideloading needed.

### Requirements

- Android 8.0+ (API 26) phone
- A paired Wear OS watch
- Both devices on the same Google account

## Features

- 🔍 **Smart discovery** — checks the Play Store to find Wear OS versions of your apps
- ⚡ **Cached results** — first scan checks the network; subsequent scans are instant
- 📦 **Batch install** — open Play Store listings for all missing apps at once
- 🔄 **Pull to refresh** — rescan any time
- ⚙️ **System apps toggle** — optionally include system apps
- 🎨 **Material 3** — dynamic color, edge-to-edge, dark mode

## Privacy

WearSync is **zero-network by design** except for two specific actions:
1. Checking the Play Store for Wear OS app availability (only the package names of your apps are queried, nothing is sent to any server we control)
2. Opening a Play Store deep-link when you tap "Install"

**We collect nothing.** No analytics, no telemetry, no crash reporting, no accounts, no data stored outside your device.

## How It Works (Technical)

```
Phone                          Watch
  │                              │
  ├─ PackageManager.getInstalled ─┤
  │                              │
  ├─── Wearable Data Layer ──────►│
  │    /wearsync/request         │
  │                              ├─ PackageManager.getInstalled
  │◄──────────────────────────── │
  │    /wearsync/response        │
  │                              │
  ├─ Compare lists               │
  ├─ Check Play Store (parallel) │
  └─ Show results                │
```

```
com.wearsync.app
├── data/
│   ├── PhonePackageRepository   # Reads phone's installed apps
│   ├── WatchPackageRepository   # Queries watch via Wearable Data Layer
│   └── PlayStoreChecker         # Checks Play Store for Wear OS availability
├── domain/
│   ├── AppComparisonEngine      # Pure diff function
│   └── AppInfo / ComparisonResult
├── playstore/
│   └── PlayStoreIntentBuilder   # market:// deep-links
└── ui/
    ├── MainScreen               # Compose UI
    ├── MainViewModel            # UiState management
    └── theme/

com.wearsync.wear
└── WearSyncListenerService      # Responds to package list requests from phone
```

## Build

```bash
git clone https://github.com/dinosoup1/wearsync.git
cd wearsync

# Phone app
./gradlew :app:assembleDebug

# Watch companion
./gradlew :wear:assembleDebug

# Tests
./gradlew :app:test
```

Requires JDK 17 and Android Studio Ladybug (2024.2.1+).

## Permissions

| App | Permission | Why |
|-----|-----------|-----|
| Phone | `QUERY_ALL_PACKAGES` | Read your installed app list |
| Phone | `INTERNET` | Check Play Store + open install links |
| Watch | `QUERY_ALL_PACKAGES` | Read watch's installed app list |

No other permissions. No location, no camera, no contacts, no accounts.

## Contributing

PRs welcome. The most valuable contributions right now:

- [ ] Screenshots for the README
- [ ] Play Store submission (store listing assets, privacy policy page)
- [ ] More reliable Play Store Wear OS detection
- [ ] Improved UI/UX
- [ ] Testing on different Wear OS versions / watches

**Rules:** Keep it privacy-first. No analytics, no Firebase, no ad SDKs. If it makes a network call we didn't intend, it doesn't ship.

1. Fork → feature branch → tests → PR

## License

Apache 2.0 — see [LICENSE](LICENSE)

---

<div align="center">
Built by <a href="https://github.com/dinosoup1">dinosoup1</a> · <a href="https://github.com/dinosoup1/wearsync/releases/latest">Download</a> · <a href="https://github.com/dinosoup1/wearsync/issues">Issues</a>
</div>
