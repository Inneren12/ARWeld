# Sprint 2 End-to-End Verification (QA Notes)

## Checklist Summary
- **ScanCode → ResolveWorkItemByCode → WorkItemSummary:** ✅ Implemented via `ScanCodeViewModel.resolveCode`, which calls `ResolveWorkItemByCodeUseCase` and navigates to the found WorkItem ID on success.
- **Assembler actions append WORK_CLAIMED / WORK_STARTED / WORK_READY_FOR_QC:** ✅ Use cases construct and append events with actor/device metadata for claim, start, and ready-for-QC transitions.
- **AR boots (session + model load):** ✅ `ARViewController` initializes ARCore session, renderer, and loads the sprint test node GLB into the scene.
- **Marker alignment logs AR_ALIGNMENT_SET:** ✅ Marker-based alignment updates the model pose and asynchronously logs an `AR_ALIGNMENT_SET` event with marker ID and transform.
- **Manual alignment logs AR_ALIGNMENT_SET:** ✅ Manual 3-point solve applies the rigid transform, resets state, and emits an `AR_ALIGNMENT_SET` event with point count and pose data.
- **Tracking indicator visible:** ✅ `ARViewScreen` renders the tracking indicator widget bound to `TrackingStatus` with color-coded quality.

## Evidence
- **Scanner to WorkItemSummary:** ViewModel resolves codes and triggers navigation on matches.【F:app/src/main/kotlin/com/example/arweld/ui/scanner/ScanCodeViewModel.kt†L12-L37】
- **Assembler event actions:** Claim/Start/Ready use cases append `WORK_CLAIMED`, `WORK_STARTED`, and `WORK_READY_FOR_QC` events with user/device context.【F:core-data/src/main/kotlin/com/example/arweld/core/data/work/WorkActionUseCases.kt†L1-L85】
- **AR session + model load:** Controller sets up AR session/rendering and loads `test_node.glb` into the scene during startup.【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt†L167-L210】
- **Marker alignment + event log:** Marker pose update aligns the model, refreshes score, and logs `AR_ALIGNMENT_SET` via `logMarkerAlignment`.【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt†L300-L320】
- **Manual alignment + event log:** Hit-test collection drives a 3-point solve; success updates the model and emits `AR_ALIGNMENT_SET` with metadata.【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt†L325-L367】【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/alignment/AlignmentEventLogger.kt†L60-L100】
- **Tracking indicator UI:** Compose overlay displays `TrackingIndicator` with quality colors and reason text bound to `TrackingStatus`.【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/ui/arview/ARViewScreen.kt†L135-L186】【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/ui/arview/ARViewScreen.kt†L238-L281】
- **Tracking heuristics:** Controller computes tracking quality from ARCore state, marker recency, alignment freshness, and feature point density.【F:feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt†L470-L545】

## Test Execution
- `./gradlew :app:assembleDebug` ➜ **FAILED** (Android SDK missing in CI; requires `ANDROID_HOME` or `local.properties`).【67a839†L1-L10】
- `./gradlew test` ➜ **FAILED** (same SDK path requirement).【5ed8f8†L1-L14】

## Remaining Risks
- **P0:** Android toolchain unavailable in container prevented assembling and running tests; build relies on a configured SDK path.
- **P1:** AR alignment events depend on a logged-in user and non-null `workItemId`; missing context skips logging with only warnings.
- **P2:** Manual alignment relies on collecting exactly three hit-test points; UX guidance/status messaging is minimal for ambiguous inputs.

## Verdict
**S2 DONE?** Code paths for the sprint-2 checklist are present, but build/test blocking on missing Android SDK keeps verification at **⚠️ Partially Verified** until toolchain is provisioned.
