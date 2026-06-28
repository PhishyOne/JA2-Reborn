# JA2 Stracciatella Integration Log

## Zusammenfassung

- Ziel: Upstream-Patches aus dem offiziellen JA2 Stracciatella Repository sauber in JA2 Reborn übernehmen.
- Arbeitsbranch: `experimental`
- Upstream-Basis: `6cd7bc2ab49d88e95ff58b3300d232ace048fc37`
- Synchronisierter Upstream-Zielstand: `a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
- Umfang: 8 Upstream-Commits aus `6cd7bc2a..a3bd56e`
- Android-App-Version: unverändert `1.0.5`
- Phase-0-Commit: `749a27479` P0: Prepare upstream integration
- Phase-1-Commit: `a54aa1320` P1: Apply Stracciatella upstream sync
- Phase-2-Commit: `1af7d1559` P2: Validate upstream integration
- Phase-3-Commit: `6648d7c91` P3: Finalize upstream integration review
- Merge nach `main`: nicht ausgeführt.
- Merge-Freigabe: erst nach manuellem Test und ausdrücklicher Merge-Freigabe durch den Nutzer.

## Archivierte Planvorgaben

- Ein sauberer Sync-Commit pro abgeschlossener Phase, keine ungeplanten Mischcommits.
- Vor Abschluss jeder Phase wurden Status, Diff/Checks und Schwerpunktdateien erneut geprüft.
- Android-App-Version bleibt unverändert, solange keine neue JA2-Reborn-Release vorbereitet wird.
- Port-spezifisch zu bewahren:
  - JA2-Reborn-Scroll-Guard in `PrintAboveGuy()`
  - JA2-Reborn-`CheatSystem`-Hooks in `src/game/Tactical/Weapons.cc`
  - `version` bleibt `1.0.5`
- Bekannte Risiken:
  - Die Repository-Historie hat keinen gemeinsamen Upstream-Ancestor; ein normaler Merge war deshalb nicht die bevorzugte Methode.
  - Windows-CRLF kann Patch-/Diff-Warnungen verursachen, obwohl der Inhalt passt.
  - `Interface.cc` und `Weapons.cc` enthalten JA2-Reborn-spezifische Änderungen, die beim Sync ausdrücklich erhalten bleiben müssen.
  - Savegame-Version-Bump ist native Stracciatella-Logik und darf nicht mit der Android-App-Version verwechselt werden.

## Phase 0 - Vorbereitung und Sicherheitsnetz

Datum: 2026-06-24

### Ergebnis

- Aktiver Branch: `experimental`
- Arbeitsbaum vor Phase 0: sauber (`git status --short --branch` -> `## experimental`)
- JA2-Reborn-Ausgangspunkt: `72b6e98a4eb832a42cbeb95c6154cec8c2180116`
- `upstream`-Remote hinzugefügt: `https://github.com/ja2-stracciatella/ja2-stracciatella.git`
- Upstream mit `git fetch upstream --tags --prune` aktualisiert.
- Keine Upstream-Code-Dateien wurden in Phase 0 verändert.

### Aktive lokale Branches

- `experimental` -> `72b6e98a4`
- `main` -> `ccb868fbb` (`origin/main`)
- `backup/experimental-before-1.0.5-main-merge` -> `8958e9159`

### Upstream-Range

- Basis: `6cd7bc2ab49d88e95ff58b3300d232ace048fc37`
  - `2026-05-01T15:05:39+02:00 Merge pull request #2378 from momoko-h/MPrintBuffer`
- Ziel: `a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
  - `2026-06-23T17:48:32+02:00 Merge pull request #2390 from steph-pg/externalize-suppression`
- Umfang: 8 Commits
- Diffstat: 13 Dateien, 209 Insertions, 25 Deletions

### Upstream-Commits

1. `581c492d3` Add ability to heal stats through doctoring (#2377)
2. `9522865d7` Close keyring popup during mapscreen shutdown
3. `6999f3c34` Fix comment formatting in MapScreen.cc
4. `8435450c1` Merge pull request #2381 from cMurschall/patch-1
5. `05c239eea` Fix commit d571f2a regression
6. `b8928923e` Merge pull request #2385 from momoko-h/ArrowRegression
7. `a86bd8986` Externalize fire suppression modifier
8. `a3bd56e65` Merge pull request #2390 from steph-pg/externalize-suppression

### Erwartete geänderte Dateien in Phase 1

- `assets/externalized/game.json`
- `rust/stracciatella/src/schemas/yaml/game.schema.yaml`
- `src/externalized/policy/DefaultGamePolicy.cc`
- `src/externalized/policy/GamePolicy.h`
- `src/game/GameVersion.h`
- `src/game/Strategic/Assignments.cc`
- `src/game/Strategic/MapScreen.cc`
- `src/game/Tactical/Interface.cc`
- `src/game/Tactical/LoadSaveSoldierType.cc`
- `src/game/Tactical/LoadSaveSoldierType_unittest.cc`
- `src/game/Tactical/Overhead.cc`
- `src/game/Tactical/Soldier_Control.h`
- `src/game/Tactical/Weapons.cc`

### Zweite Prüfung

- Branch erneut geprüft: `experimental`
- Arbeitsbaum vor Dokumentationsänderung erneut sauber.
- Plan- und Log-Hashes gegen `git show` und `git rev-list` geprüft.
- Phase 0 enthält nur Dokumentation und Git-Remote-Vorbereitung, keinen Upstream-Code-Sync.

## Phase 1 - Upstream-Patch anwenden

Datum: 2026-06-24

### Ausgangspunkt

- Aktiver Branch: `experimental`
- Phase-0-Commit vor Anwendung: `749a27479`
- Arbeitsbaum vor Phase 1: sauber (`git status --short --branch` -> `## experimental`)
- Android-App-Version vor Anwendung: `1.0.5`

### Anwendung

- Patch trocken geprüft:
  - `git diff --binary 6cd7bc2a..a3bd56e65e6adfd9f12994ddc1a523a0be784bf3 | git apply --check --ignore-space-change --ignore-whitespace`
- Patch angewendet:
  - `git diff --binary 6cd7bc2a..a3bd56e65e6adfd9f12994ddc1a523a0be784bf3 | git apply --ignore-space-change --ignore-whitespace`
- Angewendete Range:
  - `6cd7bc2ab49d88e95ff58b3300d232ace048fc37..a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
- Ergebnisumfang: 13 Dateien, 209 Insertions, 25 Deletions

### Inhalt

- Stat-Healing durch Doctoring übernommen:
  - `enable_stat_healing` in `assets/externalized/game.json`, Schema und Policy
  - Savegame-Version-Bump von `102` auf `103`
  - persistierte Damage-Felder für Agility, Dexterity, Strength und Wisdom
  - Roundtrip-Test für neue Damage-Felder angepasst
- Mapscreen-Keyring-Popup wird beim Verlassen des Mapscreens geschlossen.
- Down-Arrow-Rendering in `src/game/Tactical/Interface.cc` auf Down-Koordinaten korrigiert.
- Suppression-Werte externalisiert:
  - `suppression_fire_modifier`
  - `suppression_fire_reaction_threshold`

### Port-spezifische Prüfung

- JA2-Reborn-Scroll-Guard in `PrintAboveGuy()` bleibt erhalten:
  - `if (g_scroll_inertia || gfScrollPending) return;`
- JA2-Reborn-`CheatSystem`-Hooks in `src/game/Tactical/Weapons.cc` bleiben erhalten.
- Android-App-Version bleibt unverändert: `version` enthält weiterhin `1.0.5`.
- Upstream-Format in `assets/externalized/game.json` wurde übernommen; keine zusätzliche Formatierung oder Bereinigung durchgeführt.

### Auffälligkeiten

- `git apply` meldete vier trailing-whitespace-Warnungen aus dem Upstream-Patch.
- Diese vier neu eingeführten Whitespace-Stellen wurden minimal bereinigt:
  - `rust/stracciatella/src/schemas/yaml/game.schema.yaml`
  - `src/game/Strategic/Assignments.cc`
  - `src/game/Strategic/MapScreen.cc`
- `git diff --check` meldet danach keine Fehler mehr; nur Windows-CRLF-Warnungen für betroffene Dateien.

### Zweite Prüfung

- `git diff --check`: keine Whitespace-Fehler, nur CRLF-Warnungen.
- `rg -n "^(<<<<<<<|=======|>>>>>>>)" --glob "!dependencies/**" --glob "!.git/**" .`: keine Treffer.
- Geprüfte Schwerpunktdateien:
  - `src/game/Tactical/Interface.cc`
  - `src/game/Tactical/Weapons.cc`
  - `src/game/GameVersion.h`
  - `version`
- Diffstat erneut geprüft: 13 Dateien, 209 Insertions, 25 Deletions.

## Phase 2 - Build- und Testvalidierung

Datum: 2026-06-24

### Ausgangspunkt

- Aktiver Branch: `experimental`
- Phase-1-Commit vor Validierung: `a54aa1320`
- Arbeitsbaum vor Phase 2: sauber (`git status --short --branch` -> `## experimental`)
- Android-App-Version: `1.0.5`

### Tests und Builds

- Root:
  - `git diff --check`: erfolgreich
- Rust:
  - `cargo` war nicht im globalen `PATH`.
  - Gefundene Toolchain: lokale Rustup-Toolchain.
  - `cargo --version`: `cargo 1.95.0 (f2d3ce0bd 2026-03-21)`
  - Erster Direktaufruf ohne Toolchain-`PATH` scheiterte, weil Build-Scripts `rustc` nicht fanden.
  - Mit temporär ergänztem Toolchain-`PATH` ausgeführt:
    - `cargo test`
  - Ergebnis nach Test-Fix: erfolgreich
- Android:
  - `.\gradlew.bat :app:testDebugUnitTest`: erfolgreich (`BUILD SUCCESSFUL in 2s`)
  - `.\gradlew.bat :app:externalNativeBuildDebug`: erfolgreich (`BUILD SUCCESSFUL in 9m 36s`)
  - `.\gradlew.bat :app:assembleRelease --rerun-tasks`: erfolgreich (`BUILD SUCCESSFUL in 12m 13s`)

### Fix während Phase 2

- `rust/stracciatella_c_api/src/c/config.rs` angepasst:
  - Der Test `write_engine_options_should_write_a_pretty_json_file` erwartete noch `scaling: PERFECT`.
  - Der aktuelle Rust-Default und Android-Default sind `NEAR_PERFECT`.
  - Die erwartete JSON-Ausgabe wurde auf `NEAR_PERFECT` aktualisiert.

### Manuelle Review

- Savegame-Kompatibilität:
  - Neue Stat-Damage-Felder werden beim Laden von Savegames vor Version `103` mit `0` initialisiert.
  - Savegame-Version bleibt nativ auf `103`; Android-App-Version bleibt `1.0.5`.
- Config-Defaults:
  - `enable_stat_healing` defaultet auf `false`.
  - `suppression_fire_modifier` defaultet auf `6`.
  - `suppression_fire_reaction_threshold` defaultet auf `130`.
  - Damit bleibt das Vanilla-Balancing ohne Config-Anpassung erhalten.
- JA2-Reborn-Portänderungen:
  - Touch-Overlay-Code ist weiterhin vorhanden.
  - Widescreen/TacticalScaling-Code ist weiterhin vorhanden.
  - `CheatSystem`-Hooks sind weiterhin vorhanden.
  - Scroll-Guard in `PrintAboveGuy()` ist weiterhin vorhanden.

### Artefakte

- Nach Gradle-Builds blieb `git status --short --branch` sauber.
- Release-APK wurde lokal erzeugt:
  - `android/app/build/outputs/apk/release/app-release.apk`
- Gradle-Problems-Report wurde lokal erzeugt:
  - `android/build/reports/problems/problems-report.html`
- Diese Build-Artefakte sind nicht getrackt.

### Zweite Prüfung

- Testergebnisse gegen Log geprüft.
- `git status --short --branch`: nur geplante Phase-2-Dokumentation und der Rust-Test-Fix nach Log-Aktualisierung.
- `git diff --check`: keine Fehler.
- Keine versehentlich getrackten Build-Artefakte festgestellt.
- Manuelle Gameplay-Szenarien mit alten Saves wurden nicht auf einem Gerät geladen; die Kompatibilität wurde per Code-Review und Build/Test validiert.

## Phase 3 - Abschlussreview und Merge-Vorbereitung

Datum: 2026-06-24

### Ausgangspunkt

- Aktiver Branch: `experimental`
- Phase-2-Commit vor Abschlussreview: `1af7d1559`
- Arbeitsbaum vor Phase 3: sauber (`git status --short --branch` -> `## experimental`)
- Merge nach `main`: nicht ausgeführt.
- Merge-Sperre: `experimental` darf erst nach manuellem Test und ausdrücklicher Merge-Freigabe durch den Nutzer nach `main` gemerged werden.

### Review

- Reviewte Integrationscommits:
  - `749a27479` P0: Prepare upstream integration
  - `a54aa1320` P1: Apply Stracciatella upstream sync
  - `1af7d1559` P2: Validate upstream integration
- Gesamtdiff seit Phase 0 geprüft:
  - 16 Dateien
  - 348 Insertions
  - 30 Deletions
- Geänderte Bereiche entsprechen dem erwarteten Sync-, Test- und Dokumentationsumfang.
- Kein Merge oder Rebase nach `main` wurde durchgeführt.

### Dokumentation

- `CHANGELOG.md` um einen `Unreleased`-Eintrag für den Upstream-Sync ergänzt.
- `README.md`-Upstream-Abschnitt aktualisiert:
  - Upstream-Basis: `6cd7bc2ab49d88e95ff58b3300d232ace048fc37`
  - letzter synchronisierter Upstream-Commit: `a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
  - `experimental` bleibt bis manuellem Test und Freigabe der Merge-Vorbereitungsbranch.
- Die verbindliche Merge-Sperre wurde im damaligen Plan festgehalten und ist jetzt in diesem Log archiviert.

### Finaler Merge-Hinweis

- Vorbereitete Range:
  - `6cd7bc2ab49d88e95ff58b3300d232ace048fc37..a3bd56e65e6adfd9f12994ddc1a523a0be784bf3`
- Validierte Builds und Tests:
  - `git diff --check`
  - `cargo test`
  - `.\gradlew.bat :app:testDebugUnitTest`
  - `.\gradlew.bat :app:externalNativeBuildDebug`
  - `.\gradlew.bat :app:assembleRelease --rerun-tasks`
- Bekannte Risiken zum Zeitpunkt von Phase 3:
  - Manuelle Gameplay-Tests auf Android-Hardware standen noch aus.
  - Alte Savegames vor Version `103` wurden per Code-Review geprüft, aber nicht manuell geladen.
  - Release-APK wurde gebaut, aber im Rahmen dieser Phase nicht auf einem Gerät installiert.
  - Der spätere Merge nach `main` durfte erst nach Nutzerfreigabe erfolgen.

### Zweite Prüfung

- `git status --short --branch` geprüft.
- `git log --oneline --decorate --max-count=12` geprüft.
- Plan, Log, Changelog und README final geprüft.

## Nachlauf - Test und Planablage

Datum: 2026-06-25

- Release-APK für manuellen Test in den lokalen Übergabeordner kopiert:
  - `JA2-Reborn-1.0.5-upstream-sync-6648d7c91.apk`
  - SHA-256: `EF431E2BDEAB5A4D49FDC782D60742D63697124C1AB481AF2EE8F65DE606CE40`
- Nutzerfeedback nach manuellem Test: "scheint alles zu funktionieren".
- Trotz erfolgreichem Test wurde kein Merge nach `main` ausgeführt.
- Der Merge nach `main` bleibt bis zu einer ausdrücklichen Merge-Freigabe blockiert.
- `JA2Integration.md` wurde in diesen Log überführt und entfernt.
