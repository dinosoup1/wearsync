<div align="center">

# WearSync

**Discover which of your phone apps have Wear OS versions**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)

*Privacy-first. Zero data collection. Fully offline.*

</div>

---

## Screenshots

| Loading | Not on Watch | Already Installed | Error |
|---------|-------------|-------------------|-------|
| *Coming soon* | *Coming soon* | *Coming soon* | *Coming soon* |

## Features

- **App Discovery** — Scans your phone apps and compares them with your Wear OS watch
- **One-Tap Install** — Deep-links directly to the Play Store listing for each app
- **Batch Install** — Opens Play Store listings sequentially for all missing apps
- **Pull to Refresh** — Re-scan both phone and watch at any time
- **System Apps Toggle** — Optionally include system apps in the comparison
- **Privacy First** — No network calls, no analytics, no data leaves your device
- **Modern UI** — Material 3 with dynamic color support

## Architecture

```
com.wearsync.app
├── data/              # Repositories for phone and watch package queries
├── domain/            # Models (AppInfo, ComparisonResult) and comparison logic
├── playstore/         # Play Store deep-link intent builder
├── ui/                # Compose screens, theme, ViewModel
│   └── theme/         # Material 3 theme configuration
└── wearable/          # Wearable Data Layer constants

com.wearsync.wear
└── WearSyncListenerService  # Watch-side companion service
```

### How It Works

1. The phone app queries `PackageManager` for all installed non-system apps
2. It sends a message to the paired watch via the Wearable Data Layer API
3. The watch-side `WearSyncListenerService` responds with its installed packages
4. `AppComparisonEngine` compares the two lists and categorizes each app
5. Results are displayed in a tabbed interface with install actions

### Design Principles

- **Security first:** No network calls, no analytics, no data leaves the device
- **Zero data collection:** No user accounts, no telemetry, no crash reporting
- **Minimal permissions:** Only `QUERY_ALL_PACKAGES` and `INTERNET`
- **Offline-capable:** Core discovery works without internet
- **Simple and functional:** Single-purpose app, no feature creep

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Async | Coroutines + Flow |
| DI | Manual constructor injection |
| Watch Comms | Wearable Data Layer API |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

## Build

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- A Wear OS device or emulator (for full functionality)

### Steps

```bash
# Clone the repository
git clone https://github.com/your-username/wearsync.git
cd wearsync

# Build the phone app
./gradlew :app:assembleDebug

# Build the watch companion
./gradlew :wear:assembleDebug

# Run tests
./gradlew :app:test
```

### Install

1. Install the phone APK on your Android phone
2. Install the wear APK on your Wear OS watch
3. Make sure both devices are paired via the Wear OS app
4. Launch WearSync on your phone

## Permissions

| Permission | Purpose |
|-----------|---------|
| `QUERY_ALL_PACKAGES` | Read installed apps on the phone |
| `INTERNET` | Open Play Store links (no other network activity) |

**Explicitly excluded:** Firebase, analytics, crash reporting, ad SDKs, any data collection.

## Privacy Policy

WearSync collects **zero** user data:

- No network requests (except Play Store deep-links)
- No analytics or telemetry
- No crash reporting services
- No user accounts
- No data stored outside the device
- No data shared with third parties
- All processing happens entirely on-device

## Testing

```bash
# Unit tests (AppComparisonEngine, PlayStoreIntentBuilder, ViewModel)
./gradlew :app:test

# All tests with coverage
./gradlew :app:test --info
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for new functionality
4. Ensure all tests pass (`./gradlew :app:test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep dependencies minimal — do not add libraries without discussion
- Maintain the privacy-first approach — no analytics or data collection

## License

```
Copyright 2025 WearSync Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Acknowledgments

Built with [Jetpack Compose](https://developer.android.com/jetpack/compose) and the [Wearable Data Layer API](https://developer.android.com/training/wearables/data-layer).
