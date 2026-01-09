# Sprint 6 Pilot Scenarios (10–15)

These scenarios are the manual pilot tests for Sprint 6 (AR hardening + pilot readiness).
Record PASS/FAIL and notes during on-device testing.

## 1) Happy Path — Full Cycle
- **Steps:** Assembler scans → auto-claim/start → AR align → mark ready. QC starts → AR inspect → capture 1 AR screenshot + 2 photos → Pass. Supervisor reviews timeline + evidence.
- **Expected:** Workflow completes without errors; evidence + events visible in timeline.

## 2) QC Rejection → Rework → Pass
- **Steps:** QC fails with reason (e.g., Porosity). Assembler reworks and marks ready. QC re-inspects and passes.
- **Expected:** WorkItem enters REWORK state then returns to PASSED; timeline reflects both QC cycles.

## 3) Low Lighting Scan
- **Steps:** Attempt scan in dim lighting. Improve lighting and retry.
- **Expected:** Clear guidance for scan failure; success after lighting improves.

## 4) Dirty/Obscured Marker
- **Steps:** Cover marker partially, observe AR tracking; clean marker and re-align.
- **Expected:** Tracking indicator warns about drift/instability; alignment stabilizes after cleaning.

## 5) Multi-Marker Refinement
- **Steps:** Show 2+ markers simultaneously during AR alignment.
- **Expected:** Alignment becomes more stable with reduced drift vs single marker.

## 6) Drift Trigger + Re-align
- **Steps:** Move around part to induce drift; observe indicator; tap to re-align if prompted.
- **Expected:** Drift warning appears; re-align improves quality score.

## 7) Performance Guardrail
- **Steps:** Load large model and move around rapidly to stress FPS.
- **Expected:** Performance warning + guardrail mode activates (reduced visuals/processing).

## 8) Offline Mode End-to-End
- **Steps:** Disable network mid-workflow. Complete full cycle offline.
- **Expected:** Workflow completes offline; sync queue increments; no crashes.

## 9) Evidence Gate — QC Cannot Pass Without Evidence
- **Steps:** QC attempts to pass without AR screenshot/photo.
- **Expected:** Action blocked; guidance indicates missing evidence.

## 10) Export Package Validation
- **Steps:** Supervisor exports report to device storage; open JSON and CSV; inspect evidence.zip + manifest.sha256.
- **Expected:** Files present and readable; manifest hashes match evidence files.

## 11) Long Timeline Readability
- **Steps:** Create 20+ events (claim, start, pause, resume, QC fail/pass).
- **Expected:** Timeline renders chronologically; no missing entries.

## 12) Session Continuity
- **Steps:** Start work, force-close app, reopen.
- **Expected:** WorkItem remains in IN_PROGRESS; no data loss.
