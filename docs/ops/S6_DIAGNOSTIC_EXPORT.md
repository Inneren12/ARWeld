# Sprint 6 Diagnostic Export (Zip)

Use the Supervisor Export Center to generate a diagnostic zip for pilot support.

## Output Contents
The export directory includes:
- `export.json` — full report payload.
- `export.csv` — flattened report (optional CSV).
- `evidence.zip` — evidence files packaged.
- `manifest.sha256` — checksums for all files in the export folder.

## Steps (Device)
1. Sign in as Supervisor.
2. Open **Export Center**.
3. Select the date range (Today / Shift).
4. Tap **Export**.
5. Locate output under the app external files directory:
   - `Android/data/<package>/files/exports/<export_id>/`
6. Share the entire export folder (or just `evidence.zip` + `manifest.sha256`) with the pilot support contact.

## Verification
- Confirm `manifest.sha256` contains entries for `export.json` and `evidence.zip`.
- If needed, verify hashes using `sha256sum` on a workstation after transfer.
