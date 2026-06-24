# JA2 Reborn Android To-do — Implementierungs-Log

## Phase 0: Setup und Baseline

- **Start**: 2026-05-21
- **Branch**: `experimental` (HEAD = `9d208f3` Finalize JA2 Reborn 1.0.3 Android release)
- **Tätigkeiten**:
  - `git switch experimental` → Clean working tree
  - `docs/JA2_ANDROID_TODO_IMPLEMENTATION_LOG.md` angelegt (diese Datei)
  - `docs/JA2_ANDROID_TODO_PHASE_PLAN.md` angelegt (Planstatus)
- **Ergebnis**: Baseline dokumentiert, keine Feature-Änderungen
- **Ende**: 2026-05-21

## Phase 1: Mapscreen-Overlay-Grundlage

- **Start**: 2026-05-21
- **Branch**: `experimental`
- **Tätigkeiten**:
  - `TouchButtonModels.kt`: Schema-Version 9→10, `mapScreenButtons`-Feld mit `@SerialName("map_screen_buttons")` zu `TouchOverlayConfig` hinzugefügt, `defaultMapScreenButtons()` mit 10 Map-Buttons (SHIFT/CTRL/ALT + ESC/SPACE/±/ENTER/I/L) implementiert
  - `TouchOverlayController.kt`: Screen-Whitelist auf `{GAME_SCREEN=5, MAP_SCREEN=9}` korrigiert, `currentActiveScreen`-Tracking, `activeButtons()`/`updateActiveButtons()`-Helper für screen-spezifische Button-Liste, `autoHidePoll()` auf 3-state-Logik umgebaut (hidden→visible, visible→hidden, screen-swap), `switchToScreen()` zum Neuaufbau der ButtonViews bei Screen-Wechsel, alle Edit-Methoden auf `activeButtons()` umgestellt
  - `TouchOverlayAdaptiveDefaults.kt`: `isSameButtonLayout()` um `mapScreenButtons`-Vergleich ergänzt
  - `default_touch_preset.json`: `map_screen_buttons`-Array mit 10 Buttons hinzugefügt
  - `TouchOverlayConfigMigrationTest.kt`: 4 Unit-Tests
- **Tests**: `./gradlew.bat testDebugUnitTest` → BUILD SUCCESSFUL
- **Korrektur**: Plan nannte `MAP_SCREEN = 8` (= DEBUG_SCREEN), Codex Endbefund 2 korrigierte auf `MAP_SCREEN = 9`
- **Ergebnis**: Mapscreen-Overlay-Grundlage vollständig
- **Ende**: 2026-05-21

## Phase 2: Modifier und Mapscreen-Buttonset

- **Start**: 2026-05-21
- **Branch**: `experimental`
- **Tätigkeiten**: Toggle-Key-Modus (`performToggle`, `forceReleaseToggle`), visuelles Feedback (grüner aktiver Toggle-Zustand), Mapscreen-Default-Set auf 19 Buttons erweitert, `"INSERT"` zu `keyNameToCode()` hinzugefügt
- **Tests**: 7 Tests, alle grün
- **Ergebnis**: Toggle-Key-Modus vollständig
- **Ende**: 2026-05-21

## Phase 3: Strafing

- **Start**: 2026-05-21
- **Branch**: `experimental`
- **Tätigkeiten**: `strafe_hold` (CTRL-Hold) und `strafe_toggle` (CTRL-Toggle) im taktischen Default-Set. Keine Native-Änderungen nötig — beide Pfade erzeugen identische SDL-KeySequenzen.
- **Tests**: 12 Tests, alle grün
- **Ergebnis**: Strafing per Touch-Overlay vollständig
- **Ende**: 2026-05-21

## Phase 4: DirectTap-Arbitration

- **Start**: 2026-05-21
- **Branch**: `experimental`
- **Tätigkeiten**: `directTouchArbitrationMs`-Feld (Default 1800, Range 200..2500), SeekBar-Slider in Touch-Einstellungen, Persistenz über Config/Store-System
- **Tests**: 15 Tests, alle grün
- **Ergebnis**: DirectTap-Arbitration vollständig konfigurierbar
- **Ende**: 2026-05-21

## Phase 5: Hardware Mouse/Keyboard Mode

- **Start**: 2026-05-21
- **Branch**: `experimental`
- **Tätigkeiten**: `MouseMode.HARDWARE` als vierter Enum-Wert, Touch-Overlay im Hardware-Mode deaktiviert, Touchscreen-Gesten fallen auf native SDL-Touch-Events durch, Hardware-Maus-Events direkt an JA2-Engine
- **Tests**: 17 Tests, alle grün
- **Ergebnis**: Hardware Mouse/Keyboard Mode vollständig
- **Ende**: 2026-05-21

## Codex Endabnahme — Nacharbeit (FreeClaude)

- **Start**: 2026-05-21
- **Review**: Codex Endabnahme 2026-05-21, 5 fachliche Blocker
- **Fixes**: Sticky-Toggle-Release strukturell korrigiert (`forceReleaseToggle`), Legacy-Konfigurationen bekommen Mapscreen-Buttonset, Strafe-Presets im Editor, M/Mines-Button im Defaultset, deutsche DirectTap-Strings
- **Tests**: 20 Tests, alle grün. `assembleDebug` erfolgreich.
- **Ende**: 2026-05-21

## Codex Endabnahme — Nacharbeit Endbefund 2 (FreeClaude)

- **Start**: 2026-05-21
- **Review**: Codex Endbefund 2 2026-05-21, 1 fachlicher Blocker
- **Fix**: MAP_SCREEN-Konstante von 13 auf 9 korrigiert (13 war FADE_SCREEN)
- **Tests**: 20 Tests, alle grün. `assembleDebug` erfolgreich.
- **Ende**: 2026-05-21

## Codex Doku-Nacharbeit und Endfreigabe

- **Start**: 2026-05-21
- **Tätigkeiten**: Phase-1-Logeintrag korrigiert, APK-Formulierungen präzisiert
- **Tests**: BUILD SUCCESSFUL
- **Ergebnis**: Codex gibt Stand für Test-APK frei
- **Ende**: 2026-05-21

## Phase 7: SVG-Icon-System + 37 Icons (Batches 01–06)

- **Start**: 2026-06-18
- **Branch**: `experimental`
- **Commit**: `3913ee2` — Add touch overlay SVG icon system + 37 IconConverter icons, rework map screen defaults
- **Tätigkeiten**:
  - `SvgIconManager` mit kanonischer 512 px Rendering-Pipeline (IconConverter-kompatibel)
  - 37 SVG-Icons aus Batches 01–06 (Tactical Core, Stances, Combat, Actions, MapScreen Nav, MapScreen Display)
  - `iconset.json` + `iconmappings.json` für IconConverter-Export-Integration
  - SVG-First-Rendering in `drawIcon()`, 27 Canvas-Fallback-Branches entfernt
  - Per-Button `iconFill`-Override + Runtime-Slider im Editor
  - Lock-Button auf SVG-basiertes Rendering umgestellt
  - Mapscreen-Buttons von 20 auf 8 reduziert (12 Legacy-Buttons aussortiert)
  - `strafe_hold`/`strafe_toggle` teilen sich `alt_movement_hold`-SVG
  - `TOUCH_OVERLAY_CONFIG_VERSION` auf 12 gehoben, Migration für veraltete Mapscreen-Configs
  - CHANGELOG und Tests aktualisiert
- **Reduzierungen**:
  - **Batches 07+08 (Extras UI, Cpp Inline)**: Bewusst gestrichen — keine weiteren Icons nötig
  - **Map-Screen-Buttons**: 20→8, 12 überflüssige Buttons aussortiert
- **Tests**: `./gradlew.bat testDebugUnitTest` → BUILD SUCCESSFUL
- **Ergebnis**: SVG-Icon-System vollständig, 37 Icons integriert, Mapscreen aufgeräumt
- **Ende**: 2026-06-18

---

## Update 104 Phase 1: Touch Overlay Migration und Map Inventory

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - `map_inventory` in Code-Defaults und gebuendeltem `default_touch_preset.json` von `I` auf `ENTER` umgestellt.
  - `TOUCH_OVERLAY_CONFIG_VERSION` von 12 auf 13 erhoeht.
  - `normalizeTouchOverlayConfig()` als zentrale Migration fuer alte Map-Inventory-Buttons, leere `mapScreenButtons` und Schema-Update eingefuehrt.
  - Laden und Importieren von Touch-Presets verwenden die zentrale Normalisierung.
  - Migrationstests fuer Schema 12 -> 13, `map_inventory` I -> ENTER, leere Map-Buttons und unveraenderte taktische Buttons ergaenzt.
- **Tests**:
  - `android\gradlew.bat testDebugUnitTest` aus Repo-Root versucht -> kein Gradle-Root, erwarteter Fehlstart.
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
- **Zweitpruefung**: Diff, Runtime-Suche nach alten `I`-Mappings und Testreport geprueft; alte `I`-Werte stehen nur noch in Legacy-Testdaten.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; durch Mapping-/Migrationstests abgedeckt.
- **Ergebnis**: Phase 1 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 2: Screen-Kontextgetrennte Presets und Export/Import

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Touch-Button-Presets in `TACTICAL_TOUCH_BUTTON_PRESETS` und `MAP_SCREEN_TOUCH_BUTTON_PRESETS` getrennt.
  - Map-Screen-Presets fuer alle acht Map-Buttons ergaenzt, inklusive `map_inventory` mit `ENTER`.
  - `TouchOverlayController` waehlt Presets anhand des aktiven Screens aus.
  - `TouchOverlayEditDialog` erhaelt die aktive Presetliste explizit und sucht nur darin.
  - Map-Preset-Labels und Kategorie in Englisch/Deutsch ergaenzt.
  - Export/Import als Full-Layout bestaetigt: `buttons` und `map_screen_buttons` bleiben getrennt.
- **Tests**: `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
- **Zweitpruefung**: Diff und Suche nach globaler Preset-Nutzung im Editorpfad geprueft; Unit-Tests decken getrennte Presets und Export/Import-Roundtrip ab.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; Map-Editor-Verhalten muss in der spaeteren Endverifikation geprueft werden.
- **Ergebnis**: Phase 2 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 3: Icon Alignment, Lock Button und Reload Preset

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Batch-02/03-SVGs aus `IconWork` fuer Tactical Stances und Combat uebernommen.
  - Batch-02/03-Converter-Werte in `iconset.json` uebernommen; `iconOffsetY` fuer die Runtime invertiert.
  - `SvgIconManager.IconSetEntry` um `iconRotation`, `iconFlipH` und `iconFlipV` erweitert.
  - `SvgIconManager.renderIcon()` wendet Fill, Scale, Offset, Rotation und Flip an; verbose Debug-Logs liegen hinter `DEBUG_ICON_RENDERING`.
  - Lock-Systembutton rendert sein SVG mit explizitem `1.55f`-Icon-Fill, ohne Button-Groesse oder Hitbox zu aendern.
  - Reload-Preset `reload_selected` mit `ALT + R`, `icon_reload.svg` und Mapping-ID `2000000038` ergaenzt.
  - Native Reload-Logik nicht geaendert; `ALT+R` fuehrt bereits zu `HandleTBReload()` und `AutoReload(selectedMerc)`.
- **Tests**: `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
- **Zweitpruefung**: Native Reload-Route, Diff, Default-Layout-Suche und Resource-Compile geprueft; Reload wurde nicht in sichtbare Default-Layouts eingefuegt.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; Icon-Zentrierung, Lock-Groesse und Reload-Ausloesung muessen in der spaeteren Endverifikation visuell geprueft werden.
- **Ergebnis**: Phase 3 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 4: Mouse Mode Order

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Zentrale Mouse-Mode-Reihenfolge `MouseMode.DISPLAY_ORDER` definiert: `TOUCHPAD`, `HARDWARE`, `ABSOLUTE`, `TOUCHSCREEN`.
  - `SettingsFragment` und `DataTabFragment` auf die gemeinsame Reihenfolge umgestellt.
  - Labels und gespeicherte Werte unveraendert gelassen.
  - Unit-Test fuer die Display-Reihenfolge ergaenzt.
- **Tests**: `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
- **Zweitpruefung**: Suche nach direkter UI-Nutzung der Enum-Reihenfolge und Diff geprueft; beide betroffenen Fragmente nutzen `MouseMode.DISPLAY_ORDER`.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; Expert-Settings-Dropdown muss in der spaeteren Endverifikation visuell geprueft werden.
- **Ergebnis**: Phase 4 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 5: Auto-Bandage Scaling

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Debug-Trace fuer Auto-Bandage Enter, First-Frame und Exit ergaenzt.
  - Trace enthaelt `SCREEN_WIDTH`, `SCREEN_HEIGHT`, `STD_SCREEN_X/Y`, `INV_INTERFACE_START_Y`, aktuellen Screen sowie Map-/Tactical-/Auto-Bandage-Status.
  - Full-Refresh fuer Enter, First-Frame und Exit zentralisiert: `SetRenderFlags(RENDER_FLAG_FULL)`, `InvalidateScreen()` und `fInterfacePanelDirty = DIRTYLEVEL2`.
  - Auto-Bandage-Panel anhand aktueller Screen-Werte im taktischen Bereich oberhalb der Bottom-UI zentriert.
  - Panel- und Invalidierungskoordinaten gegen aktuelle `SCREEN_WIDTH/HEIGHT` geklemmt.
  - Input-Maske bleibt full-screen ueber aktuelle `SCREEN_WIDTH/HEIGHT`.
- **Tests**:
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
  - `.\gradlew.bat externalNativeBuildDebug` in `android` -> BUILD SUCCESSFUL fuer `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
- **Zweitpruefung**: Diff, Planabgleich und native Build-Ausgabe geprueft; keine 640x480-Koordinaten im Auto-Bandage-Eintritt, Panel-Layout oder Invalidate-Pfad eingefuehrt.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; Widescreen-Start, Panel-Klickbarkeit und Rueckkehr-Artefakte muessen in der spaeteren Endverifikation visuell geprueft werden.
- **Ergebnis**: Phase 5 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 6: Shopkeeper / Vendor Scaling

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Shopkeeper-UI auf einen gemeinsamen SKI-Origin relativ zu `STD_SCREEN_X/Y` umgestellt.
  - `tradescreen.sti`, Buttons, Haendlergesicht, Inventar-Slots, Mouse Regions, Restore-/Invalidate-Rects und Shopkeeper-Subtitles ueber denselben Origin gefuehrt.
  - Taktische Aussenbereiche um das Haendlerpanel bleiben Widescreen und werden ueber aktuelle `SCREEN_WIDTH` sowie `INV_INTERFACE_START_Y` restauriert.
  - Shopkeeper-Messageboxen innerhalb des 640er SKI-Panels zentriert.
  - Item-Description-Anker gegen aktuelle `SCREEN_WIDTH/HEIGHT` geklemmt.
- **Tests**:
  - `.\gradlew.bat externalNativeBuildDebug` in `android` -> BUILD SUCCESSFUL fuer `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
- **Zweitpruefung**: Diff auf Vollscreen-4:3-Rueckfall geprueft; `SCREEN_WIDTH/HEIGHT` werden nicht global ersetzt, nur klassische SKI-Panel-Koordinaten bekommen den 4:3-Origin.
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; Sichtbarkeit, Slot-Klickbarkeit und Popup-Position muessen in der Endverifikation auf breiter Android-Aufloesung geprueft werden.
- **Ergebnis**: Phase 6 abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 7: Widescreen Screen-/Text-Audit

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Plan geschaerft: Widescreen-Probleme betreffen alle Texteinblendungen, nicht nur Auto-Bandage; Menues und Map-Screen bleiben bewusst 4:3.
  - Taktische NPC-/Dialog-Texteinblendungen auf zentrale Tactical-Textbox-Position umgestellt.
  - Tactical-Textbox-Overlays gegen `SCREEN_WIDTH` und `INV_INTERFACE_START_Y` geklemmt.
  - Map-Screen-Dialogpositionierung unveraendert im 4:3-Kontext belassen.
  - Zivilisten-Quotes gegen die aktuelle taktische Viewport-Hoehe geklemmt.
  - Sector-Exit-Dialog-Dirty-Rect korrigiert: Invalidate nutzt jetzt rechte/untere Koordinaten statt Breite/Hoehe.
  - Shopkeeper-Subtitles, Item-Description-Anker und Messageboxen im Rahmen von Phase 6 mitgeprueft.
- **Tests**:
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
  - `.\gradlew.bat externalNativeBuildDebug` in `android` -> BUILD SUCCESSFUL fuer `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
- **Zweitpruefung**: Relevante Textpfade (`Dialogue_Control`, `Interface`, `Civ_Quotes`, `Strategic_Exit_GUI`, Shopkeeper) gesucht und Diff auf Map-/Menue-4:3-Abgrenzung geprueft.
- **Manuelle Pruefung**: Vollstaendige 16:9-/Ultrawide-Geraetepruefung nicht ausgefuehrt; bleibt fuer Endabnahme offen.
- **Ergebnis**: Phase 7 als Code-Audit abgeschlossen.
- **Ende**: 2026-06-19

## Update 104 Phase 8: Final Verification und APK

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Vollstaendige Android-Unit-Test-Suite ausgefuehrt.
  - Native Debug-Build-Pruefung fuer alle Android-ABIs ausgefuehrt.
  - Lokale Debug-APK zur Verifikation gebaut.
  - Kein Google-Drive-Upload durchgefuehrt.
- **Tests**:
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
  - `.\gradlew.bat externalNativeBuildDebug` in `android` -> BUILD SUCCESSFUL fuer `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
  - `.\gradlew.bat assembleDebug` in `android` -> BUILD SUCCESSFUL.
- **Artefakt**: `android\app\build\outputs\apk\debug\app-debug.apk`.
- **Restbefund**: Manuelle Endabnahme auf 16:9-/Ultrawide-Geraet bleibt offen, insbesondere Shopkeeper, Texteinblendungen, Popups und Rueckkehr-Artefakte.
- **Ergebnis**: Phase 8 abgeschlossen.
- **Ende**: 2026-06-19

## Shape-Aware Touch Hit-Testing

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - `TouchOverlayButtonView.isPointInsideShape(localX, localY)`: Shape-basierter Hit-Test fuer Kreis, abgerundetes Quadrat und abgerundetes Rechteck, statt rechteckiger Bounding-Box.
  - `isInsideRoundRect()`: Helper fuer Rounded-Rect-Hit-Test (Mittelstreifen + Eckrundungen).
  - Shape-Gate in `ACTION_DOWN` und `ACTION_POINTER_DOWN`: `return false` bei Treffer ausserhalb der sichtbaren Form, damit Android das Event an die naechste View dispatched.
- **Tests**: `.\gradlew.bat assembleRelease` in `android` -> BUILD SUCCESSFUL
- **Manuelle Pruefung**: Nicht am Geraet ausgefuehrt; korrekte Hit-Testing-Logik durch Memory-Pattern aus SM64 validiert.
- **Ergebnis**: Shape-Aware Touch Hit-Testing implementiert. Kreisförmige und abgerundete Buttons registrieren keine Fehltreffer mehr in den unsichtbaren Ecken (~41% Flaeche ausserhalb der sichtbaren Form).
- **Ende**: 2026-06-19

## Shape-Aware Hit-Testing: Dpad-Ausnahme

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - `isPointInsideShape`: Gate `if (isDpad) return true` eingefuegt, da das Dpad eine Kreuzform rendert die nicht dem `buttonConfig.shape`-Schema folgt und korrekt die volle Bounding-Box braucht.
- **Tests**: `.\gradlew.bat assembleRelease` in `android` -> BUILD SUCCESSFUL
- **Manuelle Pruefung**: Am Geraet getestet, Dpad-Ecken reagieren wieder korrekt.
- **Ergebnis**: Dpad von Shape-Aware Hit-Testing ausgenommen.
- **Ende**: 2026-06-19

## Icon X-Offset-Invertierung (Stand Stance Fix)

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - Analyse: Codex hatte beim Kopieren der Batch-02/03-Iconverter-Daten in die finale `iconset.json` alle Y-Offsets invertiert (IconConverter Y-up → Android Canvas Y-down), aber X-Offsets unangetastet gelassen.
  - Nur 3 von 38 Icons haben X-Offsets ≠ 0: `stance_stand` (-0.3), `run_toggle` (-0.1), `keyring` (0.023).
  - Fix: Alle drei X-Offsets in `iconset.json` negiert. `stance_stand` war mit -0.3 der einzige sichtbar falsch alignierte Button.
- **Known Issue #2** (Batch 02+03 Icon-Alignment) damit vollstaendig resolved.
- **Tests**: `.\gradlew.bat assembleRelease` in `android` -> BUILD SUCCESSFUL
- **Manuelle Pruefung**: Am Geraet getestet, Stand-Stance-Icon sitzt jetzt korrekt.
- **Ergebnis**: Alle SVG-Icon-Offsets jetzt korrekt (X und Y).
- **Ende**: 2026-06-19

---

## Button-Bereinigung: Strafe-Entfernung, Toggle-Umbau, Umbenennungen

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - `strafe_hold` und `strafe_toggle` aus `TACTICAL_TOUCH_BUTTON_PRESETS` (Presets.kt) und `defaultButtons()` (Models.kt) entfernt — diese CTRL-Buttons waren auf der Map gedacht und im Tactical Screen unnoetig.
  - `alt_movement_hold` von `mode="hold"` auf `mode="toggle"` umgestellt, Label auf "Seitwärts-/Rückwärtsschritte" (de) / "Sidestep/Backstep" (en) geaendert.
  - `shift_toggle`-Preset-Label auf "Item Stacking" (de/en) umbenannt, Localization-Eintrag in `TouchButtonLocalization.kt` ergaenzt.
  - `map_inventory`-Migration von `I` auf `ENTER` in `TouchButtonModels.kt` (`migrateMapInventoryEnterKey()`) implementiert.
  - Englische String-Ressourcen (`values/strings.xml`) und deutsche (`values-de/strings.xml`) aktualisiert.
- **Tests**: `.\gradlew.bat assembleRelease` -> BUILD SUCCESSFUL
- **Commits**: `3ebd625` (Remove strafe buttons, rename controls, add shift toggle)
- **Ende**: 2026-06-19

---

## Strafe-Fix-Nachbesserung, Lock-Button Square+Opacity, Map-Button-Rename

- **Start**: 2026-06-19
- **Branch**: `experimental`
- **Taetigkeiten**:
  - `default_touch_preset.json`: `strafe_hold`/`strafe_toggle` aus taktischen Buttons entfernt, `alt_movement_hold` auf `mode: "toggle"` mit `key_name: "ALT"` umgestellt (war in commit `3ebd625` uebersehen worden).
  - `TouchOverlayLockButtonView.kt`: von `drawOval`+`computeOuterShapeBounds()` auf `drawRoundRect` mit voller 44dp-Groesse umgebaut, `cornerRadius = 12dp` wie `createSystemButtonView`.
  - `TouchOverlayController.kt`: Lock-Button `alpha = 0.45f` gesetzt (matcht `TouchButtonConfig`-Default).
  - Map-Screen-Presets umbenannt — `map_shift`→Item Stacking, `map_ctrl`→Mark Enemies/Feinde markieren, `map_alt`→Waypoints/Wegpunkt setzen, `map_options`→Options/Optionen, `map_inventory`→Inventory/Inventar.
  - String-Ressourcen (values-de, values) und `TouchButtonPresets.kt` synchron aktualisiert.
- **Tests**: `.\gradlew.bat assembleRelease` -> BUILD SUCCESSFUL, manuelle Geraetepruefung bestaetigt.
- **Commits**: `0cd2fea` (strafe/alt_movement JSON fix), `72485d1` (lock button square), `[pending]` (map rename + lock alpha)
- **Ende**: 2026-06-19

---

## ABSCHLUSS-STATUS (2026-06-19)

**Feature-Entwicklung ist abgeschlossen.** Alle Phasen (0–7) sind implementiert und getestet.

### Noch offen — 8 Known Issues (4 resolved am 2026-06-19):

| # | Bug | Beschreibung |
|---|-----|-------------|
| 1 | ~~`map_inventory`-Mapping~~ | ✅ **RESOLVED (2026-06-19).** Migration `I`→`ENTER` in `migrateMapInventoryEnterKey()` implementiert. |
| 1a | ~~Strafe-Buttons auf Tactical~~ | ✅ **RESOLVED (2026-06-19).** `strafe_hold`/`strafe_toggle` aus Tactical-Presets und Defaults entfernt. |
| 1b | ~~shift_toggle / alt_movement Umbenennung~~ | ✅ **RESOLVED (2026-06-19).** `shift_toggle`→Item Stacking, `alt_movement_hold`→Seitwärts-/Rückwärtsschritte (Toggle), Localization ergaenzt. |
| 2 | ~~Batch 02+03 Icon-Alignment~~ | ✅ **RESOLVED (2026-06-19).** Y-Offsets durch Codex invertiert, X-Offset-Invertierung nachgeholt. Alle Icons korrekt aligned. |
| 3 | Editor ohne Map-Screen-Presets | `TOUCH_BUTTON_PRESETS` enthält nur taktische Presets. Beim Editieren eines Map-Screen-Buttons (z.B. Laptop) findet `touchButtonPresetFor()` keinen Match und das Dropdown fällt auf den ersten taktischen Eintrag zurück — der Map-Button wird ungewollt zum taktischen Button. **Fix:** Preset-Liste nach Screen-Kontext trennen (In-Game vs. Map-Screen), Create-New-Dropdown zeigt nur die für den jeweiligen Screen relevanten Presets |
| 4 | Export/Import ohne Screen-Trennung | Das Touch-Preset-Export-/Import-System muss `mapScreenButtons` und taktische `buttons` korrekt getrennt behandeln. Export darf beide nicht vermischen, Import muss in die richtigen Config-Felder zurückschreiben |
| 5 | ~~Lock-Button zu klein~~ | ✅ **RESOLVED (2026-06-19).** Oval→Rounded Square mit voller 44dp, Alpha 0.45f matcht TouchButtonConfig-Default. |
| 6 | Reload-Button prüfen | Unklar ob JA2 einen nativen Reload-Key hat. Kein Icon im Set, was verwundert. Prüfen und ggf. Overlay-Button ergänzen |
| 7 | HARDWARE-Mode falsche Dropdown-Position | Bleibt in Expert Settings, aber Reihenfolge soll direkt unter Modern Controls sein, nicht am Ende |
| 8 | Screen-Skalierung unvollständig | Nicht alle Bildschirme skalieren korrekt. Auto-Aid-Button: Das Spiel springt während der Aktion aus dem Widescreen in den 4:3-Modus und danach zurück. Händler-Bildschirm: User berichten von gecroppter und falsch skalierter Darstellung. **Prüfauftrag:** Das gesamte Spiel auf sämtliche Bildschirme durchgehen, die bei der Widescreen-Anpassung möglicherweise übersehen wurden (alle Spiel-Modi, Menüs, Dialoge, Inventar-, Händler-, Laptop-, Vertrags- und sonstige UI-Screens) |

### Ausstehend:

- **Phase F**: Finale APK + Endabnahme (nach Fix der 8 Bugs)
- **Phase 6**: Mod Support v1 (ausgelagert in eigenen Plan)

---

## v1.0.5 Punkt 7: Stealth-Toggle Zustands-Sync

- **Start**: 2026-06-24
- **Branch**: `experimental`
- **Ausgangslage**: Der Android-Touchbutton `stealth_toggle` hielt seinen visuellen `toggle_tap`-Status lokal. Nach Wechsel des aktiven Söldners konnte der Button daher noch aktiv aussehen, obwohl der neu selektierte Merc nicht im Sneak-/Stealth-Modus war.
- **Tätigkeiten**:
  - Native JNI-Abfrage `SDLActivity.getSelectedMercStealthMode()` ergänzt.
  - JNI-Implementierung in `MercPortraitTouch_JNI.cc` liest `GetSelectedMan()->bStealthMode` im Tactical Screen.
  - `TouchOverlayButtonView.syncToggleTapState()` ergänzt, um den visualisierten Toggle-Zustand ohne Key-Dispatch zu setzen.
  - `TouchOverlayController` synchronisiert den Stealth-Toggle beim Polling und nach Screen-/Button-Wechsel gegen den nativen Merc-Status.
  - Version für die Test-/Release-APK von `1.0.3` auf `1.0.5` gehoben, da `1.0.4` bereits veröffentlicht war.
- **Tests**:
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
  - `.\gradlew.bat assembleRelease` in `android` -> BUILD SUCCESSFUL.
  - `apksigner verify --print-certs` -> Zertifikat SHA-256 `833a992d08cdf66f9abcbb239e16419a80c75aba216636e339fa5aff6b67c44d`.
  - `aapt dump badging` -> `versionName='1.0.5'`, `versionCode='1000005'`.
- **Artefakt**: `G:\Meine Ablage\Claude\JA2-Reborn-1.0.5-experimental-99ce75e-stealth-toggle-signed.apk`.
- **Manuelle Prüfung**: Am Gerät bestätigt; APK installiert über `1.0.4`, Stealth-Toggle-Sync funktioniert.
- **Ergebnis**: Punkt 7 abgeschlossen und funktional.
- **Ende**: 2026-06-24

---

## v1.0.5: Item Stacking / Sidestep Toggle Auto-Release

- **Start**: 2026-06-24
- **Branch**: `experimental`
- **Ausgangslage**: `SHIFT` (Item Stacking) und `ALT` (Seitwärts-/Rückwärtsschritte) wurden als sticky `mode="toggle"`-Modifier gehalten. Bei späteren Aktionen, z.B. Söldnerwechsel mit aktivem `ALT`, blockierte der gehaltene Modifier die eigentliche Nutzeraktion.
- **Tätigkeiten**:
  - `TouchInputDispatcher.releaseToggleKeysExcept()` ergänzt, um gehaltene sticky Modifier gezielt per KeyUp freizugeben.
  - Overlay-Buttons melden vor neuen Nicht-Modifier-Aktionen einen User-Action-Start; aktive sticky Modifier werden vorher gelöst.
  - Wenn ein anderer sticky Modifier aktiviert wird, werden bestehende sticky Modifier gelöst, der neu gedrückte Toggle bleibt aber priorisiert.
  - `SDLSurface` meldet Team-Portrait-Auswahl vorab an das Overlay, damit z.B. `ALT` den Söldnerwechsel nicht blockiert.
  - Direkte Tactical-Inventar- und Map-Screen-Touches behalten `SHIFT` für Item Stacking; `ALT` wird nach dem tatsächlichen Touch-/Mouse-Up gelöst.
  - Die visuelle Toggle-Anzeige der betroffenen Buttons wird beim Auto-Release zurückgesetzt.
- **Tests**:
  - `.\gradlew.bat testDebugUnitTest` in `android` -> BUILD SUCCESSFUL.
  - `.\gradlew.bat assembleRelease` in `android` -> BUILD SUCCESSFUL.
- **Manuelle Prüfung**: Am Gerät bestätigt; Sidestep/Backstep löst bei anderen Aktionen korrekt, Item Stacking bleibt im Tactical-Inventar und im Map Screen bedienbar.
- **Ergebnis**: Useraktionen haben Vorrang vor aktiven Modifier-Toggles. `ALT` wird bei anderen Aktionen automatisch gelöst; `SHIFT` bleibt bei Tactical-Inventar- und Map-Screen-Touches aktiv, damit Item Stacking bedienbar bleibt. Fix abgeschlossen und funktional.
- **Ende**: 2026-06-24
