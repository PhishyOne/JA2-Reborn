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

Update 104 Phase 1 ist abgeschlossen. `map_inventory` verwendet nun `ENTER`; Schema 13 migriert alte Layouts zentral und ergaenzt fehlende Map-Screen-Buttons.
Update 104 Phase 2 ist abgeschlossen. Tactical- und Map-Screen-Presets sind getrennt; Export/Import bleibt Full-Layout-basiert und erhaelt `buttons` und `map_screen_buttons`.
Update 104 Phase 3 ist abgeschlossen. Batch-02/03-Icon-Transforms, Lock-Icon-Groesse und Reload-Preset sind umgesetzt.
Update 104 Phase 4 ist abgeschlossen. `HARDWARE` steht in der gemeinsamen Mouse-Mode-Reihenfolge direkt unter Modern Controls.
Update 104 Phase 5 ist abgeschlossen. Auto-Bandage nutzt aktuelle Widescreen-Geometrie, geklemmte Dirty-Rects und Full-Refresh auf Enter/Exit.

Status der **8 Known Issues**: 7 behoben, 1 teilweise behoben und weiter in Arbeit.

1. **`map_inventory`-Button mapped auf `I`** — Behoben in Update 104 Phase 1.
2. **Batch 02+03 Icon-Alignment** - Behoben in Update 104 Phase 3.
3. **Button-Editor kennt keine Map-Screen-Presets** - Behoben in Update 104 Phase 2.
4. **Touch-Preset-Export/Import trennt nicht nach Screen-Kontext** - Behoben in Update 104 Phase 2.
5. **Lock-Button zu klein nach SVG-Umstellung** - Behoben in Update 104 Phase 3.
6. **Reload-Button pruefen** - Behoben in Update 104 Phase 3.
7. **Hardware Mouse/Keyboard-Mode an falscher Dropdown-Position** - Behoben in Update 104 Phase 4.
8. **Screen-Skalierung unvollständig** — Nicht alle Bildschirme skalieren korrekt. Auto-Aid-Button: Das Spiel springt während der Aktion aus dem Widescreen in den 4:3-Modus und danach zurück. Händler-Bildschirm: User berichten von gecroppter und falsch skalierter Darstellung. **Prüfauftrag:** Das gesamte Spiel auf sämtliche Bildschirme durchgehen, die bei der Widescreen-Anpassung möglicherweise übersehen wurden (alle Spiel-Modi, Menüs, Dialoge, Inventar-, Händler-, Laptop-, Vertrags- und sonstige UI-Screens).

Update 104 Phase 5 behebt den Auto-Bandage-Anteil von Known Issue 8; Haendler-Bildschirm und gesamter Screen-Audit bleiben fuer Phase 6/7 offen.

## Regeln

- Zielbranch: `experimental`
- Nach jeder Änderung: Zweitprüfung, Tests, Log/Plan aktualisieren, dann **stoppen**
- Keine Release-/Abgabe-APK vor Endabnahme; `assembleDebug` fuer Checks und eine explizit freigegebene Test-APK sind erlaubt
