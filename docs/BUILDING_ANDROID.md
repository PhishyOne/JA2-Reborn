# Building the Android APK

This document describes the Android release build for JA2 Reborn Android.

## Requirements

- Windows host
- Android Studio JBR / JDK 17
- Android SDK with `compileSdk 35`
- Android NDK `27.2.12479018`
- Rust and Cargo
- Rust Android targets:
  - `aarch64-linux-android`
  - `armv7-linux-androideabi`
  - `i686-linux-android`
  - `x86_64-linux-android`

Install missing Rust targets with:

```powershell
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

## Environment

Set the Android and Java paths before building. Adjust paths for your local machine.

```powershell
cd <repo>\android

$env:JAVA_HOME='<path-to-android-studio-jbr>'
$env:ANDROID_HOME='<path-to-android-sdk>'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:USERPROFILE\.cargo\bin;$env:Path"
```

Example path layout:

```text
Android SDK: <user-home>\AppData\Local\Android\Sdk
NDK:         <user-home>\AppData\Local\Android\Sdk\ndk\27.2.12479018
JDK 17:      <Android Studio>\jbr
Rust/Cargo:  <user-home>\.cargo\bin\cargo
```

## Build

For a normal incremental release build:

```powershell
.\gradlew.bat :app:assembleRelease
```

For the first build after deleting caches, or after changes to SDL Java, JNI registration, CMake files, native source lists, or dependency build scripts:

```powershell
.\gradlew.bat :app:assembleRelease --rerun-tasks
```

Expected output:

```text
android\app\build\outputs\apk\release\app-release-unsigned.apk
```

The repository does not contain release signing files, so the default release build produces an unsigned APK. If you need a signed APK, configure signing locally outside the repository.

## Install on Device

```powershell
$adb="$env:ANDROID_HOME\platform-tools\adb.exe"
& $adb devices
& $adb install -r android\app\build\outputs\apk\release\app-release.apk
& $adb shell appops set com.ja2.reborn MANAGE_EXTERNAL_STORAGE allow
```

Do not install a debug build over an existing release build. Android will reject it because the signatures differ.

## Smoke Test

```powershell
$adb="$env:ANDROID_HOME\platform-tools\adb.exe"
& $adb shell am force-stop com.ja2.reborn
& $adb logcat -c
& $adb shell monkey -p com.ja2.reborn -c android.intent.category.LAUNCHER 1

Start-Sleep -Seconds 2
& $adb shell dumpsys activity activities | Select-String 'topResumedActivity|RebornActivity|LauncherActivity'
& $adb shell pidof com.ja2.reborn
```

Expected:

- The app process exists.
- The launcher opens.
- Starting the game enters `RebornActivity`.
- There is no `libSDL2.so not found` error.
- There is no `NoSuchMethodError` from `org.libsdl.app`.

## Cleaning Build Outputs

Generated build outputs can be deleted before a fresh build:

```text
android\.gradle
android\.kotlin
android\build
android\build-monitor
android\last-screen.png
android\app\.cxx
android\app\build
rust\target
```

Keep source directories and wrapper files:

```text
android\app\src
android\gradle
android\gradlew
android\gradlew.bat
assets
cmake
dependencies
rust
src
CMakeLists.txt
```

Avoid broad cleanup commands such as:

```powershell
git clean -xfd
```

This repository contains source files and local configuration that should not be removed accidentally.

## Troubleshooting

If a clean release build fails after CMake or native source-list changes, delete `android\app\.cxx` and rebuild with `--rerun-tasks`.

If the native SDL configure step cannot find shell tools on Windows, check the CMake dependency patches under:

```text
dependencies\lib-sdl2\builder\
```
