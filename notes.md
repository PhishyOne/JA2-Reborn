# JA2 Reborn — Notes

Laufende Notizen und To-dos. Stand: 2026-06-24 | Branch: `experimental`

---

## v1.0.5 — Offene Punkte

### 1. Grab/Examine-Button — ✅ erledigt und funktional (CTRL-Toggle)

**User-Wunsch:** Ein STRG-Modifier-Button im Touch-Overlay, der in Kombination mit Rechtsklick/Tap die Examine-Funktion auslöst (Items vom Boden aufheben, Container/Leichen durchsuchen, Fallen erkennen).

**Hintergrund:** Die native Examine-Aktion ist `STRG + rechte Maustaste`. Wählt der Spieler einen Söldner aus (idealerweise mit hohem Explosives-Wert wie Slay, Fidel oder Barry — erkennen Fallen am besten) und hält STRG gedrückt, verwandelt sich der Mauszeiger in das Hand-Symbol (Interaktions-Modus). Ein STRG-Toggle-Button im Overlay macht das auf Touch-Geräten ohne physische Tastatur nutzbar.

**Umsetzung:**
- ✅ STRG-Modifier-Toggle (analog zum existierenden ALT- und SHIFT-Toggle) mit Icon `icon_action_examine.svg` (Hand-Symbol) aus Batch_Extra.
- ✅ Toggle-Verhalten: antippen = STRG gedrückt halten (Examine aktiv), nochmal antippen = loslassen.
- ✅ Button in der Modifier-Toggle-Gruppe im Tactical Overlay (rechts unten, unter ALT-Toggle bei x=0.91, y=0.544).
- ✅ Nur im Tactical Screen sichtbar; Map Screen nicht betroffen.
- ✅ Funktional auf Gerät bestätigt.

### 2. Drei-Wege-Slider: Touch-Eingabemodus (Map Screen) — ✅ erledigt und funktional

**User-Wunsch:** Ein Drei-Wege-Slider im Touch-Overlay-Settings-Menü für den Map Screen, um den Eingabemodus zu wählen.

**Hintergrund:** Aktuell läuft ein Mischsystem aus Direct Touch (direktes Antippen von Spielelementen) und Relative Touchpad Mouse (Wischen bewegt Mauszeiger relativ). Nutzer möchten für den Map Screen entscheiden können, ob sie beide Modi parallel nutzen oder sich auf einen beschränken wollen.

**Slider-Stufen:**
- **Both** (Standard) — Direct Touch + Touchpad Mouse parallel.
- **Direct Touch Only** — Nur direktes Antippen, kein Mauszeiger-Wischen, Overlay-Mausbuttons deaktiviert.
- **Touchpad Mouse Only** — Nur relatives Mauspad, keine Direct-Touch-Hit-Tests, Overlay-Mausbuttons immer aktiv.

**Umsetzung:**
- ✅ `map_screen_input_mode` Feld in `TouchOverlayConfig` (Schema v15→v16, Default `"both"`).
- ✅ 3-Wege-SeekBar im Touch-Overlay-Settings-Dialog (unter Direct-Tap-Sektion).
- ✅ `SDLSurface.dispatchNativeTouch()`: Modus-Enforcement im Map Screen — *direct_touch* überspringt Maus-Handling, *touchpad_mouse* erzwingt Touchpad-Maus.
- ✅ `SDLSurface.performOverlayMouseButton()`: Mausbuttons je nach Modus aktiv/inaktiv.
- ✅ `SDLSurface.shouldDirectTapAtFinger()`: Unterdrückt Direct-Tap-Arbitration bei *touchpad_mouse*.
- ✅ Persistenz in `touch_buttons.json`, geladen via `TouchButtonStore`.
- ✅ Funktional auf Gerät bestätigt (Fix: Direct-Tap-Arbitration in v2).

### 3. Touch-Overlay-Settings-Menü: Tabs statt Fließtext-Wüste

**User-Wunsch:** Das Touch-Overlay-Settings-Menü ist über die Versionen zu einer langen, unübersichtlichen Scroll-Liste angewachsen. Es soll in logische Tabs aufgeteilt werden.

**Hintergrund:** Mit jedem neuen Feature (DirectTap-Slider, HARDWARE-Mode, Panel-Scaling, bald Eingabemodus-Slider etc.) wurde das Menü länger. Neueinsteiger und auch Power-User verlieren den Überblick. Eine Tab-Struktur gruppiert verwandte Einstellungen und macht das Menü scanbar.

**Zu klären (Tommy überlegt noch):**
- Welche Tabs? Mögliche Kandidaten: General/Behavior, Layout/Buttons, Scaling/Display, Expert/Advanced.
- Tab-Navigation: Top-Tab-Bar oder Bottom-Navigation? Seitliches Drawer-Menü?
- Persistenz der Tab-Auswahl über Sitzungen hinweg?
- Soll die Struktur nur organisatorisch sein oder auch Sichtbarkeitsregeln einführen (z.B. Expert-Tab ausblendbar)?

### 4. Hardware-Mode: Echter Gerätetest mit Maus & Tastatur

**User-Wunsch:** Der Mouse & Keyboard Hardware Mode (Phase 5) ist implementiert, aber noch nicht mit echter Peripherie auf einem Android-Gerät getestet worden. Das muss vor v1.0.5 nachgeholt werden.

**Testumfang:**
- Physische Maus per USB/Bluetooth anschließen: Zeigerbewegung, Links-/Rechtsklick, Scrollrad.
- Physische Tastatur per USB/Bluetooth: Alle gemappten Hotkeys (ESC, Enter, Tab, Modifier etc.), Spezialtasten.
- Zusammenspiel: Maus + Tastatur gleichzeitig, kein Konflikt mit Touch-Overlay.
- HARDWARE-Mode-Aktivierung: Automatische Erkennung beim Anschließen, manuelles Umschalten im Menü.
- HARDWARE-Mode-Deaktivierung: Touch-Overlay erscheint wieder korrekt, wenn Geräte entfernt werden.
- Verschiedene Geräteklassen: Einfache BT-Maus, Gaming-Maus mit Zusatztasten, Standard-USB-Keyboard, kompakte BT-Tastatur.

**Zu klären:**
- Welches Gerät nutzt Tommy für den Test? (Eigenes Phone/Tablet + verfügbare Peripherie)
- Testprotokoll: Checkliste oder freie Exploration?
- Fallback, wenn keine Hardware verfügbar: Emulator-Test mit simulter Eingabe?

### 5. Mod-Support-Recherche: Top-5-Stracciatella-Mods pro Kategorie

**User-Wunsch:** Vor der Planung der ersten Mod-Support-Implementierungen (Phase 6) muss recherchiert werden, welche Stracciatella-Mods am beliebtesten sind. Ziel ist eine fundierte Priorisierung, welche Mod-Typen zuerst unterstützt werden sollen.

**Recherche-Quellen:**
- Stracciatella-GitHub-Repo (Discussions, Issues mit Mod-Bezug, Wiki)
- Community-Hubs: The Bear's Pit Forum, JA2-Community-Discord, ModDB
- Direkte Durchsicht bekannter Mod-Repos / Releases

**Zu ermitteln pro Mod:**
- Name, Kategorie (Total Conversion, Item/Tileset, KI/Kampf, UI/QoL, Kampagne/Story, Grafik/Sound)
- Installationsbasis / Download-Zahlen / Community-Aktivität
- Technische Basis: Reiner Data-Drop oder DLL-Patching? Kompatibel mit aktueller Stracciatella?
- Lizenz / Erlaubnis zur Integration?

**Ziel-Lieferung:** Kurzliste der 5 relevantesten Mods mit Kategorie, technischer Analyse und Empfehlung für erste Mod-Support-Priorität. Dient als Entscheidungsgrundlage für den Mod-Support-Implementierungsplan.

### 6. Stracciatella-Upstream-Bugfixes übernehmen (Android-relevant)

**User-Wunsch:** Wichtige Bugfixes aus dem Upstream `ja2-stracciatella/ja2-stracciatella` (Stand: ~90 Commits seit v0.22.1 / Okt 2025) übernehmen, aber nur was für Android relevant ist.

**Hintergrund:** Der Upstream hat seit dem Fork zahlreiche Bugfixes und Verbesserungen erhalten. Besonders die Dach-/Bullet-Fixes und der Heal-Stats-via-Doctoring sind spielrelevant. Die Refactorings (GridSquare, STRUCTURE, TILE_ANIMATION_DATA) sind dagegen für uns nur interessant, wenn sie späteres Mergen erleichtern — jetzt aber kein Selbstzweck.

**Vorgehen:**
- Relevante Commits aus dem Upstream identifizieren und auf Android-Relevanz prüfen.
- Nur cherry-picken, was auf Android tatsächlich spürbar ist (Kampf-Fixes, UI-Fixes, Crash-Fixes).
- Keine reinen C++-Refactorings ohne Spieler-Impact (GridSquare, MPrint, C++20-Utils).
- Keine Dependency-Bumps ohne Notwendigkeit.
- Jeder übernommene Fix muss Build und Laufzeit auf Android nicht verschlechtern.

**Prioritäre Kandidaten (vorausgewählt):**
- Dach-/Bullet-Kampf-Fixes: Ziel-Z-Position, Bullet-Roof-Kollision, Roof-zu-Roof, Mörser-Trajektorie, AP-Formel
- Heal-Stats-via-Doctoring (`#2377`)
- Crash-Fixes: `pAnimData`-Null-Check, Iterator-Invalidation, Use-after-free
- UI-Fix: 0%-Trefferchance-Anzeige, Keyring-Popup-Shutdown

### 7. Stealth-Toggle: Zustands-Sync bei Söldner-Wechsel — ✅ erledigt und funktional

**User-Wunsch:** Der Stealth-Toggle muss beim Wechsel des aktiven Söldners den tatsächlichen Sneak-Status des neu ausgewählten Söldners abfragen und anzeigen.

**Hintergrund:** Ist der Stealth-Toggle aktiv und der Spieler wechselt zu einem anderen Söldner, bleibt der Toggle aktuell im alten Zustand — auch wenn der neue Söldner gar nicht im Sneak-Mode ist. Das führt zu einer Diskrepanz zwischen angezeigtem Toggle-Status und tatsächlichem Söldner-Verhalten.

**Umsetzung:**
- ✅ Native JNI-Abfrage `getSelectedMercStealthMode()` ergänzt, die den tatsächlichen `bStealthMode` des aktuell ausgewählten Mercs liefert.
- ✅ Touch-Overlay synchronisiert den visuellen `stealth_toggle`-Status regelmäßig im Tactical Screen und beim Screen-/Button-Neuaufbau.
- ✅ Toggle-Zustand wird nicht global persistiert, sondern aus dem aktuell selektierten Merc abgeleitet.
- ✅ Funktional auf Gerät bestätigt: APK `1.0.5` ließ sich über `1.0.4` installieren, Fix sitzt sauber.
- Edge Case Team-/Multi-Select: Anzeige folgt weiterhin dem aktuell ausgewählten Merc; kein gemischter Dreizustand implementiert.

### 8. Item Stacking / Sidestep Toggle Auto-Release — ✅ erledigt und funktional

**User-Wunsch:** Aktive Modifier-Toggles dürfen spätere Useraktionen nicht blockieren. Wenn z.B. Sidestep (`ALT`) aktiv ist und der Spieler den Söldner wechseln möchte, muss die Useraktion Vorrang haben.

**Umsetzung:**
- ✅ Sticky Modifier werden bei neuen Nicht-Modifier-Aktionen automatisch per KeyUp gelöst.
- ✅ Sidestep/Backstep (`ALT`) blockiert den Söldnerwechsel über Team-Portraits nicht mehr.
- ✅ Item Stacking (`SHIFT`) bleibt dort aktiv, wo es für die Bedienung benötigt wird: Tactical-Inventarbereich und Map Screen.
- ✅ Die visuelle Toggle-Anzeige wird beim Auto-Release synchron zurückgesetzt.
- ✅ Funktional auf Gerät bestätigt: Sidestep, Tactical-Item-Stacking und Map-Screen-Item-Stacking funktionieren komplett.

---

## Changelog

### 2026-06-24 — v1.0.5
- **Map Screen Control Slider** (#2): Drei-Wege-SeekBar im Settings-Dialog für den Map Screen Touch-Eingabemodus (Both / Direct Touch Only / Touchpad Mouse Only). Persistiert in `touch_buttons.json`. Enforcement in SDLSurface via `dispatchNativeTouch`, `performOverlayMouseButton` und `shouldDirectTapAtFinger`.
- **STRG/CTRL Examine Toggle** hinzugefügt: Neuer Modifier-Toggle-Button im Tactical Overlay (Hand-Icon, direkt unter ALT-Toggle). Ermöglicht Examine/Grab per STRG-Tap auf Touch-Geräten ohne physische Tastatur.
- **Deutsche Labels korrigiert**: Examine-Button von "Examine (STRG)" auf "Untersuchen (STRG)", Item-Stacking-Button von "Item Stacking" auf "Item-Mehrfachauswahl".
- Launcher-Überschrift von v1.0.3 beta auf v1.0.5 aktualisiert.

### 2026-06-22
- **Stealth-Toggle Sync** (#7): Toggle synchronisiert jetzt korrekt den Sneak-Status beim Söldner-Wechsel.
- **Item Stacking / Sidestep Auto-Release** (#8): Sticky Modifier-Toggles blockieren keine Useraktionen mehr. ALT wird bei Söldnerwechsel automatisch gelöst; SHIFT bleibt in Inventar/Map Screen aktiv.
- **v1.0.5 Planung**: Grab/Examine-Button, Drei-Wege-Eingabemodus-Slider, Settings-Tabs, Hardware-Mode-Test, Mod-Support-Recherche, Upstream-Bugfixes.

---
