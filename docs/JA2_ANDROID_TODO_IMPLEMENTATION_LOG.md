# JA2 Reborn 1.0.5 Android Release Log

Branch: `main`
Stand: 2026-06-28

This log keeps the release-relevant Android 1.0.5 work in one place. Completed planning scratch files were folded into this document so the tree is ready for a later push.

## P1 - Auto-Update Foundation

| Step | Result |
|------|--------|
| Manifest permissions | Added `INTERNET`, `ACCESS_NETWORK_STATE`, `REQUEST_INSTALL_PACKAGES`, and FileProvider setup. |
| GitHub release lookup | Added tolerant release JSON parsing, SemVer comparison, APK asset selection, and download handling. |
| APK verification | Added SHA-256, package name, version code, and signing-certificate checks before install handoff. |
| Installer handoff | Added FileProvider URI generation, install permission flow, and pending install continuation. |
| Tests | Covered update selection, SemVer, digest, verifier, and failure paths in unit tests. |

## P2 - Launcher Integration

| Step | Result |
|------|--------|
| Opt-in flow | Added explicit auto-update opt-in with persisted preferences and rate limiting. |
| Manual check | Added launcher header update button with up-to-date, update-available, no-network, and error feedback. |
| Dialog lifecycle | Added guarded UI callbacks so background update checks do not update a dead activity. |
| Release readiness | Verified update checks against installed 1.0.3, 1.0.5, offline, and no-update cases. |

## P3 - Touch Overlay 1.0.5 Features

| Step | Result |
|------|--------|
| CTRL Examine | Added a CTRL/Examine touch-overlay preset and bundled default button support. |
| Map Screen input | Added Map Screen input mode selection for Direct Touch, Touchpad Mouse, and combined input. |
| Sticky modifiers | Fixed sticky Item Stacking and Sidestep/Backstep toggles blocking later touch actions. |
| Stealth state | Fixed stale Stealth toggle active state after selected merc changes. |
| German labels | Fixed German labels for CTRL Examine and SHIFT Item Stacking. |

## P4 - Touch Preset Reset Policy

| Step | Result |
|------|--------|
| Legacy threshold | Replaced `defaultPresetVersion != DEFAULT_TOUCH_PRESET_VERSION` with a fixed 1.0.4 threshold. |
| Correct stamp | Uses `TOUCH_PRESET_V104_RESET_VERSION = 20260619`, matching the actual v1.0.4 preset stamp. |
| Pre-1.0.4 behavior | Configs with missing or older stamps are reset to the bundled preset and get the reset notice. |
| 1.0.4+ behavior | Configs stamped `20260619` or newer are migrated normally without reset or reset notice. |
| Fresh install behavior | Missing `touch_buttons.json` loads bundled defaults and does not show the reset notice. |
| Commit | `02093740b Fix touch preset reset threshold` |

## P5 - 1.0.5 Start Notice

| Step | Result |
|------|--------|
| New notice | Added a one-time start notice for the new Examine/Untersuchen overlay button. |
| Languages | Added German and English panel text through the existing tutorial language selection. |
| Persistence | Added a separate `touch_overlay_feature_notice.set` state file so it is independent from the reset notice. |
| Priority | Main Menu and Game Screen show the reset notice first, then the feature notice if pending. |
| Android bridge | Added `requestTouchOverlayFeatureNotice()` JNI and Android calls. |
| Commit | `d16513cf8 Add examine touch overlay start notice` |

## Verification

| Command / Check | Status |
|-----------------|--------|
| `git diff --check` | Passed; only Windows line-ending conversion warnings were reported. |
| `:app:compileDebugKotlin` | Passed. |
| `:app:testDebugUnitTest --tests com.ja2.reborn.touch.TouchOverlayConfigMigrationTest` | Passed. |
| `:app:externalNativeBuildDebug` | Passed for all configured ABIs. |
| `:app:assembleRelease` | Passed. |
| `apksigner verify --print-certs` | Passed for `JA2RebornRelease1.0.5.apk`. |

## Release Artifact

Latest local release APK copied to the release handoff folder:

`JA2RebornRelease1.0.5.apk`

Last verified artifact:

| Field | Value |
|-------|-------|
| Size | `100875207` bytes |
| SHA-256 | `E8407824C9A4C758599909D4B92B47C0797E114519E08BDCCF6D92C7144C4FD9` |
| Signer SHA-256 | `833a992d08cdf66f9abcbb239e16419a80c75aba216636e339fa5aff6b67c44d` |

## Push Notes

- `main` contains the 1.0.5 auto-update work, touch-overlay fixes, reset-threshold fix, and Examine start notice.
- `origin/main` is behind the local branch; push only when explicitly requested.
- The only intentionally removed scratch note was `JA2PresetReset.md`; its corrected content is now covered in P4 above.
