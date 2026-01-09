# Sprint 6 Release Notes — Pilot Readiness

## Highlights
- Multi-marker alignment refinement with drift monitoring and smoothing.
- Performance guardrails when FPS drops (reduced visuals/processing + warning).
- Pilot documentation package: scenarios, readiness checklist, diagnostic export.

## AR + Performance
- Tracking stability improvements for 2+ markers in view.
- Drift estimate surfaces when alignment shifts beyond thresholds.
- CV throttling under low FPS to keep UX responsive.

## Pilot Documentation
- Manual test scenarios: `docs/testing/S6_PILOT_SCENARIOS.md`
- Readiness checklist: `docs/testing/S6_PILOT_CHECKLIST.md`
- Diagnostic export guide: `docs/ops/S6_DIAGNOSTIC_EXPORT.md`

## Build Notes
- Release build command: `./gradlew :app:assembleRelease`
- Export folder: `Android/data/<package>/files/exports/<export_id>/`
