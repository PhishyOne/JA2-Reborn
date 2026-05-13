# Android Port Features

This document describes the Android-specific systems added by the JA2 Reborn Android port. It is intended as public project documentation, not as an implementation work log.

## Touch Input

The Android launcher exposes three mouse input modes:

- `Touchscreen`: forwards native touch events to SDL.
- `Absolute mouse`: maps finger coordinates directly to the game cursor.
- `Touchpad mouse`: uses swipes to move a virtual cursor.

Touchpad mouse mode supports mobile-friendly gestures:

- One-finger tap sends a left mouse click.
- Two-finger tap sends a right mouse click.
- Quick double tap sends a double click.
- Double tap and hold keeps the left mouse button pressed for drag actions.
- Bottom-panel touches are routed directly to the tactical interface for buttons, inventory movement, and portrait interaction.
- Long-press on a merc portrait selects the whole active team.

## Touch Overlay

The original fixed Android mouse buttons were replaced by a modular in-game overlay. Overlay buttons are stored as data and can be edited without rebuilding the app.

The overlay provides:

- Floating action buttons rendered above the SDL surface.
- A fixed system button bar for layout editing and utility actions.
- Editable button size, opacity, shape, preset action, and position.
- Drag-to-position behavior when the layout is unlocked.
- Persistent layout storage in `touch_buttons.json`.
- Robust release handling for held mouse, keyboard, combo, and DPAD inputs.
- Import and export of overlay presets.

Overlay button positions and sizes are normalized relative to the screen, so layouts survive different screen sizes and orientation changes.

### Preset Actions

The preset catalog covers common JA2 actions:

- Mouse buttons: left, right, middle.
- Movement and stance actions: stand, crouch, prone, run, stealth, reverse/strafe.
- Combat helpers: fire mode, range cursor, target cycling, auto bandage.
- Tactical UI: map, options, end turn, blink items, pause, quick save/load.
- Merc and squad selection shortcuts.
- Sector-exit controls.
- A configurable DPAD for directional map control.

The DPAD is implemented as a single overlay button with four direction zones. It sends held directional key events and releases them reliably on pointer-up, cancel, pause, or activity teardown.

## Cheat System

The Android port includes an optional cheat system designed for mobile testing and convenience. Cheats are disabled by default and are controlled through both launcher settings and an in-game overlay dialog.

Supported toggles include:

- Master enable switch.
- God mode.
- Non-lethal player damage.
- Full medical healing.
- Unlimited ammo.
- No weapon jams.
- Unlimited action points.
- Unlimited breath.
- Reveal enemies.
- Reveal items.
- One-hit kill.
- Perfect hit chance.

The cheat configuration is stored in `cheats.json`. Runtime changes are sent to the native engine through a JNI bridge.

### Safety Rules

Gameplay hooks are intentionally scoped:

- Player-benefit cheats target `OUR_TEAM` only.
- Enemy, NPC, and militia behavior is not changed unless a feature explicitly requires it.
- Cheat state is not stored in savegames.
- Missing or unknown JSON fields fall back to safe defaults.
- One-shot and advanced cheats should be added through explicit UI and native entry points, not by reusing hidden keyboard shortcuts.

## Tutorial Overlay

The port includes an optional first-run tutorial for Android controls. It is rendered by the native game UI so it matches the existing JA2 visual style.

The tutorial system provides:

- Three slide cards for touch control basics.
- English and German text.
- Dot indicators and previous/next navigation.
- A confirmation button.
- A "do not show again" checkbox.
- A toolbar help button that can reopen the tutorial.

Tutorial visibility is persisted in `tutorial.set` under the game profile directory. The tutorial can auto-open on first tactical screen entry and can also be opened manually from the Android overlay controls.

## Localization

The Android launcher and overlay UI use Android string resources for English and German. The native JA2 game text continues to use the upstream Stracciatella translation data.

Android-side localization covers:

- Launcher configuration labels.
- Settings screens.
- Mouse mode and scaling labels.
- Touch overlay editor and settings dialogs.
- Preset names and action categories.
- Import/export messages.
- Cheat overlay UI.
- Crash and error messages.

The launcher includes a DE/GB language switch. The selected language is stored in app preferences and applied before launcher and game activities create their UI.

## Runtime Configuration

Android-specific runtime files are stored under the app's `.ja2` directory:

```text
ja2.json              Launcher and game configuration
touch_buttons.json    Touch overlay layout and actions
cheats.json           Optional cheat settings
tutorial.set          Tutorial visibility preference
```

These files are user configuration and should not be committed to the repository.

## Manual Verification

Recommended manual checks before publishing a release APK:

- Start with no existing `touch_buttons.json` and verify the default overlay loads.
- Move, resize, edit, delete, and recreate overlay buttons, then restart the app.
- Test left click, right click, drag, double click, and two-finger right click in tactical view.
- Verify DPAD hold and release behavior, including app pause while pressed.
- Toggle layout lock and confirm positions persist.
- Switch launcher language between English and German and inspect launcher, settings, overlay, and cheat dialogs.
- Open the tutorial automatically on first tactical entry and manually through the help button.
- Toggle cheats from launcher and in-game overlay, then verify player-only behavior in tactical gameplay.
- Build a release APK after deleting caches when native, CMake, SDL Java, or Gradle integration changes were made.

