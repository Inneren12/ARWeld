# Pilot Build Steps

Use these steps for the Sprint 6 pilot build.

## 1) Build Release APK
```bash
./gradlew :app:assembleRelease
```

## 2) Install on Device
- Transfer `app/build/outputs/apk/release/app-release.apk` to the Pixel 9.
- Install via adb or device file manager.

## 3) Preflight
- Confirm ARCore services installed/updated.
- Open app and log in as a test user.
- Verify AR view loads and tracking indicator appears.

## 4) Run Pilot Checklist
- Execute `docs/testing/S6_PILOT_CHECKLIST.md`.
- Record PASS/FAIL in the checklist.

## 5) Capture Diagnostics
- Generate export package via Export Center.
- Share `evidence.zip` + `manifest.sha256` if support requests diagnostics.
