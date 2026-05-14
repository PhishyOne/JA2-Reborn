# Android Scaling Plan

This document defines the scaling strategy for JA2 Reborn on Android. The goal is to keep the original JA2 interface readable and touchable on modern phones and tablets without rewriting the native UI.

## Goals

- Keep the original 640x480 UI logic stable.
- Prefer readable tactical controls over showing the largest possible map area.
- Preserve sharp rendering where possible.
- Avoid runtime resolution changes that invalidate native UI state.
- Keep touch-overlay layouts portable across phone, tablet, and landscape aspect ratios.

## Current Strategy

The Android launcher recommends an internal resolution based on the device's physical display. On modern devices, half of the native landscape resolution is used when it remains above the original 640x480 baseline. This gives the renderer enough pixels for clean output while preventing the JA2 interface from becoming too small.

The default video scaling mode is `Near Perfect with Oversampling`. It renders the game to the configured internal resolution and then scales the result to the device screen. This is the recommended default for Android because it balances sharpness, aspect-ratio handling, and full-screen coverage.

The native tactical screen keeps its logical UI model stable while the action panel can be presented larger on Android. Panel scaling is applied during final presentation, after the game has rendered the original panel. Input that lands on the scaled panel is mapped back to the original tactical coordinates before it reaches the native interface.

## Tactical Action Panel Scaling

The tactical action panel supports presentation scaling from 100% to 130%.

Default panel scale is selected by Android device profile:

- Tablets: 100%
- Wide phones, aspect ratio 2.05 or higher: 130%
- Phones, aspect ratio 1.80 or higher: 120%
- Narrower phones: 110%

The panel remains centered and anchored to the bottom of the tactical screen. If the scaled panel is wider than the internal render surface, SDL clipping handles the hidden edges. The tactical viewport is shortened when needed so the enlarged panel does not cover active map rendering.

## Touch Overlay Scaling

The bundled touch overlay uses normalized positions and sizes. This keeps layouts stable across resolutions, aspect ratios, and orientation changes.

Adaptive defaults are applied when a generated bundled layout is first loaded or migrated:

- Tablets receive smaller overlay buttons to avoid oversized controls.
- Narrower phones receive reduced overlay sizes to prevent crowding.
- Very wide phones keep the full default layout because they have enough horizontal space.
- The map DPAD keeps a larger minimum size than regular action buttons.

User-edited layouts are preserved. Migration logic only rewrites layouts that match known generated defaults.

## Runtime Rules

- Launcher resolution and video scaling are persisted in `ja2.json`.
- Touch overlay and action-panel scale are persisted in `touch_buttons.json`.
- Tactical map FOV values are clamped to the supported range, but live native resolution resizing is intentionally disabled during gameplay because existing JA2 UI regions, buttons, mouse regions, and panel geometry are not safe to rebuild mid-session.
- Action-panel scale changes are safe at runtime because they operate at presentation and input-mapping level.

## Implementation Notes

Relevant Android files:

```text
android/app/src/main/java/com/ja2/reborn/LauncherActivity.kt
android/app/src/main/java/com/ja2/reborn/touch/TouchOverlayAdaptiveDefaults.kt
android/app/src/main/java/com/ja2/reborn/touch/TouchButtonStore.kt
android/app/src/main/java/com/ja2/reborn/touch/TouchOverlaySettingsDialog.kt
```

Relevant native files:

```text
src/game/TacticalScaling.cc
src/game/TacticalScaling.h
src/game/TacticalScaling_JNI.cc
src/sgp/Video.cc
src/sgp/Input.cc
src/game/MercPortraitTouch_JNI.cc
```

## Future Work

Future scaling work should stay conservative. The safest improvements are:

- Additional device-profile presets for unusual foldables, desktop modes, and Android handhelds.
- Better preview UI for action-panel and overlay scale before starting the game.
- Optional per-layout profiles for phone and tablet devices.
- More manual verification coverage for Samsung DeX, Chromebooks, and external displays.

Avoid runtime native resolution switching unless the tactical UI can be fully torn down and rebuilt safely. That path touches too much original JA2 state to be treated as a small display feature.
