# Releasing

This repository should not contain APK, AAB, keystore, or signing files. Publish builds through GitHub Releases instead.

## Release 1.0 APK

Keep the prepared release artifact outside the repository:

```text
<local-path>\JA2RebornRelease1.0.apk
```

Current file details:

```text
Filename: JA2RebornRelease1.0.apk
SHA256:   DCF8A7DE4A1C63F9120454B666AA42E6B9895A3384E5D9861A238B8F258455F0
Size:     100,341,109 bytes
```

Upload it as a GitHub Release asset, not as a committed repository file.

Recommended release metadata:

```text
Tag:        v1.0.0
Title:      JA2 Reborn 1.0
Asset:      JA2RebornRelease1.0.apk
```

Suggested release notes:

```markdown
# JA2 Reborn 1.0

Initial public Android release.

This APK does not include Jagged Alliance 2 game data. You need a legally owned copy of the original game files.

SHA256:
`DCF8A7DE4A1C63F9120454B666AA42E6B9895A3384E5D9861A238B8F258455F0`
```

## GitHub CLI Upload

After pushing the public branch and creating a `v1.0.0` tag, the APK can be uploaded with:

```powershell
gh release create v1.0.0 <local-path>\JA2RebornRelease1.0.apk `
  --title "JA2 Reborn 1.0" `
  --notes-file docs\RELEASE_NOTES_v1.0.0.md
```

Do not commit the APK into the Git repository.
