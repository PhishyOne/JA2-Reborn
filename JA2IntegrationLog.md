# JA2 Stracciatella Integration Log

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
