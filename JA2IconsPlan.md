# JA2 Reborn — Icon-Erstellung & Integration Plan

## Context

JA2 Reborn hat ein Touch-Overlay-System für Android. Die Icons werden aktuell entweder per Canvas programmatisch gezeichnet oder (bei genau einem Icon: `stance_crouch`) als SVG aus `res/raw/` geladen via `SvgIconManager`. Insgesamt braucht es **67 Icons**. Der User erstellt die Icons als SVGs batchweise, danach integriere ich sie ins Projekt.

## Technischer Hintergrund

**So funktioniert das SVG-Ladessystem (`SvgIconManager`):**
1. SVGs liegen in `android/app/src/main/res/raw/` — benannt als `icon_<name>.svg`
2. `iconset.json` definiert Metadaten pro Icon (iconFill, offsets, scales)
3. `iconmappings.json` mappt Game-Icon-Name → iconset-Eintrags-Name
4. `SvgIconManager.init()` lädt beide JSONs beim App-Start, pre-cached alle SVGs als Bitmaps
5. `TouchOverlayButtonView.drawIcon()` fragt zuerst `SvgIconManager.renderIcon()` — findet es nix, fällt es auf Canvas-Zeichnung zurück

**Ablage:** Alle Icons als `.svg` in `android/app/src/main/res/raw/`
**Benennung:** `icon_<gameiconname>.svg` (z.B. `icon_mouse_left.svg`, `icon_end_turn.svg`)

---

## Phase A — Ordnerstruktur für User (Icon-Erstellung)

Der User braucht diesen Ordner — zum Arbeiten, NICHT im Projekt:

```
D:\Coding\JA2 Reborn\IconWork\
  ├── Batch_01_Tactical_Core\
  ├── Batch_02_Tactical_Stances\
  ├── Batch_03_Tactical_Combat\
  ├── Batch_04_Tactical_Actions\
  ├── Batch_05_MapScreen_Nav\
  ├── Batch_06_MapScreen_Display\
  ├── Batch_07_Extras_UI\
  └── Batch_08_Cpp_Inline\
```

Jeder Batch-Ordner enthält eine `README.txt` mit der Liste der zu erstellenden Icons (siehe unten). SVGs werden direkt im Batch-Ordner abgelegt.

**SVG-Vorgaben:**
- viewBox `0 0 64 64` (quadratisch, skaliert automatisch)
- Pfade relativ, nur fill/stroke mit Weiß (#FFFFFF) — das System tinted automatisch
- Keine Hintergründe, nur die Silhouette/Pfade

---

## Phase B — 8 Batches à max. 10 Icons

### Batch 01 — Tactical Core (11 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_01_Tactical_Core\
  icon_mouse_left.svg       → Linke Maustaste
  icon_mouse_right.svg      → Rechte Maustaste
  icon_mouse_middle.svg     → Mittlere Maustaste
  icon_end_turn.svg         → Zug beenden (D)
  icon_map.svg              → Kartenbildschirm (M)
  icon_cancel_action.svg    → Aktion abbrechen (ESC)
  icon_quick_save.svg       → Schnellspeichern (ALT+S)
  icon_quick_load.svg       → Schnellladen (ALT+L)
  icon_cheats.svg           → Cheats
  icon_lock_closed.svg      → Touch-Overlay sperren (verschlossen)
  icon_lock_open.svg        → Touch-Overlay entsperren (offen)
```

### Batch 02 — Tactical Stances (8 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_02_Tactical_Stances\
  icon_stance_stand.svg     → Aufstehen (S)
  icon_stance_crouch.svg    → Ducken (C)
  icon_stance_prone.svg     → Hinlegen (P)
  icon_stealth_toggle.svg   → Schleichmodus (Z)
  icon_run_toggle.svg       → Laufen an/aus (R)
  icon_alt_movement_hold.svg → Rückwärts/Seitwärts (ALT)
  icon_swap_places.svg      → Plätze tauschen (X)
  icon_level_toggle.svg     → Ebene wechseln (TAB)
```

### Batch 03 — Tactical Combat (7 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_03_Tactical_Combat\
  icon_fire_mode.svg        → Feuermodus wechseln (B)
  icon_look_direction.svg   → Blickrichtung ändern (L)
  icon_cycle_targets.svg    → Durch Ziele schalten (N)
  icon_range_cursor.svg     → Reichweite zum Cursor (F)
  icon_target_enemy.svg     → Nächsten Feind anvisieren (E)
  icon_auto_bandage.svg     → Automatisches Verbinden (A)
  icon_keyring.svg          → Schlüsselbund öffnen (K)

Hinweis: strafe_hold, strafe_toggle und alt_movement_hold nutzen alle
dasselbe Icon aus Batch 02 (icon_alt_movement_hold.svg).
```

### Batch 04 — Tactical Actions (3 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_04_Tactical_Actions\
  icon_blink_items.svg      → Gegenstände blinken (I)
  icon_wireframe.svg        → Drahtgitter (W)
  icon_treetops.svg         → Baumkronen (T)
```

### Batch 05 — MapScreen Navigation (3 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_05_MapScreen_Nav\
  icon_map_shift.svg        → Shift (Mehrfachauswahl Soeldner/Squads)
  icon_map_ctrl.svg         → Ctrl (Wegpunkte setzen)
  icon_map_alt.svg          → Alt (Feind-Marker einblenden)
```

### Batch 06 — MapScreen Display (5 Icons)
```
D:\Coding\JA2 Reborn\IconWork\Batch_06_MapScreen_Display\
  icon_map_options.svg      → Optionen (O)
  icon_map_time_minus.svg   → Zeit zurück (-)
  icon_map_time_plus.svg    → Zeit vor (+)
  icon_map_inventory.svg    → Inventar (I)
  icon_map_laptop.svg       → Laptop (L)
```

### Batch 07 — entfallen
Sämtliche Icons aus diesem Batch (merc_/squad_ Badges, sector_exit_south/west)
werden nicht als SVG erstellt und erhalten auch keine Touch-Overlay-Buttons.
Merc/Squad-Badges bleiben Engine-gezeichnet per Canvas-numberedSuffix.
Sector-Exit-Buttons existieren nicht.
```

### Batch 08 — entfallen
Stammt aus dem alten Stracciatella-Repo. Die Inline-Viewport-Buttons gibt es
im aktuellen Touch-System nicht mehr — alles läuft über das Overlay.

---

## Button-Policy für Touch-Overlay

**Regel: Nur für Icons aus Phase B werden Touch-Overlay-Buttons erstellt.** Alles andere ist entweder Engine-gezeichnet oder existiert nicht.

### Neue SVG-Buttons (aus Batches)
Alle Icons aus Batch 01–08 erhalten dedizierte Touch-Overlay-Buttons mit SVG.

### Engine-gezeichnete Buttons (behalten)
Diese Buttons bleiben wie bisher von der Engine per Canvas gezeichnet — kein SVG, kein Ersatz:

| Button | Zeichnung |
|--------|-----------|
| Zahnrad (Settings) | Engine-Canvas |
| Hilfe (Help) | Engine-Canvas |
| Plus (+) zum Hinzufügen neuer Buttons | Engine-Canvas |

### Keine Buttons (weder SVG noch Engine)
Folgende Funktionen bekommen **gar keinen** Touch-Overlay-Button — sie wurden aus den Batches entfernt und werden nie angeboten:

- Options (O)
- Version Info (V)
- Pause (PAUSE)
- Quit Game (ALT+X)
- Next Merc (SPACE)
- Sector Exit North
- Sector Exit East
- Map Left / Right
- Map Insert / Delete
- Map ESC / Enter / Space
- Map Towns / Mines / Teams / Militia / Airspace
- Sector Exit South / West

---

## Phase C — Integration (macht Claude)

Sobald der User einen Batch fertig hat und die SVGs nach `D:\Coding\JA2 Reborn\android\app\src\main\res\raw\` kopiert sind, mache ich:

1. **SVGs nach `res/raw/` prüfen** — Sind alle da? Korrekte Benennung?

2. **`iconset.json` erweitern** — Pro Icon ein Eintrag:
```json
{
  "name": "<internal_id>",
  "svg": "icon_<name>.svg",
  "iconFill": 1.0,
  "iconOffsetX": 0.0,
  "iconOffsetY": 0.0,
  "iconScaleX": 1.0,
  "iconScaleY": 1.0
}
```

3. **`iconmappings.json` erweitern** — Mapping: Game-Icon-Name → interne ID:
```json
{
  "mouse_left": "<internal_id>",
  ...
}
```

4. **Canvas-Fallback in `drawIcon()` entfernen** — Sobald ein SVG existiert, wird der `when`-Branch überflüssig (SvgIconManager läuft vorher). Die entsprechenden `draw*()`-Methoden können später aufgeräumt werden.

**Interne ID-Vergabe:** Ich verwende fortlaufende numerische IDs (z.B. `"2000000001"`, `"2000000002"`, ...) oder präfix-basierte IDs (`"mouse_left"` → ID `"mouse_left"`). Numerisch ist konsistenter mit dem bestehenden `"1000189978"`.

---

## Verification

1. APK bauen (`gradlew assembleRelease`)
2. App starten — Touch-Overlay erscheint mit den neuen Icons
3. Prüfen: Icons sind zentriert, nicht verzerrt, korrekt getinted
4. Falls Icon zu groß/klein/verschoben: `iconFill`, `iconOffsetX/Y` in `iconset.json` anpassen und neu bauen
