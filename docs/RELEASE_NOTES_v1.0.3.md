# JA2 Reborn 1.0.3 Release Notes

JA2 Reborn 1.0.3 focuses on safer Android display defaults, better widescreen presentation, improved touch behavior, and more useful crash diagnostics.

## Highlights

- New launcher resolution presets:
  - Modern: recommended default with readable UI.
  - High Res (More Map): shows more tactical map area with a smaller UI.
  - Retro: fixed classic 640x480 mode.
- Expert Settings now gate manual resolution, scaling, and legacy control mode choices.
- Menus, splash screens, videos, and map screens are presented in a centered 4:3 area so they are no longer stretched on wide displays.
- High Res mode supports larger action panel defaults for phones.
- Retro touch mapping was corrected for bottom-panel controls and merc portrait selection.
- Crash reports now include `crashlog-latest.txt` next to the emergency savegame when possible.

## Compatibility Notes

Existing configs are migrated automatically:

- Old configs without `resolution_mode` and with `640x480` become Retro.
- Other old manual resolutions become Modern unless Expert Settings is enabled.
- Standard mode writes safe defaults for scaling and controls.

Existing savegames remain supported. As before, changing enabled mods or their order can affect modded savegames.

## Verification

The release was locally verified with:

```text
:app:compileDebugKotlin
:app:compileDebugJavaWithJavac
:app:externalNativeBuildDebug
:app:testDebugUnitTest
:app:assembleRelease
```
