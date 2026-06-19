# JA2 Update 104 Plan

## Arbeitsregeln

1. Gearbeitet wird weiterhin ausschliesslich auf dem Branch `experimental`.
2. Nach Abschluss jeder Phase werden Log und Plan aktualisiert und committed.
3. Nach Abschluss jeder Phase werden alle Ergebnisse ein zweites Mal sorgfaeltig geprueft.
4. Nach Beendigung jeder Phase wird gestoppt, damit der Kontext bei Bedarf geleert werden kann.
5. Eine APK darf zum Verifizieren erstellt werden; ein Google-Drive-Upload ist erst zur Endabnahme notwendig.

## Summary

Die offenen Punkte betreffen drei Bereiche: Touch-Overlay-Konfiguration, Icon-/Button-Darstellung und native Screen-Skalierung. Die Touch-Overlay-Aenderungen werden mit Schema-Version `13` migriert, damit bestehende User-Layouts erhalten bleiben. Die nativen Skalierungsfixes werden zuerst fuer Auto-Bandage und Shopkeeper umgesetzt und danach ueber eine feste Screen-Audit-Matrix geprueft.

## Phase 1: Touch Overlay Migration und Map Inventory

**Status:** Abgeschlossen am 2026-06-19. Implementiert, zweitgeprueft und committed; manuelle Geraetepruefung steht fuer die spaetere Endverifikation aus.

### Ziel

Der Map-Screen-Inventarbutton muss das Inventar-Panel korrekt ueber `ENTER` oeffnen. Bestehende Layouts duerfen dabei nicht beschaedigt werden.

### Umsetzung

- `map_inventory` in `defaultMapScreenButtons()` von `I` auf `ENTER` umstellen.
- `map_inventory` im gebuendelten `android/app/src/main/res/raw/default_touch_preset.json` ebenfalls auf `ENTER` umstellen.
- `TOUCH_OVERLAY_CONFIG_VERSION` von `12` auf `13` erhoehen.
- Eine zentrale Normalisierung/Migration einfuehren:
  - alte `map_inventory`-Buttons mit `I` werden auf `ENTER` migriert;
  - leere oder fehlende `mapScreenButtons` werden mit Map-Defaults gefuellt;
  - vorhandene taktische `buttons` bleiben unveraendert.
- Die Normalisierung beim Laden und Importieren von Touch-Presets anwenden.

### Pruefung

- Unit-Test fuer Migration Schema 12 -> 13.
- Unit-Test: `map_inventory` mit `I` wird zu `ENTER`.
- Unit-Test: taktische Buttons bleiben unveraendert.
- Manuell pruefen: Map-Inventarbutton oeffnet das Inventar-Panel.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

### Ergebnis 2026-06-19

- `map_inventory` nutzt in Code-Defaults und gebuendeltem Preset `ENTER`.
- Schema-Version ist `13`.
- `normalizeTouchOverlayConfig()` migriert alte `I`-Map-Inventory-Buttons, fuellt fehlende/leere Map-Buttons und laesst taktische Buttons unveraendert.
- Laden und Importieren von Touch-Presets verwenden die zentrale Normalisierung.
- `android\gradlew.bat testDebugUnitTest` aus dem Android-Gradle-Root: BUILD SUCCESSFUL.

## Phase 2: Screen-Kontextgetrennte Presets und Export/Import

**Status:** Abgeschlossen am 2026-06-19. Implementiert, zweitgeprueft und committed; manuelle Editorpruefung steht fuer die spaetere Endverifikation aus.

### Ziel

Map-Screen-Buttons duerfen im Editor nicht mehr auf taktische Presets zurueckfallen. Export und Import muessen taktische Buttons und Map-Screen-Buttons dauerhaft getrennt halten.

### Umsetzung

- Presets screen-kontextgetrennt aufteilen:
  - `TACTICAL_TOUCH_BUTTON_PRESETS`
  - `MAP_SCREEN_TOUCH_BUTTON_PRESETS`
- Einen Screen-Kontext-Typ oder eine aequivalente API einfuehren: Tactical vs. Map Screen.
- `TouchOverlayController` bestimmt den aktiven Kontext aus dem aktuellen Screen.
- `onPlusTapped()` verwendet den ersten Preset des aktiven Screen-Kontexts.
- `TouchOverlayEditDialog` erhaelt die passende Preset-Liste vom Controller.
- `touchButtonPresetFor()` sucht nur innerhalb der uebergebenen kontextbezogenen Presets.
- Map-Presets fuer die bestehenden Map-Buttons ergaenzen, inklusive `map_inventory` mit `ENTER`.
- Localization fuer Map-Preset-Labels und Kategorien ergaenzen.
- Export bleibt Full-Layout-basiert:
  - JSON enthaelt `buttons`;
  - JSON enthaelt `map_screen_buttons`;
  - Export exportiert niemals nur den aktuell sichtbaren Screen.
- Import normalisiert Schema und Legacy-Werte, mischt aber keine Map-Buttons in taktische Buttons.

### Pruefung

- Unit-Test: Map-Editor bietet keine taktischen Presets an.
- Unit-Test: taktischer Editor bietet keine Map-only-Presets an.
- Unit-Test: Export/Import-Roundtrip erhaelt `buttons` und `map_screen_buttons` getrennt.
- Unit-Test: Import alter Presets ergaenzt fehlende Map-Buttons, ohne taktische Buttons zu veraendern.
- Manuell pruefen: Map-Button bearbeiten veraendert keinen taktischen Button.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

### Ergebnis 2026-06-19

- Presets sind in `TACTICAL_TOUCH_BUTTON_PRESETS` und `MAP_SCREEN_TOUCH_BUTTON_PRESETS` getrennt.
- `TouchOverlayController` waehlt die Presetliste anhand des aktiven Screens.
- `TouchOverlayEditDialog` sucht Presets nur innerhalb der uebergebenen Kontextliste.
- Map-Screen-Presets fuer alle acht Map-Buttons inklusive `map_inventory` mit `ENTER` sind vorhanden.
- Export/Import bleibt Full-Layout-basiert; Tests bestaetigen getrennten Erhalt von `buttons` und `map_screen_buttons`.
- `android\gradlew.bat testDebugUnitTest` aus dem Android-Gradle-Root: BUILD SUCCESSFUL.

## Phase 3: Icon Alignment, Lock Button und Reload Preset

**Status:** Abgeschlossen am 2026-06-19. Implementiert, zweitgeprueft und committed; visuelle Geraetepruefung steht fuer die spaetere Endverifikation aus.

### Ziel

Batch-02/03-Icons muessen korrekt ausgerichtet sein, der Lock-Button muss wieder gut sichtbar sein, und Reload soll als Overlay-Preset verfuegbar sein.

### Umsetzung

- Batch 02 und Batch 03 aus `IconWork` in `android/app/src/main/res/raw/iconset.json` uebernehmen.
- Beim Uebernehmen von Converter-Werten `iconOffsetY` invertieren, weil der Converter positive Y-Werte visuell nach oben interpretiert, die JA2-Runtime aber nach Manual-Formel positive Y-Werte nach unten anwendet.
- `SvgIconManager.IconSetEntry` um folgende Felder erweitern:
  - `iconRotation`
  - `iconFlipH`
  - `iconFlipV`
- `SvgIconManager.renderIcon()` wendet Fill, Scale, Offset, Flip und Rotation kompatibel zur Converter-Preview an.
- Verbose Debug-Logs fuer Icon/Button-Rendering entfernen oder hinter einen Debug-Flag legen.
- Lock-Systembutton mit expliziter Icon-Vergroesserung rendern:
  - Zielwert `1.55f`;
  - Hitbox und Button-Groesse bleiben unveraendert.
- Reload-Preset ergaenzen:
  - Action-ID: `reload_selected`
  - Tastenkombi: `ALT + R`
  - neues Icon: `icon_reload.svg`
  - Mapping-ID: `2000000038`
  - Kategorie: Combat
- Native Reload-Logik nicht aendern, da `ALT+R` bereits `HandleTBReload()` / `AutoReload(selectedMerc)` ausloest.
- Reload nicht automatisch in bestehende oder gebuendelte sichtbare Default-Layouts einfuegen; der Button ist ueber den Editor verfuegbar.

### Pruefung

- Unit-Test oder JSON-Decoding-Test: neue Icon-Felder werden korrekt gelesen.
- Unit-Test: taktischer Editor enthaelt Reload-Preset mit `ALT+R`.
- Manuell pruefen: Batch-02/03-Icons sind zentriert und nicht abgeschnitten.
- Manuell pruefen: Lock-Icon ist sichtbar vergleichbar gross wie andere Systembuttons.
- Manuell pruefen: Reload-Preset loest im Tactical Screen `ALT+R` aus.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

### Ergebnis 2026-06-19

- Batch 02 und Batch 03 wurden aus `IconWork` uebernommen.
- `iconset.json` enthaelt die Converter-Werte fuer Fill, Scale, Offset, Rotation und Flip; `iconOffsetY` wurde fuer die Runtime invertiert.
- `SvgIconManager.renderIcon()` wendet Fill, Scale, Offset, Flip und Rotation an und loggt Renderdetails nur noch hinter einem Debug-Flag.
- Lock-Systembutton nutzt `1.55f` als Icon-Fill-Override.
- Reload-Preset `reload_selected` ist mit `ALT + R`, `icon_reload.svg` und Mapping-ID `2000000038` verfuegbar, aber nicht in sichtbare Default-Layouts eingefuegt.
- Native Pruefung: `ALT+R` ruft bereits `HandleTBReload()` und `AutoReload(selectedMerc)` auf.
- `android\gradlew.bat testDebugUnitTest` aus dem Android-Gradle-Root: BUILD SUCCESSFUL.

## Phase 4: Mouse Mode Order

### Ziel

`HARDWARE` bleibt in den Expert Settings, steht im Dropdown aber direkt unter Modern Controls.

### Umsetzung

- Eine zentrale Mouse-Mode-Reihenfolge definieren:
  - `TOUCHPAD`
  - `HARDWARE`
  - `ABSOLUTE`
  - `TOUCHSCREEN`
- `SettingsFragment` und `DataTabFragment` auf diese gemeinsame Reihenfolge umstellen.
- Bestehende Labels und Werte nicht umbenennen.

### Pruefung

- Unit-Test fuer die Reihenfolge.
- Manuell pruefen: Expert Settings zeigen `HARDWARE` direkt unter Modern Controls.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

## Phase 5: Auto-Bandage Scaling

### Ziel

Auto-Bandage darf auf Widescreen-Displays keinen kurzen 4:3-Sprung mehr verursachen.

### Umsetzung

- Debug-Trace fuer Screen-Transitions ergaenzen:
  - `SCREEN_WIDTH`
  - `SCREEN_HEIGHT`
  - `STD_SCREEN_X/Y`
  - `INV_INTERFACE_START_Y`
  - aktueller Screen
  - Map-/Tactical-Status
- Sicherstellen, dass Auto-Bandage beim Eintritt und Austritt keine temporaere 4:3-Geometrie erzwingt.
- Panel ueber aktuelle Screen-Werte im taktischen Bereich oberhalb der Bottom-UI zentrieren.
- Input-Maske und Invalidierungsbereiche mit aktuellen `SCREEN_WIDTH/HEIGHT` berechnen.
- Auf Enter/Exit einen vollen Render-Refresh erzwingen.

### Pruefung

- Manuell pruefen: Auto-Bandage auf breiter Android-Aufloesung startet ohne 4:3-Sprung.
- Manuell pruefen: Auto-Bandage-Panel ist zentriert und klickbar.
- Manuell pruefen: Rueckkehr zum Tactical Screen hat keine falschen Dirty-Rects oder Cropping-Artefakte.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

## Phase 6: Shopkeeper / Vendor Scaling

### Ziel

Vendor-/Shopkeeper-Screens duerfen auf breiten Displays nicht abgeschnitten oder falsch skaliert sein.

### Umsetzung

- Fuer den Shopkeeper-Screen einen gemeinsamen SKI-Origin relativ zu `STD_SCREEN_X/Y` einfuehren.
- Feste 640x480-Shopkeeper-Koordinaten ueber diesen Origin berechnen.
- Folgende Bereiche auf denselben Origin umstellen:
  - Blits;
  - Mouse Regions;
  - Restore-Rects;
  - Invalidate-Rects;
  - Item-Description-Anker;
  - relevante Popup-Anker.
- Popups gegen aktuelle `SCREEN_WIDTH/HEIGHT` klemmen.
- Taktische Hintergrundstreifen oder Vollbreitenbereiche nur dort full-width lassen, wo das UI sie tatsaechlich so erwartet.

### Pruefung

- Manuell pruefen: Shopkeeper ist auf breiter Android-Aufloesung vollstaendig sichtbar.
- Manuell pruefen: alle Shopkeeper-Buttons und Item-Slots sind klickbar.
- Manuell pruefen: Item-Beschreibung und Popups liegen innerhalb des sichtbaren Bereichs.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

## Phase 7: Gesamter Screen-Audit

### Ziel

Alle wichtigen Screens sollen gegen Widescreen-Geometrie, Klickbereiche, Dirty-Rects und Popups geprueft werden.

### Umsetzung

- Verbindliche Layout-Regel anwenden:
  - klassische 640x480-Screens und Modals werden ueber `STD_SCREEN_X/Y` zentriert;
  - taktische Welt, Viewport und Bottom-UI verwenden `SCREEN_WIDTH/HEIGHT` und `INV_INTERFACE_START_Y`;
  - Popup- und Dirty-Rects duerfen keine impliziten 640x480-Grenzen verwenden.
- Screen-Audit-Matrix abarbeiten:
  - Tactical normal;
  - Tactical Inventory;
  - Auto-Bandage;
  - Item Description;
  - Message Boxes;
  - Sector Exit;
  - Map;
  - Sector Inventory;
  - Assignment-/Contract-Menues;
  - Laptop AIM;
  - Laptop MERC;
  - Bobby Ray;
  - Personnel;
  - Finances;
  - Email;
  - Save/Load;
  - Options;
  - Help;
  - Shopkeeper;
  - NPC Dialog/Subtitles.
- Gefundene Abweichungen nach demselben Muster beheben:
  - falsche fixe Koordinate identifizieren;
  - auf passenden Layout-Origin oder aktuelle Screen-Groesse umstellen;
  - Mouse Region, Blit und Invalidate gemeinsam korrigieren.

### Pruefung

- Audit auf mindestens einer 16:9- und einer ultrabreiten Android-Aufloesung.
- Pro Screen pruefen:
  - sichtbar;
  - korrekt zentriert oder bewusst full-width;
  - klickbar;
  - keine abgeschnittenen Popups;
  - keine falschen Dirty-Rects;
  - Rueckkehr zum vorherigen Screen korrekt.
- Danach Log und Plan aktualisieren, Ergebnisse erneut pruefen, committen und stoppen.

## Phase 8: Final Verification und APK

### Ziel

Alle acht Punkte werden gemeinsam verifiziert. Eine APK darf fuer die Verifikation gebaut werden; Google-Drive-Upload erfolgt erst zur Endabnahme.

### Umsetzung

- Vollstaendige Android-Test-Suite ausfuehren:
  - `android\gradlew.bat testDebugUnitTest`
- Debug APK bauen:
  - `android\gradlew.bat assembleDebug`
- Falls native Tests oder ein nativer Build-Check im Projekt vorhanden sind, diese ebenfalls ausfuehren.
- Alle acht Fixbereiche nochmals gegen die urspruengliche To-do-Liste abgleichen.
- Log und Plan final aktualisieren.
- Finalen Commit fuer die Verifikationsphase erstellen.
- Stoppen und auf Endabnahme warten.

### Abnahmekriterien

- Branch ist weiterhin `experimental`.
- Alle acht offenen Punkte sind umgesetzt oder mit begruendetem Restbefund dokumentiert.
- Tests laufen erfolgreich oder bekannte Testausfaelle sind dokumentiert.
- APK wurde nur lokal zur Verifikation erstellt.
- Kein Google-Drive-Upload vor Endabnahme.
