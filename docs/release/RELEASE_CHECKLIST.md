# Release checklist

## Versioning policy
- Bump `versionCode` for every release build.
- Update `versionName` to the user-facing semantic version (for example `1.2.0`).
- Tag the release in VCS with the same `versionName`.

## Signing (internal / pilot)
- Internal (smoke): use a local debug or temporary release keystore and keep the APK unsigned only for device smoke installs.
- Pilot: sign with the shared release keystore and store credentials in CI secrets.
- Verify the final APK/AAB signature and upload-ready artifacts before shipping.

## Smoke steps (release APK on device)
1. Install release APK on a Pixel device.
2. Launch the app and login.
3. Navigate: Home → Scanner → Work Summary.
4. Open the AR view and confirm camera + tracking initialize.
5. Scan a barcode to confirm ML Kit barcode pipeline works.
