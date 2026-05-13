# JA2 Reborn

JA2 Reborn is an Android-focused Jagged Alliance 2 port. It is built on top of [Jagged Alliance 2 Stracciatella](https://github.com/ja2-stracciatella/ja2-stracciatella), keeps the original native game engine, and adds an Android launcher, modern build setup, touch controls, Android storage handling, and optional gameplay helpers for mobile play.

This project is a tribute to the original JA2 Stracciatella team and their long-running preservation work. Without that foundation, this Android port would not exist.

The project does not include Jagged Alliance 2 game data. You need a legally owned copy of the original game files.

## Status

Work on this Android port is considered complete. No regular updates are planned. Future updates may happen only if a maintainer personally feels like returning to the project.

Everyone is welcome to fork the project and continue experimenting with it within the terms of the [LICENSE](LICENSE).

The Android port currently supports:

- Release APK builds for all configured Android ABIs
- Android 7.0+ devices, including Android 16
- Android launcher for game data path, save path, resolution, scaling, language, and mouse mode
- Fixed-path game data loading
- Android 11+ all-files access support
- Android 7-10 legacy storage permission fallback
- SDL statically linked into `libja2.so`
- OpenSL ES audio backend tuning for stable playback
- Modern Controls input mode, plus legacy absolute mouse and touchscreen modes
- Modern Controls cursor movement with single-tap left click, two-finger right click, double click, and held-click drag
- Direct tactical bottom-panel touch handling for panel buttons and inventory drag/drop
- Modular in-game touch overlay with editable buttons and JSON persistence
- Bundled default touch overlay preset
- Team-panel portrait touch selection and long-press multi-select
- Sector-exit overlay actions
- Optional in-game tutorial overlay
- Optional cheat system with launcher and in-game overlay controls

## Repository Layout

```text
android/        Android app, Gradle build, launcher, SDL Java bridge
assets/         Distribution assets and bundled data
cmake/          CMake helper modules
dependencies/   Third-party source dependencies used by the native build
docs/           Build and project documentation
rust/           Rust crates used by the Stracciatella engine
src/            Native JA2 Stracciatella engine and Android JNI bridges
```

## Controls

The launcher exposes three input modes:

- `Modern Controls`: uses swipes to move a virtual cursor and taps to click.
- `Absolute mouse` (legacy): maps finger coordinates directly to the game cursor.
- `Touchscreen` (legacy): forwards native touch events.

In Modern Controls mode:

- One-finger tap sends left click.
- Two-finger tap sends right click.
- Quick double tap sends double click.
- Double tap and hold keeps the left mouse button held for drag actions.
- Tactical bottom-panel touches are routed directly to the JA2 interface for panel controls and inventory movement.
- A two-finger tap on the tactical bottom panel toggles team portraits and single-merc inventory view.

The touch overlay is available in the tactical game screen and can be unlocked in-game to edit button layout and actions.

## Configuration Files

Runtime configuration is stored under the app's `.ja2` directory.

Common files:

```text
ja2.json              Launcher/game configuration
touch_buttons.json    Touch overlay layout and settings
cheats.json           Optional cheat configuration
tutorial.set          Tutorial visibility preference
```

## Documentation

- [Android build instructions](docs/BUILDING_ANDROID.md)
- [Android port feature documentation](docs/ANDROID_PORT_FEATURES.md)
- [Release process](docs/RELEASING.md)

## Building

Android build instructions are maintained separately in [docs/BUILDING_ANDROID.md](docs/BUILDING_ANDROID.md).

Short version:

```powershell
cd android
.\gradlew.bat :app:assembleRelease
```

The first build after deleting caches, or any build after SDL Java / CMake integration changes, should use:

```powershell
.\gradlew.bat :app:assembleRelease --rerun-tasks
```

## Game Data

This port expects the user to provide original Jagged Alliance 2 data files. The Android launcher lets you enter the game data directory and save directory manually.

Storage behavior:

- Android 11+ uses all-files access for fixed-path native reads.
- Android 7-10 uses legacy runtime storage permissions.

## Upstream

This repository is based on JA2 Stracciatella. JA2 Reborn is not an official Stracciatella release. The Android port branch starts at the upstream base tag:

```text
android-port-base
```

Local Android port commits are kept on:

```text
ja2-reborn-android-port
```

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for the public change history.

## License

The project source is distributed under the Strategy First Inc. Source Code License Agreement. See [LICENSE](LICENSE).

Third-party dependencies and bundled mods may include their own license files. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Jagged Alliance 2 game assets are not included and remain the property of their respective owners.
