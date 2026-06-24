# JA2 Reborn — Auto-Update-Checker Implementierungs-Log

Branch: `experimental` | Stand: 2026-06-24

---

## P1 — Manifest, FileProvider, Strings (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P1.1 | AndroidManifest.xml: INTERNET, ACCESS_NETWORK_STATE, REQUEST_INSTALL_PACKAGES | done |
| P1.2 | AndroidManifest.xml: FileProvider innerhalb `<application>` | done |
| P1.3 | res/xml/file_paths.xml erstellen | done |
| P1.4 | values/strings.xml ergänzen (17 Keys) | done |
| P1.5 | values-de/strings.xml ergänzen (17 Keys) | done |
| P1.6 | Phase Review: Manifest-Merge und Resource-Namen | done |
| P1.7 | compileDebugKotlin erfolgreich | done |
| P1.8 | Commit: 68ac76f | done |

## P2 — UpdateChecker.kt (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P2.1 | GitHubRelease/GitHubAsset Datenklassen mit @SerialName | done |
| P2.2 | Tolerante JSON-Konfiguration (ignoreUnknownKeys) | done |
| P2.3 | fetchLatestRelease() mit Headers, Timeouts, Fehlerbehandlung | done |
| P2.4 | SemVer Parser + isNewerVersion(), fail closed | done |
| P2.5 | selectApkAsset() mit Namens-/Debug-/Size-Regeln | done |
| P2.6 | downloadApk() mit .part, Grössenprüfung, atomic rename | done |
| P2.7 | Unit-Tests: 15 Tests (SemVer, JSON, Asset-Selektion) | done |
| P2.8 | Phase Review + testDebugUnitTest (65 tests, all pass) | done |
| P2.9 | Commit: 64abb25 | done |
