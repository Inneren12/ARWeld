# Diagnostics Export (Zip)

Use this guide to capture a lightweight diagnostics zip for support, AR performance investigations, or thermal/memory issues.

## Where to find the export
- **In-app path:** Supervisor menu → **Export Center** → **Diagnostics** → **Export diagnostics zip**.
- **On device storage:** `Android/data/<package>/files/exports/diagnostics/diag_<timestamp>/diagnostics.zip`

## What is inside the zip
- `diagnostics.json` — combined report (metadata, key settings, recent events, AR telemetry, device health).
- `recent_events.json` — recent event summary.
- `ar_telemetry.json` — FPS, frame-time p95, CV latency p95 snapshot.
- `device_health.json` — thermal status + last memory trim signal.
- `settings.json` — key runtime settings (performance mode, thermal/memory hints).
- `logs.txt` — text summary of recent events.

## Steps (Device)
1. Sign in as **Supervisor**.
2. Open **Export Center**.
3. Under **Diagnostics**, tap **Export diagnostics zip**.
4. Share the `diagnostics.zip` with support or attach it to the incident ticket.

## Quick checks
- Confirm the zip contains all files listed above.
- Verify `ar_telemetry.json` has non-zero FPS and frame-time values after AR usage.
- If the device was hot, `device_health.json` should report a `thermalStatus` of `severe` or higher.
