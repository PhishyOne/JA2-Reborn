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

## P3 — UpdateApkVerifier.kt + Installer-Intent (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P3.1 | UpdateApkVerifier.kt: SHA256, PackageInfo aus APK lesen | done |
| P3.2 | Package-Name und VersionCode gegen installierte App prüfen | done |
| P3.3 | Signaturvergleich API 24-27 und API >= 28 | done |
| P3.4 | FileProvider URI + Installer Intent (createInstallerIntent) | done |
| P3.5 | canRequestPackageInstalls() + Settings-Flow | done |
| P3.6 | Pending APK nach Settings-Return in onResume() | done |
| P3.7 | Unit-Tests: 4 Tests (SHA256, VerificationResult) | done |
| P3.8 | Phase Review + testDebugUnitTest (72 tests, all pass) | done |
| P3.9 | Commit: c4edefa | done |

## P4 — LauncherActivity Integration (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P4.1 | UpdatePrefs.kt: Opt-in, Rate-Limit (24h), Version-Helpers | done |
| P4.2 | maybePromptAutoUpdateOptIn() in onCreate() | done |
| P4.3 | showOptInDialog(): Aktivieren/Nein danke, kein Settings-Sprung | done |
| P4.4 | performUpdateCheck(force): Netzwerk, Rate-Limit, Background-Thread | done |
| P4.5 | showUpdateAvailableDialog(): Version, Größe, Release Notes | done |
| P4.6 | startUpdateDownload(): Background-Download mit ProgressBar | done |
| P4.7 | showInstallReadyDialog(): Install-Button nach Verifikation | done |
| P4.8 | tryInstallApk(): Permission-Check, Settings-Flow, Pending-APK | done |
| P4.9 | showInstallPermissionDialog() + showUpdateErrorDialog() | done |
| P4.10 | Lifecycle-Safety: isActivityAlive/runOnUiIfAlive | done |
| P4.11 | Phase Review + compileDebugKotlin + 72 Tests | done |
| P4.12 | Commit: b4ba3fb | done |

## P5 — Build, Tests, Device-/Staging-Tests (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P5.1 | testDebugUnitTest (72 tests, all pass) | done |
| P5.2 | assembleRelease (BUILD SUCCESSFUL) | done |
| P5.3 | apksigner verify: CN=JA2 Stracciatella bestätigt | done |
| P5.4 | aapt dump badging: com.ja2.reborn, versionCode=1000005, versionName=1.0.5 | done |
| P5.5 | Device-Test: Opt-in Ja/Nein, no-update, offline | done |
| P5.6 | Staging A: 1.0.3 → GitHub v1.0.4 Update-Flow (Download, Verify, Install) | done |
| P5.7 | Staging B: 1.0.5 → GitHub v1.0.4 kein falsches Update | done |
| P5.8 | Staging C: Offline/Fehlerpfade ohne Dialogspam oder Crash | done |
| P5.9 | CHANGELOG.md, JA2AutoUpdate.md, Implementierungs-Log | done |
| P5.10 | Commit: c9bb66f | done |

## P6 — Manual Update Check Button (2026-06-24)

| Schritt | Aktion | Status |
|---------|--------|--------|
| P6.1 | ic_download.xml: Download-Pfeil Vector Drawable (32x20dp, Accent #FFC17A) | done |
| P6.2 | activity_launcher.xml: updateCheck ImageView links vor flagDE/flagGB | done |
| P6.3 | strings.xml (EN): update_check_cd, auto_update_up_to_date_title/message, auto_update_no_network | done |
| P6.4 | strings.xml (DE): update_check_cd, auto_update_up_to_date_title/message, auto_update_no_network | done |
| P6.5 | LauncherActivity.kt: updateCheck.setOnClickListener → performUpdateCheck(force=true) | done |
| P6.6 | performUpdateCheck(): UI-Feedback für alle Pfade (no-network, up-to-date, draft-only, error) | done |
| P6.7 | showUpdateUpToDateDialog(): Titel + Version + OK-Button | done |
| P6.8 | showUpdateInfoDialog(): generisches Info-Panel (delegiert an showUpdateErrorDialog) | done |
| P6.9 | Versions-Test: 1.0.3 APK installiert → Button erkennt v1.0.4 auf GitHub → Update-Flow erfolgreich | done |
| P6.10 | Versions-Test: 1.0.5 APK installiert → Button zeigt "Up to date" / "Aktuell" | done |
| P6.11 | Vollständigkeits-Check: alle 21 Auto-Update-Strings in EN + DE vorhanden | done |
| P6.12 | assembleRelease + GDrive Upload | done |

## Codex Reviews (2026-06-24)

| Review | Ergebnis |
|--------|---------|
| Erste Review | Fixliste: Public-Sanity, Changelog-Duplikat, Asset-URL-Prüfung, Asset-Version, GitHub-Digest, Permission-Fallback, Verifier-Tests |
| Fix-Commit | bf4cbf8: Alle 7 Punkte behoben |
| Re-Review | Neue Fixliste: Settings-Roundtrip ohne Permission, Installer-Intent-Nullpfad |
| Fix-Commit | f6a96d6: Beide Punkte behoben |
| Finale Freigabe | 624324a: Freigabe für Device-/Staging-Tests |

## Zusammenfassung

- **5 Phasen (P0-P5)** vollständig abgeschlossen
- **72 Unit-Tests**, alle bestanden
- **5 Commits** auf experimental (68ac76f → c9bb66f)
- **3 Codex-Review-Runden** mit Freigabe
- **Device-Tests** auf Android-Hardware: positiver Update-Flow, negativer Check, Offline-Pfade
- **Auto-Update-Checker ist release-ready** für v1.0.5+
- Plan-Dokument (JA2AutoUpdate.md) nach Abschluss gelöscht
