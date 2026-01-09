# Sprint 6 Pilot Readiness Checklist

Use this checklist during pilot preparation and on-site readiness review.

## Build + Install
- [ ] `./gradlew :app:assembleRelease` completes.
- [ ] Release APK installs on Pixel 9 test device.
- [ ] ARCore services installed/updated.

## AR Alignment + Tracking
- [ ] Multi-marker alignment (2+ markers) improves stability.
- [ ] Drift warning appears when alignment shifts > threshold.
- [ ] Re-align restores stable tracking.
- [ ] Manual alignment fallback works (3-point).

## Performance Guardrails
- [ ] FPS stays ≥30 under normal inspection.
- [ ] Guardrail mode activates if FPS <20 (visuals/processing reduced).
- [ ] Operator warning appears when performance is low.

## QC Evidence Gate
- [ ] QC cannot pass without 1 AR screenshot + 1 photo.
- [ ] Evidence counters update after capture.
- [ ] Timeline shows evidence events.

## Offline Readiness
- [ ] Offline workflows complete without network.
- [ ] Sync queue populates after events/evidence.
- [ ] No data loss after app restart.

## Export + Diagnostics
- [ ] Export package generated (JSON/CSV + evidence.zip + manifest.sha256).
- [ ] Evidence manifest hashes verify.
- [ ] Diagnostic export steps documented and runnable.

## Pilot Scenarios
- [ ] Scenarios 1–12 in `docs/testing/S6_PILOT_SCENARIOS.md` executed.
- [ ] All scenarios pass or have documented mitigations.
