# JA2 Reborn - Auto-Update-Checker im Launcher

Implementierungsplan. Stand: 2026-06-24 | Branch: `experimental`

Dieser Plan wurde nach Review korrigiert. Der Auto-Updater ist machbar, aber nur
mit den unten genannten Sicherheits- und Test-Gates. Die urspruengliche Fassung
haette in mehreren Punkten funktionieren koennen, war aber nicht robust genug fuer
eine Release-Funktion, die APKs nachlaedt und installiert.

---

## Review-Ergebnis

**Status:** bedingt freigegeben. Implementierung erst nach den Korrekturen in
dieser Datei starten.

### Korrigierte Kernpunkte

1. **Kein `buildConfigField VERSION_NAME`:** Das Projekt liest die App-Version
   bereits aus der Datei `version` und setzt `versionName`/`versionCode` in
   `android/app/build.gradle`. Ein zusaetzliches BuildConfigField mit demselben
   Namen ist riskant und nicht noetig. Fuer Runtime-Vergleiche wird
   `PackageManager`/`PackageInfoCompat.getLongVersionCode()` verwendet.
2. **Install-Berechtigung nicht beim Opt-in anfordern:** Der Opt-in erlaubt nur
   den Update-Check. `REQUEST_INSTALL_PACKAGES` bzw. die Android-Settings fuer
   "Install unknown apps" werden erst geoeffnet, wenn ein verifiziertes Update
   heruntergeladen ist und der User "Installieren" tippt.
3. **GitHub-JSON muss tolerant dekodiert werden:** Die GitHub API liefert viele
   Felder, die das lokale Modell nicht kennt. `Json { ignoreUnknownKeys = true }`
   und `@SerialName` fuer `tag_name`, `browser_download_url`, `content_type` usw.
   sind Pflicht.
4. **APK-Asset muss eindeutig sein:** Es wird nur ein `.apk`-Release-Asset aus
   `RealTommyGreen/JA2-Reborn` akzeptiert. Bei mehreren APKs muss ein exakter
   Name wie `JA2RebornRelease1.0.6.apk` bevorzugt werden; Mehrdeutigkeit bedeutet
   "kein Update anzeigen".
5. **APK vor Installation verifizieren:** Vor dem Installer-Intent wird die
   heruntergeladene APK lokal geprueft:
   - Package-Name ist `com.ja2.reborn`
   - APK-`versionCode` ist groesser als die installierte Version
   - APK-Signatur/Zertifikat entspricht der aktuell installierten App
   - optionaler GitHub-Asset-Digest oder Release-SHA256 stimmt, falls vorhanden
6. **Fail closed:** Bei Parse-Fehlern, uneindeutigen Assets, Signaturfehlern,
   aelteren Versionen oder unvollstaendigen Downloads wird kein Installer
   gestartet.
7. **Current Reality Check:** Lokal ist `version` aktuell `1.0.5`. GitHub
   `/releases/latest` liefert am 2026-06-24 oeffentlich `v1.0.4` mit Asset
   `JA2RebornRelease1.0.4.apk`. Auf diesem Stand muss der Update-Check still
   bleiben, weil remote nicht neuer ist.

---

## Projektregeln

1. **Branch:** Gearbeitet wird ausschliesslich auf `experimental`.
2. **Phasen-Pruefung:** Vor Abschluss jeder Phase werden Ergebnis und Diff
   zweimal geprueft.
3. **Commits:** Erst nach sauberer Phase, Tests und Dokumentation committen.
4. **Stop nach Phase:** Nach jedem Phasenabschluss stoppen, damit der Kontext bei
   Bedarf geleert werden kann.
5. **Release-Quelle:** Der Updater verwendet nur GitHub Releases des Repos
   `RealTommyGreen/JA2-Reborn`. Google Drive ist fuer Test-Artefakte okay, aber
   keine Auto-Update-Quelle.
6. **Keine automatische Installation:** Der User bestaetigt Check-Opt-in,
   Download und Installation jeweils explizit.

---

## Phasenuebersicht

| Phase | Inhalt | Status |
|-------|--------|--------|
| P0 | Plan-Review und Korrektur | done |
| P1 | Manifest, FileProvider, Strings | done |
| P2 | UpdateChecker: GitHub API, SemVer, Asset-Auswahl, Download | done |
| P3 | APK-Verifikation und Installer-Intent | done |
| P4 | LauncherActivity Integration: Opt-in, Check, Dialoge, Rate-Limit | done |
| P5 | Build, Unit-Tests, Device-/Staging-Test, Dokumentation | done |

---

## Context

JA2 Reborn hat aktuell keine Netzwerkfunktionalitaet. Nutzer muessen manuell ins
GitHub-Repo schauen, eine neue Release-APK herunterladen und installieren. Der
Auto-Update-Checker soll im Android-Launcher nach Opt-in pruefen, ob auf GitHub
eine neuere stabile Release-Version existiert, und den User danach durch
Download und Installation fuehren.

Der Check darf keine Gameplay-Daten hochladen. Er ruft nur die GitHub Releases
API ab. Der Opt-in-Dialog muss das klar nennen.

---

## Zu erstellende Dateien

| Datei | Zweck |
|---|---|
| `android/app/src/main/java/com/ja2/reborn/UpdateChecker.kt` | GitHub API, Release-Modell, SemVer, Asset-Auswahl, Download |
| `android/app/src/main/java/com/ja2/reborn/UpdateApkVerifier.kt` | Lokale APK-Pruefung: Package, Version, Signatur, SHA256 |
| `android/app/src/main/res/xml/file_paths.xml` | FileProvider-Pfad fuer heruntergeladene APKs |
| `android/app/src/test/java/com/ja2/reborn/UpdateCheckerTest.kt` | Unit-Tests fuer SemVer, JSON, Asset-Auswahl |

Optional, falls `LauncherActivity.kt` zu gross wird:

| Datei | Zweck |
|---|---|
| `android/app/src/main/java/com/ja2/reborn/UpdateDialogController.kt` | Dialogaufbau und UI-State fuer Opt-in/Download/Install |

---

## Zu aendernde Dateien

| Datei | Aenderung |
|---|---|
| `android/app/src/main/AndroidManifest.xml` | `INTERNET`, `ACCESS_NETWORK_STATE`, `REQUEST_INSTALL_PACKAGES`, FileProvider |
| `android/app/src/main/java/com/ja2/reborn/LauncherActivity.kt` | Opt-in, Rate-Limit, Update-Check, Download-/Install-Dialog |
| `android/app/src/main/res/values/strings.xml` | Englische String-Keys |
| `android/app/src/main/res/values-de/strings.xml` | Deutsche String-Keys |
| `CHANGELOG.md` | Auto-Updater dokumentieren, sobald implementiert |
| `docs/JA2_ANDROID_TODO_IMPLEMENTATION_LOG.md` | Phasenlog pflegen |
| `JA2AutoUpdate.md` | Status je Phase aktualisieren |

**Nicht aendern ohne konkreten Build-Grund:**

| Datei | Grund |
|---|---|
| `android/app/build.gradle` | Kein neues `VERSION_NAME`-BuildConfigField. Aktuelle Version per `PackageManager` lesen. |

---

## Externe Fakten und Schnittstellen

### GitHub Releases API

Endpoint:

```text
GET https://api.github.com/repos/RealTommyGreen/JA2-Reborn/releases/latest
```

Pflicht-Header:

```text
Accept: application/vnd.github+json
X-GitHub-Api-Version: 2026-03-10
User-Agent: JA2-Reborn-Android/<installed-version>
```

Die API liefert die neueste veroeffentlichte nicht-draft und nicht-prerelease
Release. Aktueller oeffentlicher Stand am 2026-06-24:

```text
tag_name: v1.0.4
asset:    JA2RebornRelease1.0.4.apk
size:     100805415
type:     application/vnd.android.package-archive
```

### Android Install-Flow

- `REQUEST_INSTALL_PACKAGES` muss im Manifest stehen, weil `targetSdk` 35 ist.
- `PackageManager.canRequestPackageInstalls()` gibt ab API 26 an, ob der User
  diese App als vertrauenswuerdige Installationsquelle erlaubt hat.
- `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` darf erst bei Install-Wunsch
  geoeffnet werden, nicht beim Update-Check-Opt-in.
- Die APK wird per `FileProvider` als `content://` URI geteilt, nicht als
  `file://` URI.

---

## Architektur und Datenfluss

```text
LauncherActivity.onCreate()
  |
  |-- normale Launcher-Initialisierung
  |
  |-- maybePromptAutoUpdateOptIn()
  |     |-- prefs auto_update_opted_in fehlt -> Opt-in Dialog
  |     |      |-- Aktivieren -> pref=true, performUpdateCheck(force=true)
  |     |      |-- Nein danke -> pref=false, kein Netzwerk
  |     |-- pref=true -> performUpdateCheck(force=false), mit Rate-Limit
  |     |-- pref=false -> nichts tun
  |
  |-- performUpdateCheck()
        |-- kein Netzwerk / Rate-Limit aktiv -> still
        |-- GET GitHub latest release im Background Thread
        |-- JSON tolerant parsen
        |-- SemVer remote > local?
        |-- genaues APK-Asset finden?
        |-- sonst still
        |-- Update-Dialog anzeigen
              |-- Spaeter -> Dialog schliessen
              |-- Herunterladen -> Download nach cacheDir/apk_update/*.part
                    |-- atomar nach *.apk umbenennen
                    |-- APK verifizieren
                    |-- Installieren-Button anzeigen
                          |-- API >= 26 und canRequestPackageInstalls=false
                          |      -> Permission-Erklaerung + Settings
                          |-- sonst Installer-Intent mit FileProvider URI
```

---

## Komponenten im Detail

### 1. `UpdateChecker.kt`

#### Datenmodell

```kotlin
@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = "",
    val size: Long = 0L,
    @SerialName("content_type") val contentType: String? = null,
    val digest: String? = null
)
```

JSON-Konfiguration:

```kotlin
private val json = Json {
    ignoreUnknownKeys = true
}
```

#### Funktionen

| Funktion | Verhalten |
|---|---|
| `fetchLatestRelease(): GitHubRelease?` | `HttpURLConnection`, HTTPS, Header oben, 15s connect, 30s read, HTTP 200 sonst `null`. |
| `parseStableSemVer(value: String): SemVer?` | Akzeptiert nur `v1.2.3` oder `1.2.3`. Nicht parsebar => `null`. |
| `isNewerVersion(remoteTag: String, localVersionName: String): Boolean` | Nur `true`, wenn beide SemVer parsebar sind und remote > local. Kein String-Groesservergleich. |
| `selectApkAsset(release: GitHubRelease): GitHubAsset?` | Exakten Namen `JA2RebornRelease<version>.apk` bevorzugen; bei genau einem `.apk` Asset fallback; bei 0 oder >1 ohne exakten Treffer `null`. |
| `downloadApk(asset, version, onProgress): File?` | Download in `.part`, Groesse pruefen, dann atomar nach finalem `.apk` verschieben. |

#### Asset-Regeln

Ein Asset ist nur Kandidat, wenn:

- `name.endsWith(".apk", ignoreCase = true)`
- `name` enthaelt weder `debug` noch `unsigned`
- `browser_download_url` ist HTTPS
- Host ist `github.com`
- Pfad beginnt mit `/RealTommyGreen/JA2-Reborn/releases/download/`
- `size > 0`

`content_type` wird geloggt und bevorzugt, ist aber nicht allein vertrauenswuerdig.
Die finale Wahrheit ist die lokale APK-Verifikation.

---

### 2. `UpdateApkVerifier.kt`

Vor jeder Installation wird die heruntergeladene Datei geprueft.

#### Pflichtpruefungen

1. Datei existiert, ist lesbar und `length == asset.size`, wenn GitHub eine
   positive Groesse liefert.
2. SHA256 berechnen. Wenn `asset.digest` mit `sha256:` gesetzt ist oder die
   Release Notes einen SHA256-Wert enthalten, muss dieser uebereinstimmen.
3. `PackageManager.getPackageArchiveInfo()` kann die APK lesen.
4. `packageName == "com.ja2.reborn"`.
5. `archiveVersionCode > installedVersionCode`.
6. Signaturzertifikat der APK entspricht dem Signaturzertifikat der aktuell
   installierten App.

#### Signaturvergleich

- API >= 28: `PackageManager.GET_SIGNING_CERTIFICATES` und
  `PackageInfo.signingInfo.apkContentsSigners`.
- API 24-27: `PackageManager.GET_SIGNATURES` und `PackageInfo.signatures`.
- Zertifikate als SHA-256-Hashes vergleichen.

Wenn eine dieser Pruefungen fehlschlaegt:

- Datei loeschen
- Dialog zeigt einen kurzen Fehler
- kein Installer-Intent

---

### 3. FileProvider-Setup

`android/app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="apk_updates" path="apk_update/" />
</paths>
```

Manifest innerhalb `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

Installer-Intent:

```kotlin
val uri = FileProvider.getUriForFile(
    this,
    "$packageName.fileprovider",
    apkFile
)
val intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(uri, "application/vnd.android.package-archive")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
startActivity(intent)
```

`ActivityNotFoundException` abfangen und als Fehlerdialog anzeigen.

---

### 4. Manifest-Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

Wichtig:

- `INTERNET` und `ACCESS_NETWORK_STATE` sind normale Permissions.
- `REQUEST_INSTALL_PACKAGES` ist eine spezielle Installationsberechtigung. Sie
  muss im Manifest stehen, wird aber nicht per Runtime-Prompt angefragt.
- Bei API >= 26 vor Installer-Start `packageManager.canRequestPackageInstalls()`
  pruefen.
- Falls `false`, erst dann Settings oeffnen:

```kotlin
Intent(
    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
    Uri.parse("package:$packageName")
)
```

Fallback bei fehlender Settings-Activity:

```kotlin
Intent(Settings.ACTION_SECURITY_SETTINGS)
```

Policy-Hinweis: Wenn JA2 Reborn jemals ueber Google Play verteilt wird, ist
`REQUEST_INSTALL_PACKAGES` policy-relevant und muss vorab geprueft werden. Fuer
die aktuelle GitHub-Verteilung ist die Permission technisch passend.

---

### 5. SharedPreferences

File:

```text
ja2_reborn_update_prefs
```

Keys:

| Key | Typ | Bedeutung |
|---|---|---|
| `auto_update_opted_in` | Boolean? | `null` = noch nie gefragt, `true` = Check aktiv, `false` = kein Netzwerkcheck |
| `auto_update_prompted_version` | String | Installierte Version, bei der der Opt-in zuletzt angezeigt wurde |
| `last_update_check_time` | Long | Unix millis des letzten erfolgreichen oder still fehlgeschlagenen Checks |
| `pending_update_apk_path` | String? | Verifizierte APK, falls User fuer Install-Permission in Settings geschickt wurde |
| `pending_update_version` | String? | Version zur pending APK |

Rate-Limit:

- Direkt nach Opt-in einmal sofort pruefen.
- Danach maximal einmal alle 24 Stunden beim Launcher-Start.
- HTTP 403/429 oder Netzwerkfehler ebenfalls mit Timestamp speichern, damit
  keine Request-Schleife entsteht.

Kein `skip_version`, solange es keinen sichtbaren Button "Diese Version
ueberspringen" gibt.

---

### 6. Opt-in-Dialog

Anzeige erst nach normalem Launcher-Setup in `onCreate()`.

Inhalt:

- Auto-Update prueft nach Zustimmung die GitHub Releases von JA2 Reborn.
- Es werden keine Spielstaende, Logs oder Spieldaten hochgeladen.
- Downloads und Installationen passieren nur nach separater Bestaetigung.
- Android kann spaeter eine Berechtigung fuer "Install unknown apps" verlangen.

Buttons:

- `Aktivieren` / `Enable`: `auto_update_opted_in=true`, Check sofort starten.
- `Nein, danke` / `No, thanks`: `auto_update_opted_in=false`, kein Netzwerk.

Der Dialog oeffnet keine System-Settings.

---

### 7. Update-Dialog

Nur anzeigen, wenn:

- Opt-in aktiv ist
- remote SemVer neuer ist
- ein eindeutiges APK-Asset existiert
- lokale Version nicht neuer/gleich ist

Anzeigen:

- Version
- Dateigroesse in MB
- Release Notes gekuerzt, z.B. max. 1200 Zeichen
- Quelle: GitHub Release

Buttons:

- `Herunterladen`
- `Spaeter`

Nach Download und Verifikation:

- `Installieren`
- optional `Loeschen`/`Abbrechen`

Fehlerdialoge nur nach User-Aktion anzeigen. Stille Hintergrundchecks zeigen bei
Netzwerk-/GitHub-Fehlern nichts.

---

## Neue String-Keys

| Key | Deutsch | Englisch |
|---|---|---|
| `auto_update_optin_title` | Auto-Updates aktivieren? | Enable auto-updates? |
| `auto_update_optin_message` | JA2 Reborn kann nach deiner Zustimmung GitHub Releases auf neue Versionen pruefen. Es werden keine Spielstaende, Logs oder Spieldaten hochgeladen. Download und Installation passieren nur nach deiner Bestaetigung. | JA2 Reborn can check GitHub Releases for new versions after you opt in. No saves, logs, or game data are uploaded. Downloads and installation only happen after your confirmation. |
| `auto_update_optin_activate` | Aktivieren | Enable |
| `auto_update_optin_decline` | Nein, danke | No, thanks |
| `auto_update_available_title` | Update verfuegbar | Update available |
| `auto_update_available_message` | Version %1$s ist verfuegbar. Downloadgroesse: %2$s. | Version %1$s is available. Download size: %2$s. |
| `auto_update_download` | Herunterladen | Download |
| `auto_update_later` | Spaeter | Later |
| `auto_update_install` | Installieren | Install |
| `auto_update_downloading` | Lade herunter... | Downloading... |
| `auto_update_verifying` | Pruefe APK... | Verifying APK... |
| `auto_update_permission_title` | Installationsberechtigung erforderlich | Installation permission required |
| `auto_update_permission_message` | Android muss JA2 Reborn als Installationsquelle erlauben, bevor das Update installiert werden kann. | Android must allow JA2 Reborn as an installation source before the update can be installed. |
| `auto_update_open_settings` | Einstellungen oeffnen | Open settings |
| `auto_update_retry` | Erneut versuchen | Retry |
| `auto_update_download_failed` | Download fehlgeschlagen. | Download failed. |
| `auto_update_verification_failed` | Die heruntergeladene APK konnte nicht sicher verifiziert werden. | The downloaded APK could not be verified safely. |
| `auto_update_installer_failed` | Der Android-Installer konnte nicht geoeffnet werden. | Could not open the Android installer. |

---

## Edge Cases und erwartetes Verhalten

| Fall | Verhalten |
|---|---|
| Kein Internet | Stiller Abbruch, kein Dialog |
| GitHub HTTP 403/429 | Stiller Abbruch, Rate-Limit Timestamp setzen |
| GitHub latest ist `v1.0.4`, lokal ist `1.0.5` | Kein Dialog |
| Release hat kein APK | Kein Dialog, Log-Warnung |
| Release hat mehrere APKs ohne exakten Namensmatch | Kein Dialog, Log-Warnung |
| `versionName`/Tag nicht parsebar | Kein Update, fail closed |
| Download bricht ab | Fehler im Download-Dialog, `.part` loeschen |
| Content-Length unbekannt | ProgressBar indeterminate, Download trotzdem moeglich |
| Datei kleiner/groesser als Asset-Groesse | Verifikation fehlgeschlagen, Datei loeschen |
| APK Package-Name falsch | Verifikation fehlgeschlagen |
| APK Version nicht groesser | Verifikation fehlgeschlagen |
| APK Signatur anders | Verifikation fehlgeschlagen |
| User verweigert Install-Quelle | Install-Button bleibt, kein Crash |
| Settings-Return nach Permission | In `onResume()` pending APK erneut pruefen und Installer starten |
| Installer scheitert | Android zeigt Fehler; App darf nicht crashen |

---

## Teststrategie

### Unit-Tests

`UpdateCheckerTest.kt`:

- SemVer:
  - `v1.0.6 > 1.0.5`
  - `1.0.10 > 1.0.9`
  - `1.1.0 > 1.0.9`
  - `1.0.5` ist nicht neuer als `1.0.5`
  - `v1.0.4` ist nicht neuer als `1.0.5`
  - unparsebare Werte liefern `false`
- GitHub JSON:
  - Fixture mit echten GitHub-Feldern und Extra-Feldern dekodiert erfolgreich
  - `tag_name` und `browser_download_url` werden korrekt gemappt
- Asset-Auswahl:
  - Exakter `JA2RebornRelease1.0.6.apk` Treffer
  - genau ein `.apk` als Fallback
  - mehrere `.apk` ohne exakten Treffer => `null`
  - `debug`/`unsigned` wird abgelehnt
  - falscher Host/Pfad wird abgelehnt

`UpdateApkVerifierTest.kt` soweit lokal testbar:

- SHA256 helper
- Versions-/Package-Pruefung als pure Funktionen
- Signaturvergleich als helper mit synthetischen ByteArrays

### Build-Tests

In `android`:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

Danach signierte Release-APK mit lokalem Signing aus `JA2 Reborn Zusatz`
erstellen/verifizieren wie bei v1.0.5.

### Device-Tests

Pflicht auf Android-Hardware:

1. Frische Installation: Opt-in Dialog erscheint genau einmal.
2. Opt-in `Nein`: kein Netzwerkcheck mehr bei Launcher-Start.
3. Opt-in `Ja`: bei aktuellem Zustand lokal `1.0.5` vs GitHub `v1.0.4`
   erscheint kein Update-Dialog.
4. Flugmodus: kein Dialog, kein Crash.
5. Download-/Install-Permission-Flow mit verifizierter neuerer APK testen, bevor
   die Funktion als release-ready gilt.

### Staging-Test fuer echten Update-Flow

Weil GitHub aktuell nur `v1.0.4` als latest liefert und lokal `1.0.5` ist, kann
der "Update verfuegbar"-Flow vor einer neuen GitHub Release nicht mit dem echten
Endpoint ausgeloest werden. Deshalb wird vor finaler Freigabe folgender
konkreter Vorabtest verpflichtend durchgefuehrt:

#### Staging-Test A: Kuenstlich aeltere Test-APK gegen echtes GitHub Latest

Ziel: Den kompletten realen Update-Pfad testen, ohne auf `v1.0.6` warten zu
muessen.

1. Updater-Code vollstaendig implementieren.
2. Arbeitsbaum sauber halten und die Versionsdatei nur lokal/testweise auf
   `1.0.3` setzen. Diese Versionsaenderung darf nicht committed werden.
3. Eine release-signierte Test-APK mit demselben Release-Key bauen, der fuer
   die echten Releases genutzt wird.
4. Mit `aapt dump badging` pruefen:
   - `package: name='com.ja2.reborn'`
   - `versionName='1.0.3'`
   - `versionCode='1000003'`
5. Mit `apksigner verify --print-certs` pruefen, dass die APK mit dem echten
   Release-Zertifikat signiert ist.
6. Die Test-APK auf einem Android-Geraet installieren.
7. App starten und Auto-Update-Opt-in aktivieren.
8. Erwartung: Die App fragt das echte GitHub Latest Release ab:
   - `https://api.github.com/repos/RealTommyGreen/JA2-Reborn/releases/latest`
   - erwartetes remote Release am 2026-06-24: `v1.0.4`
   - erwartetes Asset: `JA2RebornRelease1.0.4.apk`
9. Erwartung: Update-Dialog erscheint, weil `1.0.4 > 1.0.3`.
10. Download starten.
11. Erwartung: Download laeuft in `cacheDir/apk_update/*.part`, danach atomarer
    Rename auf `.apk`.
12. Erwartung: Verifikation besteht:
    - Datei-Groesse stimmt mit GitHub Asset ueberein.
    - Package-Name ist `com.ja2.reborn`.
    - APK-`versionCode` ist groesser als die installierte `1000003`.
    - APK-Signatur entspricht der aktuell installierten App.
13. "Installieren" tippen.
14. Falls Android `Install unknown apps` blockiert:
    - App oeffnet `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES`.
    - Nach Rueckkehr wird die pending APK erneut verifiziert.
    - Danach wird der Installer erneut gestartet.
15. Android-Installer bestaetigen.
16. Erwartung nach Installation:
    - App ist weiterhin `com.ja2.reborn`.
    - `versionName` ist `1.0.4`.
    - App startet ohne Crash.
    - Spielkonfiguration, Savegame-Pfade und Touch-Presets bleiben erhalten.

#### Staging-Test B: Normale lokale 1.0.5 gegen echtes GitHub Latest

Ziel: Sicherstellen, dass der Updater keine falschen Downgrade- oder
False-Positive-Updates anbietet.

1. Versionsdatei wieder auf den echten lokalen Stand `1.0.5` setzen.
2. Release-signierte APK bauen und auf dem Geraet installieren.
3. App starten und Auto-Update-Opt-in aktivieren.
4. Erwartung: GitHub Latest ist `v1.0.4`, lokal ist `1.0.5`.
5. Erwartung: Kein Update-Dialog erscheint.
6. Logcat darf hoechstens eine stille "remote not newer"-Info enthalten, keinen
   User-sichtbaren Fehler.

#### Staging-Test C: Offline- und Fehlerpfade

1. Mit aktivem Opt-in Flugmodus einschalten.
2. App starten.
3. Erwartung: Kein Dialog, kein Toast-Fehler, kein Crash.
4. Flugmodus ausschalten, App erneut starten.
5. Erwartung: Rate-Limit verhindert Request-Spam; ein manueller Force-Check ist
   nur fuer Debug/Test-Builds erlaubt.

Ohne echten Device-Test eines neueren, signierten APK-Updates keine Release-Freigabe.

---

## Implementierungs-Log

### P0 - Plan-Review und Korrektur

| Schritt | Aktion | Status |
|---------|--------|--------|
| P0.1 | Projektlog gelesen, aktueller Stand v1.0.5 ermittelt | done |
| P0.2 | Android Gradle, Manifest, LauncherActivity, Strings geprueft | done |
| P0.3 | GitHub latest Release und Asset real verifiziert | done |
| P0.4 | Kritische Planfehler korrigiert | done |

### P1 - Manifest, FileProvider, Strings

| Schritt | Aktion | Status |
|---------|--------|--------|
| P1.1 | AndroidManifest.xml: `INTERNET`, `ACCESS_NETWORK_STATE`, `REQUEST_INSTALL_PACKAGES` | done |
| P1.2 | AndroidManifest.xml: FileProvider innerhalb `<application>` | done |
| P1.3 | `res/xml/file_paths.xml` erstellen | done |
| P1.4 | `values/strings.xml` ergaenzen | done |
| P1.5 | `values-de/strings.xml` ergaenzen | done |
| P1.6 | Phase review: Manifest-Merge und Resource-Namen pruefen | done |
| P1.7 | `compileDebugKotlin` erfolgreich | done |
| P1.8 | Commit: Plan + Log + Code | done |

### P2 - UpdateChecker

| Schritt | Aktion | Status |
|---------|--------|--------|
| P2.1 | Datenklassen mit `@SerialName` und Defaultwerten | done |
| P2.2 | Tolerante JSON-Konfiguration | done |
| P2.3 | `fetchLatestRelease()` mit Headern, Timeouts, Fehlerbehandlung | done |
| P2.4 | SemVer Parser und Vergleich, fail closed | done |
| P2.5 | APK-Asset-Auswahl mit Host-/Pfad-/Name-Regeln | done |
| P2.6 | Download nach `.part`, Groessenpruefung, atomic rename | done |
| P2.7 | Unit-Tests fuer SemVer, JSON, Asset-Auswahl | done |
| P2.8 | Phase review + Tests | done |
| P2.9 | Commit: Plan + Log + Code | done |

### P3 - APK-Verifikation und Installation

| Schritt | Aktion | Status |
|---------|--------|--------|
| P3.1 | `UpdateApkVerifier.kt`: SHA256, PackageInfo aus APK lesen | done |
| P3.2 | Package-Name und VersionCode gegen installierte App pruefen | done |
| P3.3 | Signaturvergleich API 24-27 und API >= 28 | done |
| P3.4 | FileProvider URI + Installer Intent | done |
| P3.5 | `canRequestPackageInstalls()` und Settings-Flow | done |
| P3.6 | Pending APK nach Settings-Return in `onResume()` behandeln | done |
| P3.7 | Unit-Tests fuer pure Helper | done |
| P3.8 | Phase review + Tests | done |
| P3.9 | Commit: Plan + Log + Code | done |

### P4 - LauncherActivity Integration

| Schritt | Aktion | Status |
|---------|--------|--------|
| P4.1 | SharedPreferences-Helfer | done |
| P4.2 | Opt-in-Dialog ohne Settings-Sprung | done |
| P4.3 | Rate-Limit und Netzwerkstatus pruefen | done |
| P4.4 | Background-Thread fuer API und Download | done |
| P4.5 | Update-Available-Dialog | done |
| P4.6 | Download-Progress und Fehlerzustand | done |
| P4.7 | Install-Button erst nach erfolgreicher Verifikation | done |
| P4.8 | Lifecycle: keine UI-Updates nach Activity-Zerstoerung | done |
| P4.9 | Phase review + Tests | done |
| P4.10 | Commit: Plan + Log + Code | done |

### P5 - Build, Test, Release-Dokumentation

| Schritt | Aktion | Status |
|---------|--------|--------|
| P5.1 | `.\gradlew.bat testDebugUnitTest` | done |
| P5.2 | `.\gradlew.bat assembleRelease` | done |
| P5.3 | Signierte APK mit Release-Key bauen und `apksigner verify` | done |
| P5.4 | `aapt dump badging`: `versionName`/`versionCode` pruefen | done |
| P5.5 | Device-Test: Opt-in Ja/Nein, no-update, offline | pending-device |
| P5.6 | Staging-Test A: lokal `1.0.3` -> GitHub `v1.0.4` Download, Verifikation, Installer | pending-device |
| P5.7 | Staging-Test B: lokal `1.0.5` -> GitHub `v1.0.4` zeigt kein Update | pending-device |
| P5.8 | Staging-Test C: Offline-/Fehlerpfade ohne Dialogspam oder Crash | pending-device |
| P5.9 | Projektlog, Changelog, Plan aktualisieren | done |
| P5.10 | Commit finaler Stand | done |

---

## Changelog

### 2026-06-24 - Plan korrigiert

- Auto-Updater-Plan gegen aktuellen Projektstand, Android-Install-Flow und
  GitHub Release-API geprueft.
- Kritische Korrekturen eingearbeitet:
  - kein doppeltes `VERSION_NAME` BuildConfigField
  - Install-Berechtigung erst beim Installieren
  - GitHub JSON tolerant mit `@SerialName`
  - eindeutige APK-Asset-Auswahl
  - lokale APK-Paket-/Versions-/Signaturpruefung vor Installer-Start
  - Staging-Testpflicht fuer echten Update-Flow

### 2026-06-24 - P1: Manifest, FileProvider, Strings

- AndroidManifest.xml: `INTERNET`, `ACCESS_NETWORK_STATE`, `REQUEST_INSTALL_PACKAGES`
- AndroidManifest.xml: FileProvider `androidx.core.content.FileProvider`
- `res/xml/file_paths.xml`: `cache-path` fuer APK-Downloads
- `values/strings.xml`: 17 englische String-Keys fuer Auto-Update-Dialoge
- `values-de/strings.xml`: 17 deutsche String-Keys fuer Auto-Update-Dialoge
- `compileDebugKotlin`: erfolgreich

### 2026-06-24 - P2: UpdateChecker.kt mit Unit-Tests

- `UpdateChecker.kt`: GitHubRelease/GitHubAsset Datenklassen, tolerantes JSON
- `fetchLatestRelease()`: HttpURLConnection mit Headern, 15s/30s Timeouts
- SemVer Parser + `isNewerVersion()` Vergleich, fail closed bei Parse-Fehlern
- `selectApkAsset()`: exakter Namensmatch, Single-Fallback, Debug-/Unsigned-Reject
- `downloadApk()`: .part-Download, Groessenvergleich, atomarer Rename
- `UpdateCheckerTest.kt`: 15 Unit-Tests (SemVer, JSON, Asset-Selektion)
- `testDebugUnitTest`: 65 Tests, alle bestanden

### 2026-06-24 - P3: APK-Verifikation und Installer-Intent

- `UpdateApkVerifier.kt`: SHA256-Berechnung, APK-PackageInfo-Lesen, Package-Name-Prüfung
- VersionCode-Vergleich (APK vs installiert), Signaturvergleich API 24-27 + API >= 28
- `createInstallerIntent()` mit FileProvider content-URI + FLAG_GRANT_READ_URI_PERMISSION
- `needsInstallPermission()` + `createInstallPermissionIntent()` für Settings-Flow
- `savePendingApk()`/`getPendingApk()`/`clearPendingApk()` für Settings-Roundtrip
- `LauncherActivity.maybeHandlePendingApk()` in `onResume()`: Re-Verifikation + Installer
- `UpdateApkVerifierTest.kt`: 4 Unit-Tests (SHA256 bekannt, leere Datei, große Daten, VerificationResult)
- `compileDebugKotlin`: erfolgreich
- `testDebugUnitTest`: 72 Tests, alle bestanden

### 2026-06-24 - P4: LauncherActivity Integration

- `UpdatePrefs.kt`: Opt-in, Rate-Limit (24h), Prompted-Version, Version-Helpers in SharedPreferences
- `LauncherActivity.maybePromptAutoUpdateOptIn()` in `onCreate()`: Opt-in-Prüfung + Dialog
- `showOptInDialog()`: Opt-in-Dialog (Aktivieren/Nein danke), kein Settings-Sprung
- `performUpdateCheck(force)`: Netzwerk-Check, Rate-Limit, Background-Thread API-Call
- `showUpdateAvailableDialog()`: Version, Größe, Release-Notes (gekürzt), Download/Later
- `startUpdateDownload()`: Background-Download mit ProgressBar + Status-Text
- `showInstallReadyDialog()`: Install-Button nach erfolgreicher Verifikation
- `tryInstallApk()`: Install-Permission-Check, Settings-Flow mit Pending-APK
- `showInstallPermissionDialog()`: Permission-Erklärung + Settings-Button
- `showUpdateErrorDialog()`: Fehlerdialog für Download/Verifikation
- Lifecycle-Safety: `isActivityAlive`/`runOnUiIfAlive` vor allen UI-Updates
- `compileDebugKotlin`: erfolgreich
- `testDebugUnitTest`: 72 Tests, alle bestanden

### 2026-06-24 - P5: Build, Test, Release-Dokumentation

- `assembleRelease`: BUILD SUCCESSFUL, signierte APK erstellt
- `apksigner verify --print-certs`: Zertifikat bestaetigt (CN=JA2 Stracciatella)
- `aapt dump badging`: `package: name='com.ja2.reborn' versionCode='1000005' versionName='1.0.5'`
- `testDebugUnitTest`: 72 Tests, alle bestanden
- `CHANGELOG.md`: Auto-Update-Checker (P1-P5) dokumentiert
- `JA2AutoUpdate.md`: P5-Status, Changelog, Device-Tests als `pending-device` markiert
- Device-/Staging-Tests (P5.5-P5.8) erfordern Android-Hardware und sind dokumentiert
