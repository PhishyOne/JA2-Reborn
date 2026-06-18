# JA2 Reborn Android To-do — Phasenplan-Status

> Quelle: `D:/Coding/JA2 Update 2 Plan.md`
> Stand: 2026-06-18

| Phase | Beschreibung | Status |
|-------|-------------|--------|
| 0 | Setup und Baseline | ✅ Abgeschlossen |
| 1 | Mapscreen-Overlay-Grundlage | ✅ Abgeschlossen |
| 2 | Modifier und Mapscreen-Buttonset | ✅ Abgeschlossen |
| 3 | Strafing | ✅ Abgeschlossen |
| 4 | DirectTap-Arbitration | ✅ Abgeschlossen |
| 5 | Hardware Mouse/Keyboard Mode | ✅ Abgeschlossen |
| 6 | Mod Support v1 | 🔀 Ausgelagert in eigenen Plan |
| 7 | SVG-Icon-System + 37 Icons | ✅ Abgeschlossen |
| F | Finale Phase (APK + Endabnahme) | ⏳ Ausstehend |

## Reduzierungen (bewusst gestrichen)

- **Batches 07+08 (Extras UI, Cpp Inline)**: Gestrichen — keine weiteren Icons nötig.
- **Map-Screen-Buttons**: Von 20 auf 8 reduziert, 12 Legacy-Buttons aussortiert.

## Aktueller Status: Feature-Entwicklung abgeschlossen

Das Projekt ist **vollständig implementiert** bis auf **7 Known Issues**:

1. **`map_inventory`-Button mapped auf `I`** (Item-Highlight) statt Inventar-Panel zu öffnen. Korrekter Key wäre `Enter` — muss auf Enter umgemappt werden.
2. **Batch 02+03 Icon-Alignment** (Stances + Combat) — `iconFill`/`iconOffset`-Werte müssen getuned werden. Die Icons stammen aus dem Game Icon Converter (`D:/Coding/Game-Icon-Converter/`), die Vorgaben dazu stehen im Overlay Icon Manual (`D:/Coding/OverlayIconManual.md`). Per-Icon-Werte aus dem Converter-Export (`iconset.json`) ins Projekt übernehmen und an die Soll-Vorgaben des Manuals anpassen.
3. **Button-Editor kennt keine Map-Screen-Presets** — `TOUCH_BUTTON_PRESETS` enthält fast nur taktische Presets (Maus, Bewegung, Kampf, UI). Die Map-Screen-Buttons (Laptop, Options, Zeit±, etc.) haben dort keine Einträge. Öffnet man im Editor einen Map-Screen-Button, findet `touchButtonPresetFor()` per Icon/Action keinen Match und das Dropdown springt auf den ersten taktischen Eintrag zurück — aus dem Map-Button wird ungewollt ein taktischer Button. **Nötig:** Preset-Liste nach Screen-Kontext trennen. In-Game: nur taktische Presets anzeigen. Map-Screen: nur Map-Screen-Presets anzeigen (und den Create-New-Dialog entsprechend bestücken).
4. **Touch-Preset-Export/Import trennt nicht nach Screen-Kontext** — Das Export-/Import-System muss geprüft und sichergestellt werden, dass es `mapScreenButtons` und taktische `buttons` korrekt getrennt behandelt. Ein Export darf Map-Buttons nicht mit In-Game-Buttons vermischen, ein Import muss beide Button-Sets in die jeweils richtigen Felder der `TouchOverlayConfig` zurückschreiben.
5. **Lock-Button zu klein nach SVG-Umstellung** — Seit dem Wechsel von Canvas-Padlock auf SVG-Rendering ist der Lock-Button winzig im Vergleich zu den Canvas-Icons. Die korrekte Größe muss wiederhergestellt werden.
6. **Reload-Button prüfen** — Es ist unklar, ob JA2 selbst einen Reload-Key/-Button im Spiel hat. Kein Icon dafür im Set, was verwundert (Nachladen ist eine Standardaktion). Prüfen, ob es einen nativen Reload-Key gibt und ob ein Overlay-Button dafür ergänzt werden muss.
7. **Hardware Mouse/Keyboard-Mode an falscher Dropdown-Position** — Der HARDWARE-Mode bleibt in den Expert Settings, aber die Dropdown-Reihenfolge ist falsch. Er soll **direkt unter Modern Controls** im Dropdown stehen, nicht an letzter Stelle.

## Regeln

- Zielbranch: `experimental`
- Nach jeder Änderung: Zweitprüfung, Tests, Log/Plan aktualisieren, dann **stoppen**
- Keine Release-/Abgabe-APK vor Endabnahme; `assembleDebug` fuer Checks und eine explizit freigegebene Test-APK sind erlaubt
