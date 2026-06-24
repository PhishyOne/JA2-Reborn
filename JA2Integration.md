# JA2 Stracciatella Integration Plan

## Ziel

Die Upstream-Patches aus dem offiziellen JA2 Stracciatella Repository sauber in
JA2 Reborn übernehmen.

- Upstream-Basis: `6cd7bc2a` (`Merge pull request #2378 from momoko-h/MPrintBuffer`, 2026-05-01)
- Upstream-Ziel: `a3bd56e65e6adfd9f12994ddc1a523a0be784bf3` (`Merge pull request #2390 from steph-pg/externalize-suppression`, 2026-06-23)
- Umfang: alle 8 Upstream-Commits aus `6cd7bc2a..a3bd56e`
- Commit-Strategie: ein sauberer Sync-Commit pro abgeschlossener Phase, keine ungeplanten Mischcommits
- Android-App-Version bleibt unverändert, solange keine neue JA2-Reborn-Release vorbereitet wird

## Verbindliche Arbeitsregeln

1. Gearbeitet wird nur auf dem Branch `experimental`.
2. Vor Abschluss einer Phase werden die Ergebnisse ein zweites Mal sorgfältig geprüft.
3. Bei Abschluss einer Phase werden Integrationslog und Plan aktualisiert und zusammen committed.
4. Nach Abschluss jeder Phase wird gestoppt, damit der Kontext bei Bedarf geleert werden kann.
5. Ein Merge von `experimental` nach `main` darf erst nach manuellem Test und ausdrücklicher Freigabe durch den Nutzer erfolgen.

## Status

- Phase 0: abgeschlossen am 2026-06-24 mit Commit `749a27479`; JA2-Reborn-Ausgangspunkt war `72b6e98a4eb832a42cbeb95c6154cec8c2180116`.
- Phase 1: abgeschlossen am 2026-06-24 mit Commit `a54aa1320`.
- Phase 2: abgeschlossen am 2026-06-24 mit Commit `1af7d1559`.
- Phase 3: abgeschlossen am 2026-06-24.
- Merge nach `main`: blockiert bis manueller Test und ausdrückliche Nutzerfreigabe erfolgt sind.

## Phase 0 - Vorbereitung und Sicherheitsnetz

Status: abgeschlossen am 2026-06-24.

Ziel: sicherstellen, dass die Integration reproduzierbar vorbereitet ist, ohne
bereits Upstream-Code zu übernehmen.

- Prüfen, dass `experimental` aktiv ist und der Arbeitsbaum sauber ist:
  - `git status --short --branch`
  - `git branch --show-current`
- Falls `upstream` fehlt, Remote hinzufügen:
  - `git remote add upstream https://github.com/ja2-stracciatella/ja2-stracciatella.git`
- Upstream aktualisieren:
  - `git fetch upstream --tags --prune`
- Zielcommit prüfen:
  - `git show -s --format="%H %ad %s" --date=iso-strict a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
- Arbeitsstand dokumentieren:
  - aktueller JA2-Reborn-Commit
  - aktive Branches
  - Upstream-Range
  - erwarteter Umfang: 13 Dateien, ca. 209 Insertions / 25 Deletions
- `JA2IntegrationLog.md` anlegen oder aktualisieren.

Zweite Prüfung vor Abschluss:

- Nochmal `git status --short --branch`.
- Nochmal prüfen, dass keine Upstream-Code-Dateien verändert wurden.
- Plan und Log auf korrekte Commit-Hashes prüfen.

Abschluss:

- `JA2Integration.md` und `JA2IntegrationLog.md` committen.
- Danach stoppen.

## Phase 1 - Upstream-Patch anwenden

Status: abgeschlossen am 2026-06-24.

Ziel: die komplette Upstream-Range in den Android-Port übernehmen, ohne lokale
Port-Änderungen zu verlieren.

- Patch erzeugen und anwenden:
  - `git diff --binary 6cd7bc2a..a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
  - wegen CRLF/LF-Kontext auf Windows mit Whitespace-Toleranz anwenden:
    `git apply --ignore-space-change --ignore-whitespace`
- Erwartete geänderte Bereiche:
  - `assets/externalized/game.json`
  - `rust/stracciatella/src/schemas/yaml/game.schema.yaml`
  - `src/externalized/policy/*`
  - `src/game/GameVersion.h`
  - `src/game/Strategic/*`
  - `src/game/Tactical/*`
- Keine Formatierung und kein unrelated Cleanup ausführen.

Inhaltlich zu übernehmen:

- Stat-Healing durch Doctoring:
  - `enable_stat_healing`
  - Savegame-Version-Bump
  - persistierte Stat-Damage-Felder
  - zugehörige Load/Save-Tests
- Mapscreen-Keyring-Popup beim Verlassen des Mapscreens schließen.
- Down-Arrow-Rendering-Fix in `Interface.cc`.
- Externalisierte Suppression-Werte:
  - `suppression_fire_modifier`
  - `suppression_fire_reaction_threshold`

Port-spezifisch zu bewahren:

- JA2-Reborn Scroll-Guard in `PrintAboveGuy()` bleibt erhalten.
- JA2-Reborn `CheatSystem`-Hooks in `Weapons.cc` bleiben erhalten.
- `version` bleibt `1.0.5`.

Zweite Prüfung vor Abschluss:

- `git diff --check`
- `rg -n "^(<<<<<<<|=======|>>>>>>>)" --glob "!dependencies/**" .`
- gezielt prüfen:
  - `src/game/Tactical/Interface.cc`
  - `src/game/Tactical/Weapons.cc`
  - `src/game/GameVersion.h`
  - `version`
- Sicherstellen, dass keine Android-Port-Features entfernt wurden.

Abschluss:

- Integrationslog mit angewendeter Range und Auffälligkeiten aktualisieren.
- Planstatus aktualisieren.
- Commit erstellen.
- Danach stoppen.

## Phase 2 - Build- und Testvalidierung

Status: abgeschlossen am 2026-06-24.

Ziel: sicherstellen, dass der native Code, Rust-Schemas und Android-Builds nach
dem Sync funktionieren.

Tests und Checks:

- Root:
  - `git diff --check`
- Rust:
  - im Ordner `rust`: `cargo test`
- Android:
  - im Ordner `android`: `.\gradlew.bat :app:testDebugUnitTest`
  - im Ordner `android`: `.\gradlew.bat :app:externalNativeBuildDebug`
  - im Ordner `android`: `.\gradlew.bat :app:assembleRelease --rerun-tasks`

Manuelle Review-Schwerpunkte:

- Alte Saves vor Savegame-Version 103 dürfen nicht offensichtlich brechen.
- Neue Stat-Damage-Felder werden beim Laden alter Saves sinnvoll initialisiert.
- Neue Config-Felder haben sichere Defaults.
- Suppression-Externalisierung ändert ohne Config-Anpassung nicht unerwartet das Balancing.
- Touch-, Widescreen- und CheatSystem-Portänderungen sind weiterhin vorhanden.

Zweite Prüfung vor Abschluss:

- Testergebnisse nochmal gegen Log prüfen.
- `git status --short --branch`
- Prüfen, ob Build-Artefakte oder lokale Dateien versehentlich getrackt werden.

Abschluss:

- Integrationslog mit allen Testergebnissen aktualisieren.
- Planstatus aktualisieren.
- Commit erstellen.
- Danach stoppen.

## Phase 3 - Abschlussreview und Merge-Vorbereitung

Status: abgeschlossen am 2026-06-24.

Ziel: die Integration für einen späteren Merge von `experimental` nach `main`
vorbereiten, aber nicht automatisch nach `main` mergen.
Der spätere Merge ist zusätzlich bis zum manuellen Test und zur ausdrücklichen
Freigabe durch den Nutzer blockiert.

- Gesamtdiff reviewen:
  - Upstream-Sync-Commit
  - Log-/Plan-Commit
  - Testvalidierungs-Commit
- `CHANGELOG.md` aktualisieren, falls die Änderungen öffentlich erwähnt werden sollen:
  - kurzer Eintrag unter einer passenden kommenden Version oder `Unreleased`
  - kein Android-Version-Bump ohne Release-Entscheidung
- README-Upstream-Abschnitt korrigieren, falls gewünscht:
  - fehlenden `android-port-base`-Ref durch konkrete Basis `6cd7bc2a` ersetzen
  - letzten synchronisierten Upstream-Stand `a3bd56e` dokumentieren
- Finalen Merge-Hinweis vorbereiten:
  - Range
  - Tests
  - bekannte Risiken
  - nicht getestete manuelle Gameplay-Szenarien
  - Merge-Sperre bis manueller Test und Nutzerfreigabe erfolgt sind

Zweite Prüfung vor Abschluss:

- `git status --short --branch`
- `git log --oneline --decorate --max-count=12`
- nochmalige Prüfung von Plan, Log und Changelog.

Abschluss:

- Integrationslog final aktualisieren.
- Planstatus final aktualisieren.
- Commit erstellen.
- Danach stoppen.

## Bekannte Risiken

- Die Repository-Historie hat keinen gemeinsamen Upstream-Ancestor; ein normaler
  Merge ist deshalb nicht die bevorzugte Methode.
- Windows-CRLF kann normale Patch-Checks scheitern lassen, obwohl der Inhalt passt.
- `Interface.cc` und `Weapons.cc` enthalten JA2-Reborn-spezifische Änderungen, die
  beim Sync ausdrücklich erhalten bleiben müssen.
- Savegame-Version-Bump ist native Stracciatella-Logik und darf nicht mit der
  Android-App-Version verwechselt werden.
