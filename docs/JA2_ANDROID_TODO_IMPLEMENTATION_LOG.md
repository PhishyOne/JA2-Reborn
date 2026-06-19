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

---

## ABSCHLUSS-STATUS (2026-06-18)

**Feature-Entwicklung ist abgeschlossen.** Alle Phasen (0–7) sind implementiert und getestet.

### Noch offen — 8 Known Issues:

| # | Bug | Beschreibung |
|---|-----|-------------|
| 1 | `map_inventory`-Mapping | Button mapped auf `I` (Item-Highlight) statt Inventar-Panel zu öffnen. Korrekter Key: `Enter`. Auf Enter ummappen |
| 2 | Batch 02+03 Icon-Alignment | Stance- und Combat-Icons sitzen nicht korrekt. Icons stammen aus `D:/Coding/Game-Icon-Converter/`, Vorgaben: `D:/Coding/OverlayIconManual.md`. `iconFill`/`iconOffset` per iconset.json-Export tunen |
| 3 | Editor ohne Map-Screen-Presets | `TOUCH_BUTTON_PRESETS` enthält nur taktische Presets. Beim Editieren eines Map-Screen-Buttons (z.B. Laptop) findet `touchButtonPresetFor()` keinen Match und das Dropdown fällt auf den ersten taktischen Eintrag zurück — der Map-Button wird ungewollt zum taktischen Button. **Fix:** Preset-Liste nach Screen-Kontext trennen (In-Game vs. Map-Screen), Create-New-Dropdown zeigt nur die für den jeweiligen Screen relevanten Presets |
| 4 | Export/Import ohne Screen-Trennung | Das Touch-Preset-Export-/Import-System muss `mapScreenButtons` und taktische `buttons` korrekt getrennt behandeln. Export darf beide nicht vermischen, Import muss in die richtigen Config-Felder zurückschreiben |
| 5 | Lock-Button zu klein | Seit SVG-Umstellung ist der Lock-Button winzig im Vergleich zu den Canvas-Icons. Korrekte Größe wiederherstellen |
| 6 | Reload-Button prüfen | Unklar ob JA2 einen nativen Reload-Key hat. Kein Icon im Set, was verwundert. Prüfen und ggf. Overlay-Button ergänzen |
| 7 | HARDWARE-Mode falsche Dropdown-Position | Bleibt in Expert Settings, aber Reihenfolge soll direkt unter Modern Controls sein, nicht am Ende |
| 8 | Screen-Skalierung unvollständig | Nicht alle Bildschirme skalieren korrekt. Auto-Aid-Button: Das Spiel springt während der Aktion aus dem Widescreen in den 4:3-Modus und danach zurück. Händler-Bildschirm: User berichten von gecroppter und falsch skalierter Darstellung. **Prüfauftrag:** Das gesamte Spiel auf sämtliche Bildschirme durchgehen, die bei der Widescreen-Anpassung möglicherweise übersehen wurden (alle Spiel-Modi, Menüs, Dialoge, Inventar-, Händler-, Laptop-, Vertrags- und sonstige UI-Screens) |

### Ausstehend:

- **Phase F**: Finale APK + Endabnahme (nach Fix der 8 Bugs)
- **Phase 6**: Mod Support v1 (ausgelagert in eigenen Plan)
