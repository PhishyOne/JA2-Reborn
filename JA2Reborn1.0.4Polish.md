# JA2 Reborn 1.0.4 Polish Plan

Stand: 2026-06-19  
Zielbranch: `experimental`  
Quelle fuer neues Touch-Default-Preset: `G:\Meine Ablage\Claude\default_preset.json`

## Verbindliche Arbeitsregeln

- Es wird ausschliesslich auf dem Branch `experimental` gearbeitet.
- Vor Abschluss jeder Phase werden die Ergebnisse ein zweites Mal sorgfaeltig geprueft: Diff lesen, relevante Suchlaeufe ausfuehren, Tests/Builds pruefen und Abgleich mit diesem Plan machen.
- Bei Abschluss jeder Phase wird der Projektlog aktualisiert und zusammen mit den Code-/Asset-Aenderungen committed.
- Nach Abschluss jeder Phase wird gestoppt, damit bei Bedarf der Kontext geleert werden kann. Die naechste Phase beginnt erst nach erneuter Freigabe.

## Zielbild

Dieses Polish-Update schliesst die offenen Punkte aus `Notes.md`:

- Das neue gebuendelte Touch-Default-Preset ersetzt das bisherige Preset.
- Bestehende User-Layouts werden beim ersten Start nach dem Update fuer alle Nutzer einmalig und ohne Ausnahme durch das neue Default-Preset ersetzt.
- Der Preset-Zwang laeuft ueber eine eigene Default-Preset-Version und bleibt unabhaengig von `TOUCH_OVERLAY_CONFIG_VERSION`.
- Nach dem Reset erscheint eine einmalige Hinweistafel im Stil des bestehenden Tutorial-Systems.
- Das Touch-Overlay bleibt im Shopkeeper-/Haendler-Screen sichtbar und nutzt dort das taktische Buttonset.

## Phase 0 - Baseline und Eingangspruefung

- `git status --short --branch` pruefen und bestaetigen, dass der aktive Branch `experimental` ist.
- Vorhandene, nicht zu dieser Arbeit gehoerende Aenderungen nur dokumentieren und nicht zuruecksetzen.
- Neues Preset aus `G:\Meine Ablage\Claude\default_preset.json` gegen `android/app/src/main/res/raw/default_touch_preset.json` vergleichen:
  - taktische Button-Anzahl: 22
  - Map-Button-Anzahl: 7
  - neue relevante Buttons: Reload Selected, Item Stacking, Level Toggle, Swap Places
  - Runtime-Werte: `relative_mouse_speed = 1.45`, `scroll_speed_ms = 35`, `tactical_action_panel_scale_percent = 130`, `direct_touch_arbitration_ms = 2500`
- Icon- und Key-Abdeckung pruefen:
  - alle Icons muessen in `iconmappings.json` gemappt sein oder bewusst als Spezialfall gerendert werden,
  - `dpad_map` bleibt Spezialfall in `TouchOverlayButtonView`,
  - alle Key-Namen muessen in `TouchInputDispatcher.keyNameToCode()` vorhanden sein.
- Abschluss: keine Code-Aenderung noetig; falls nur Baseline dokumentiert wird, Log-Update committen und stoppen.

## Phase 1 - Neues Default-Preset und Code-Fallback synchronisieren

- `android/app/src/main/res/raw/default_touch_preset.json` durch das neue Preset ersetzen.
- Das Preset um stabile Metadaten ergaenzen:
  - `schema_version: 15`
  - `default_preset_version: 20260619`
- Die Werte aus dem Preset in den Code-Fallback uebernehmen:
  - `TouchOverlayConfig`-Defaults fuer Mouse-Speed, Scroll-Speed, Panel-Scale und Direct-Tap-Arbitration synchronisieren.
  - `defaultButtons()` auf die 22 taktischen Buttons des neuen Presets bringen.
  - `defaultMapScreenButtons()` auf die 7 Map-Buttons des neuen Presets bringen.
  - IDs aus dem Preset duerfen bleiben; die Preset-Erkennung basiert auf Icon/Action, nicht auf sprechenden IDs.
- `normalizeTouchOverlayConfig()` weiterhin fuer alte Sonderfaelle beibehalten:
  - altes `map_inventory` mit `I` wird zu `ENTER`,
  - alte `map_ctrl`/`map_alt` werden entfernt,
  - alter `range_cursor` wird entfernt,
  - altes `stealth_toggle` mit `tap` wird zu `toggle_tap`.
- Tests aktualisieren:
  - Default-Button-Zaehler anpassen,
  - neue Default-Werte pruefen,
  - Reload-, Level-, Swap- und Shift-Buttons im Default pruefen,
  - Map-Defaults auf `ENTER` fuer Inventory pruefen.
- Zweitpruefung:
  - Raw-JSON und Code-Fallback inhaltlich gegen das Google-Drive-Preset vergleichen.
  - `rg` auf veraltete Default-Positionen/Counts und alte `direct_touch_arbitration_ms = 1800` im Default-Kontext ausfuehren.
  - `.\gradlew.bat testDebugUnitTest` im `android`-Ordner ausfuehren.
- Abschluss: Log aktualisieren, committen, stoppen.

## Phase 2 - Default-Preset-Version und erzwungener einmaliger Reset

- In `TouchButtonModels.kt` ergaenzen:
  - `const val DEFAULT_TOUCH_PRESET_VERSION = 20260619`
  - `@SerialName("default_preset_version") val defaultPresetVersion: Int = 0` in `TouchOverlayConfig`
- `normalizeTouchOverlayConfig()` stempelt normalisierte Konfigurationen mit:
  - `schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION`
  - `defaultPresetVersion = DEFAULT_TOUCH_PRESET_VERSION`
- Die Store-Logik in `TouchButtonStore` erweitern:
  - neues Ergebnisobjekt `TouchOverlayLoadResult(config: TouchOverlayConfig, defaultPresetWasReset: Boolean)`,
  - bestehende `loadOrDefault()`-Nutzung auf ein neues `loadOrDefaultWithResult()` umstellen oder `loadOrDefault()` intern als Wrapper erhalten,
  - bei nicht vorhandener User-Datei: gebuendeltes Default laden, speichern, `defaultPresetWasReset = false`,
  - bei vorhandener User-Datei mit `defaultPresetVersion != DEFAULT_TOUCH_PRESET_VERSION`: gebuendeltes Default laden, normalisieren, adaptive Defaults anwenden, speichern, `defaultPresetWasReset = true`,
  - bei aktueller Version: bestehende Schema-/Legacy-Normalisierung wie bisher ausfuehren,
  - bei kaputter User-Datei: wie bisher Backup erstellen und Default laden; kein Update-Hinweis erzwingen.
- Import-/Export-Verhalten:
  - Import normalisiert und stempelt auf die aktuelle Default-Preset-Version, damit ein importiertes Layout nicht sofort wieder vom Update-Reset ersetzt wird.
  - Export schreibt `schema_version` und `default_preset_version` mit.
  - Manuelles "Restore Defaults" nutzt weiterhin das aktuelle gebuendelte Default.
- Tests:
  - fehlendes `default_preset_version` wird als `0` gelesen,
  - alte Version erzwingt Reset,
  - aktuelle Version erzwingt keinen Reset,
  - Import alter Presets wird auf aktuelle Version gestempelt,
  - bestehende Schema-Migrationen bleiben erhalten.
- Zweitpruefung:
  - Store-Diff Zeile fuer Zeile pruefen, damit User-Layouts nur bei Preset-Version-Abweichung ersetzt werden.
  - `rg "default_preset_version|DEFAULT_TOUCH_PRESET_VERSION|loadOrDefault"` ausfuehren.
  - `.\gradlew.bat testDebugUnitTest` im `android`-Ordner ausfuehren.
- Abschluss: Log aktualisieren, committen, stoppen.

## Phase 3 - Einmalige Update-Hinweistafel

- Bestehendes natives `TutorialSystem` wiederverwenden, kein separater Android-Toast.
- `TutorialMode` um `TouchPresetUpdate` erweitern.
- Neue lokalisierte One-Panel-Inhalte hinzufuegen:
  - DE: Layout wurde zurueckgesetzt, Grund sind fundamentale Touch-Aenderungen, weitere grosse Reset-Aenderungen sind nicht erwartet.
  - EN: gleiche Aussage fuer englische UI.
- Persistenz separat vom Tactical- und Main-Menu-Tutorial halten:
  - eigene Datei im Stracciatella-Home, z. B. `touch_preset_update_notice.set`,
  - gespeicherte Version: zuletzt bestaetigte Default-Preset-Version,
  - beim Bestaetigen wird die aktuelle Version als gesehen gespeichert,
  - die Checkbox bleibt fuer Stilgleichheit sichtbar; sie darf denselben Persistenzpfad nutzen, darf aber nicht verhindern, dass die Meldung fuer diese Version nach Bestaetigung verschwindet.
- JNI ergaenzen:
  - `SDLActivity.requestTouchPresetUpdateNotice(int version)` in Java,
  - JNI-Funktion setzt native Pending-Version.
- Android-Verknuepfung:
  - `TouchOverlayController.attach()` wertet `TouchOverlayLoadResult.defaultPresetWasReset` aus.
  - Wenn `true`, wird `requestTouchPresetUpdateNotice(DEFAULT_TOUCH_PRESET_VERSION)` nach dem Laden des Overlays aufgerufen.
- Native Anzeige:
  - Tutorial-System zeigt den Hinweis, sobald ein renderbarer Spiel-/Menuekontext verfuegbar ist und keine andere Tutorial-Tafel sichtbar ist.
  - Eingabe wird wie beim bestehenden Tutorial blockiert, bis der Nutzer bestaetigt.
- Tests/Build:
  - Native Build wegen JNI und C++-Aenderungen.
  - Falls sinnvoll, pure Helper fuer Version-Persistenz testbar halten.
- Zweitpruefung:
  - `rg "TouchPresetUpdate|requestTouchPresetUpdateNotice|touch_preset_update_notice"` ausfuehren.
  - JNI-Signaturen mit `SDLActivity.java` und `Tutorial_JNI.cc` abgleichen.
  - `.\gradlew.bat testDebugUnitTest` und `.\gradlew.bat externalNativeBuildDebug` im `android`-Ordner ausfuehren.
- Abschluss: Log aktualisieren, committen, stoppen.

## Phase 4 - Touch-Overlay im Shopkeeper-Screen sichtbar halten

- In `TouchOverlayController` die Screen-Konstanten ergaenzen:
  - `GAME_SCREEN = 5`
  - `MAP_SCREEN = 9`
  - `SHOPKEEPER_SCREEN = 18`
- `VISIBLE_SCREEN_WHITELIST` auf `setOf(GAME_SCREEN, MAP_SCREEN, SHOPKEEPER_SCREEN)` erweitern.
- Buttonset-Auswahl bewusst nicht fuer Shopkeeper erweitern:
  - nur `currentActiveScreen == MAP_SCREEN` nutzt `mapScreenButtons`,
  - Shopkeeper nutzt automatisch taktische `buttons`.
- Testbarkeit verbessern:
  - Whitelist-/Buttonset-Entscheidung in kleine interne Helper auslagern, damit JVM-Tests ohne Android-UI moeglich sind.
- Tests:
  - Shopkeeper-Screen ist sichtbar.
  - Shopkeeper-Screen verwendet nicht das Map-Buttonset.
  - Map-Screen verwendet weiterhin `mapScreenButtons`.
  - Nicht freigegebene Screens bleiben bei Auto-Hide ausgeblendet.
- Zweitpruefung:
  - `src/game/ScreenIDs.h` erneut gegen Android-Konstanten vergleichen.
  - `rg "VISIBLE_SCREEN_WHITELIST|SHOPKEEPER_SCREEN|activeButtons"` ausfuehren.
  - `.\gradlew.bat testDebugUnitTest` im `android`-Ordner ausfuehren.
- Abschluss: Log aktualisieren, committen, stoppen.

## Phase 5 - Endverifikation ohne Release-Abgabe

- Vollstaendige technische Verifikation:
  - `.\gradlew.bat testDebugUnitTest`
  - `.\gradlew.bat externalNativeBuildDebug`
  - `.\gradlew.bat assembleDebug`
- Manuelle Testmatrix:
  - Neuinstallation ohne `touch_buttons.json`: Default wird geladen, kein Update-Reset-Hinweis.
  - Update mit alter `touch_buttons.json`: User-Layout wird genau einmal ersetzt, Hinweis erscheint.
  - Zweiter Start nach Update: kein erneuter Reset, kein erneuter Hinweis.
  - Import eines alten Presets: Import bleibt erhalten und wird nicht direkt wieder ueberschrieben.
  - Shopkeeper-Screen: taktische Buttons sichtbar; Shift/Item-Stacking und rechte Maustaste funktionieren.
  - Tactical und Map-Screen: richtiges Buttonset, keine Vermischung.
  - Referenzdisplay 2400x1080 und mindestens ein abweichendes Seitenverhaeltnis pruefen.
- Zweitpruefung:
  - finalen Diff lesen,
  - Logs/Planstatus auf Konsistenz pruefen,
  - Build-Artefakte nicht committen,
  - keine Release-/Abgabe-APK erstellen, sofern nicht explizit freigegeben.
- Abschluss: Projektlog aktualisieren, finalen Polish-Commit erstellen, stoppen.

## Akzeptanzkriterien

- Alle Nutzer mit vorhandener alter Touch-Konfiguration erhalten beim ersten Start nach Update das neue Default-Preset.
- Der Reset passiert nur einmal pro `DEFAULT_TOUCH_PRESET_VERSION`.
- Das neue Preset bleibt auf unterschiedlichen Displaygroessen erreichbar und wird weiterhin durch adaptive Defaults skaliert.
- Die Update-Hinweistafel ist lokalisiert, blockiert Eingabe sauber und wird nach Bestaetigung nicht erneut fuer dieselbe Preset-Version gezeigt.
- Shopkeeper zeigt das taktische Overlay und nicht das Map-Screen-Overlay.
- Unit-Tests, Native Build und Debug-APK-Build sind erfolgreich.

## Phasenlog

### Phase 0 - Baseline und Eingangspruefung

Stand: 2026-06-19

- Branch geprueft: aktiv ist `experimental`.
- Bestehende uncommitted Aenderungen dokumentiert und nicht zurueckgesetzt:
  `CHANGELOG.md`, `README.md`, geloeschte alte Plan-Dateien im Repo-Root,
  neue Ordner/Dateien `IconWork/`, `JA2 Reborn Zusatz/`, `Notes.md` und dieser Polish-Plan.
- Drive-Preset `G:\Meine Ablage\Claude\default_preset.json` geprueft:
  22 taktische Buttons, 7 Map-Buttons.
- Neue relevante Buttons im Drive-Preset bestaetigt:
  Reload Selected (`reload_selected`), Item Stacking (`map_shift`), Level Toggle (`level_toggle`), Swap Places (`swap_places`).
- Runtime-Werte im Drive-Preset bestaetigt:
  `relative_mouse_speed = 1.45`, `scroll_speed_ms = 35`,
  `tactical_action_panel_scale_percent = 130`,
  `direct_touch_arbitration_ms = 2500`.
- Aktuelles gebuendeltes Android-Preset ist noch nicht synchron:
  18 taktische Buttons, 7 Map-Buttons, ohne neue Metadaten.
- Code-Fallback ist noch nicht synchron:
  `TouchOverlayConfig` nutzt weiterhin `relativeMouseSpeed = 1.0f`,
  `scrollSpeedMs = 27`, `tacticalActionPanelScalePercent = 100`,
  `directTouchArbitrationMs = 1800`; `defaultButtons()` enthaelt nur 4 taktische Buttons.
- Icon-Abdeckung geprueft:
  alle Drive-Preset-Icons sind in `iconmappings.json` gemappt oder bewusst Spezialfall;
  `dpad_map` bleibt Spezialfall in `TouchOverlayButtonView`.
- Key-Abdeckung geprueft:
  alle Key-Namen aus dem Drive-Preset sind in `TouchInputDispatcher.keyNameToCode()` vorhanden.
- Ergebnis: keine Code-Aenderung in Phase 0 noetig; Phase 1 kann mit Preset-/Fallback-Synchronisierung beginnen.

### Phase 1 - Neues Default-Preset und Code-Fallback synchronisieren

Stand: 2026-06-19

- `android/app/src/main/res/raw/default_touch_preset.json` durch das Drive-Preset ersetzt und um
  `schema_version = 15` sowie `default_preset_version = 20260619` ergaenzt.
- Code-Fallback in `TouchButtonModels.kt` synchronisiert:
  22 taktische Buttons, 7 Map-Buttons, neue Runtime-Defaults
  `relativeMouseSpeed = 1.45f`, `scrollSpeedMs = 35`,
  `tacticalActionPanelScalePercent = 130`, `directTouchArbitrationMs = 2500`.
- Weitere Runtime-Fallbacks in `TouchOverlayController` und `TouchOverlaySettingsDialog`
  auf die neuen Defaults gebracht.
- Legacy-Normalisierung beibehalten:
  `map_inventory` mit `I` wird zu `ENTER`, `map_ctrl`/`map_alt` werden entfernt,
  `range_cursor` wird entfernt, `stealth_toggle` mit `tap` wird zu `toggle_tap`.
- Map-Tactical-Ergaenzung in der alten Schema-Migration sucht jetzt auch nach
  `icon == "map_tactical"`, weil das neue Preset eine generierte ID fuer diesen Button nutzt.
- Tests aktualisiert:
  Default-Zaehler, Runtime-Defaults, Reload/Shift/Level/Swap im taktischen Default,
  Map-Inventory `ENTER` und Map-Tactical per Icon.
- Zweitpruefung:
  Raw-Preset gegen `G:\Meine Ablage\Claude\default_preset.json` verglichen;
  Counts, Runtime-Werte und Button-Signaturen stimmen.
- Suchlaeufe ausgefuehrt:
  keine alten Default-Fallbacks fuer `directTouchArbitrationMs = 1800`,
  `scrollSpeedMs = 27`, `tacticalActionPanelScalePercent = 100` oder alte
  Default-Button-Count-Annahmen gefunden; verbleibendes `1800` ist nur ein Slider-Wert.
- Verifikation:
  `.\gradlew.bat testDebugUnitTest` im `android`-Ordner erfolgreich.
