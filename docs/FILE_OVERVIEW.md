# ARWeld — File Overview and Navigation Guide

This document provides a **practical map** of the ARWeld codebase, explaining where files live and where to add new functionality. Use this as a quick reference when implementing features from `stage.md`.

---

### Work feature entry points

- `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/WorkItemSummaryScreen.kt` — Composable that shows WorkItem id/code/type, derived `WorkItemState`, and role-aware assembler actions (claim/start/mark ready for QC).
- `app/src/main/kotlin/com/example/arweld/ui/work/WorkItemSummaryRoute.kt` — NavHost wrapper that forwards `workItemId` arguments from ScanCode/AssemblerQueue into the Hilt ViewModel.
- `core-domain/src/test/kotlin/com/example/arweld/core/domain/state/WorkItemReducerHappyPathTest.kt` — Reducer unit tests covering happy path and fail→rework→ready→pass rework flow.
- `feature-work/src/main/kotlin/com/example/arweld/feature/work/viewmodel/QcChecklistViewModel.kt` — Holds `QcChecklistResult` + item-state updates for the 5-point QC checklist (geometry, completeness, fasteners, marking, cleanliness).

### AR view and rendering (feature-arview)

- AR view controller + lifecycle bridge: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt` wires `SurfaceView` hosting to `ARViewLifecycleHost` and ARCore session events.
- Filament renderer bridge: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARSceneRenderer.kt` renders the GLB using either a fallback anchor or the computed `T_world_zone` once marker alignment is available (S2-18).
- GLB loader: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/render/AndroidFilamentModelLoader.kt` (implements `ModelLoader.loadGlbFromAssets`).
- Fixed AR test model render entry: `ARViewController.loadTestNodeModel()` attaches the test node to the scene via `ARSceneRenderer`.
- Marker detection pipeline: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/marker/MarkerDetector.kt` defines the API + `DetectedMarker` result (id + four ordered corners). `StubMarkerDetector` implements a no-op detector.
- Frame wiring: `ARSceneRenderer` forwards each ARCore `Frame` to `ARViewController`, which invokes `MarkerDetector.detectMarkers(frame)` off the UI thread and exposes the latest results via `ARViewController.detectedMarkers` for downstream pose estimation/alignment consumers.
- Marker→zone transforms: `core-domain/src/main/kotlin/com/example/arweld/core/domain/spatial/ZoneTransform.kt` defines `markerId` + `T_marker_zone` (`Pose3D`), with a hardcoded registry at `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/zone/ZoneRegistry.kt` (S2-18).
- Zone/world computation: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/zone/ZoneAligner.kt` multiplies `T_world_marker * T_marker_zone` to produce `T_world_zone`, which `ARViewController` applies to `ARSceneRenderer` so the model root aligns to the marker once detected.
- Manual fallback alignment: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt` enters a manual mode via the UI button, queues hitTests from taps through `ARSceneRenderer.queueHitTest`, collects `AlignmentPoint`s, and solves `T_world_model` with `alignment/RigidTransformSolver.kt` before updating the renderer.
- Alignment correspondences live in `core-domain/src/main/kotlin/com/example/arweld/core/domain/spatial/AlignmentSample.kt` (`AlignmentPoint` pairs of world/model points) and are surfaced to the UI via `alignment/ManualAlignmentState.kt`.
- Test GLB asset packaged at `feature-arview/src/main/assets/models/test_node.glb`.
- AR screenshots: `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ArScreenshotService.kt` defines the API, implemented by `ARViewController.captureArScreenshot()` using PixelCopy to save PNGs under `filesDir/evidence/ar_screenshots` and return a `Uri`.

### Seed data (Sprint 2)

- Seed definitions: `core-data/src/main/kotlin/com/example/arweld/core/data/seed/SeedWorkItems.kt` — mock `WorkItemEntity` records with stable ids/codes (`ARWELD-W-00X`).
- User seeds: `core-data/src/main/kotlin/com/example/arweld/core/data/seed/SeedUsers.kt` — mock `UserEntity` records for Assembler/QC/Supervisor (and Director) roles used by mock login.
- Initializer: `core-data/src/main/kotlin/com/example/arweld/core/data/seed/DbSeedInitializer.kt` — inserts seeds when the `work_items` or `users` tables are empty.
- Trigger: `app/src/main/kotlin/com/example/arweld/ArWeldApplication.kt` — launches `seedIfEmpty()` on app start (IO scope) so scanning a known code resolves to a WorkItem immediately and AuthRepository can return seeded users.

---

## Project Structure

```
ARWeld/
├── app/                                    # Android app module
│   ├── src/main/
│   │   ├── AndroidManifest.xml            # App manifest, permissions
│   │   ├── kotlin/com/example/arweld/
│   │   │   ├── ArWeldApplication.kt       # Application class
│   │   │   ├── MainActivity.kt            # Single activity host
│   │   │   ├── di/                        # Dependency injection
│   │   │   │   └── AppModule.kt           # Hilt/Koin DI setup
│   │   │   └── navigation/                # Navigation setup
│   │   │       └── AppNavigation.kt       # NavGraph, routes
│   │   └── res/                           # Resources (layouts, strings, etc.)
│   └── build.gradle.kts                   # App module build config
│
├── core/
│   ├── domain/                            # Pure domain logic
│   │   ├── src/main/kotlin/com/example/arweld/core/domain/
│   │   │   ├── model/                     # Domain models
│   │   │   │   ├── WorkItem.kt
│   │   │   │   ├── User.kt
│   │   │   │   ├── Role.kt                # ✅ S1-04: ASSEMBLER, QC, SUPERVISOR, DIRECTOR
│   │   │   ├── event/                     # Event log domain
│   │   │   │   ├── Event.kt               # Immutable domain event
│   │   │   │   ├── EventType.kt           # Workflow milestones
│   │   │   │   └── EventRepository.kt     # Domain contract to append/query events
│   │   │   ├── state/                     # ✅ S1-08: Derived state + reducer
│   │   │   │   └── WorkItemState.kt       # WorkStatus, QcStatus, reduce(events)
│   │   │   ├── evidence/                  # ✅ S1-07: Evidence domain models
│   │   │   │   ├── EvidenceKind.kt        # PHOTO, AR_SCREENSHOT, VIDEO, MEASUREMENT
│   │   │   │   └── Evidence.kt            # id, eventId, uri, sha256, metaJson, createdAt (ms since epoch)
│   │   │   ├── auth/                      # ✅ S1-04: Authentication and authorization
│   │   │   │   ├── Permission.kt          # Permissions enum (CLAIM_WORK, START_QC, etc.)
│   │   │   │   └── RolePolicy.kt          # Role-based permission policy
│   │   │   ├── policy/                    # Business rules
│   │   │   │   ├── QcEvidencePolicy.kt    # Validates evidence after QC start
│   │   │   │   └── QcEvidencePolicyException.kt # Raised when QC evidence gate fails
│   │   │   └── validation/                # Domain validation
│   │   │       └── ValidationResult.kt
│   │   └── build.gradle.kts               # Pure Kotlin library
│   │
│   ├── data/                              # Data layer
│   │   ├── src/main/kotlin/com/example/arweld/core/data/
│   │   │   ├── db/                        # Room database
│   │   │   │   ├── AppDatabase.kt      # Database setup
│   │   │   │   ├── entity/                # Room entities
│   │   │   │   │   ├── WorkItemEntity.kt
│   │   │   │   │   ├── EventEntity.kt
│   │   │   │   │   ├── EvidenceEntity.kt
│   │   │   │   │   ├── UserEntity.kt
│   │   │   │   │   └── SyncQueueEntity.kt
│   │   │   │   └── dao/                   # Data Access Objects
│   │   │   │       ├── WorkItemDao.kt
│   │   │   │       ├── EventDao.kt
│   │   │   │       ├── EvidenceDao.kt
│   │   │   │       ├── UserDao.kt
│   │   │   │       └── SyncQueueDao.kt
│   │   │   ├── event/                      # Event entity/domain mappers
│   │   │   │   └── EventMappers.kt         # EventEntity ↔ Event conversions
│   │   │   ├── repository/                # Repository implementations
│   │   │   │   ├── WorkItemRepositoryImpl.kt
│   │   │   │   ├── EventRepositoryImpl.kt
│   │   │   │   └── EvidenceRepositoryImpl.kt
│   │   │   ├── file/                      # File storage
│   │   │   │   ├── EvidenceFileManager.kt # Save/load evidence files
│   │   │   │   └── ChecksumCalculator.kt  # SHA-256 hashing
│   │   │   ├── sync/                      # Offline queue
│   │   │   │   └── SyncManager.kt
│   │   └── build.gradle.kts               # Android library + Room
│   │
│   └── auth/                              # Authentication
│       ├── src/main/kotlin/com/example/arweld/core/auth/
│       │   ├── repository/                # Auth implementations
│       │   │   └── InMemoryAuthRepository.kt
│       │   └── di/AuthModule.kt           # Hilt binding for AuthRepository
│       └── build.gradle.kts
│
├── feature/
│   ├── home/                              # Home screen
│   │   ├── src/main/kotlin/com/example/arweld/feature/home/
│   │   │   └── ui/
│   │   │       └── HomeScreen.kt          # Role-based navigation tiles
│   │   └── build.gradle.kts
│   │
│   ├── work/                              # Assembler workflows (or "assembler")
│   │   ├── src/main/kotlin/com/example/arweld/feature/work/
│   │   │   ├── ui/
│   │   │   │   ├── AssemblerQueueScreen.kt  # Assembler queue grouped by status
│   │   │   │   ├── QcQueueScreen.kt        # QC queue list (READY_FOR_QC/QC_IN_PROGRESS)
│   │   │   │   ├── QcStartScreen.kt        # QC inspection entry showing WorkItem details, AR/back buttons, PASS/FAIL gated by evidence policy
│   │   │   │   ├── WorkItemSummaryScreen.kt # WorkItem detail
│   │   │   │   └── TimelineScreen.kt        # Placeholder timeline view
│   │   │   └── viewmodel/
│   │   │       ├── AssemblerQueueViewModel.kt # Groups MyQueue by status for assembler
│   │   │       ├── WorkItemSummaryViewModel.kt # Loads detail + assembler actions
│   │   │       ├── QcQueueViewModel.kt        # Sprint 3 QC queue loader (READY_FOR_QC/QC_IN_PROGRESS)
│   │   │       └── QcStartViewModel.kt        # Calls StartQcInspectionUseCase + surfaces QcEvidencePolicy.canCompleteQc for PASS/FAIL buttons
│   │   │   # Queue ordering uses `readyForQcSince` computed in `core-domain/.../state/WorkItemState.kt` from the last
│   │   │   # WORK_READY_FOR_QC event
│   │   └── build.gradle.kts
│   │
│   ├── scanner/                           # Barcode/QR scanning
│   │   ├── src/main/kotlin/com/example/arweld/feature/scanner/
  │   │   │   ├── ui/
  │   │   │   │   ├── ScanCodeScreen.kt      # ScanCode screen combining preview + last code + Continue
  │   │   │   │   └── ScannerPreview.kt      # CameraX preview composable with permission + ML Kit callback
  │   │   │   ├── viewmodel/                  # (handled in app module via ui/scanner/ScanCodeViewModel.kt)
  │   │   │   ├── usecase/
  │   │   │   │   └── ResolveWorkItemByCodeUseCase.kt # Code → WorkItem lookup (core-domain contract)
  │   │   │   └── camera/
│   │   │       ├── CameraPreviewController.kt # CameraX setup and lifecycle binding
│   │   │       └── BarcodeAnalyzer.kt     # ML Kit analyzer emitting deduplicated barcode/QR values
│   │   └── build.gradle.kts
│   │
│   ├── qc/                                # QC inspector workflows
│   │   ├── src/main/kotlin/com/example/arweld/feature/qc/
│   │   │   ├── ui/
│   │   │   │   ├── QcQueueScreen.kt       # QC queue (READY_FOR_QC items)
│   │   │   │   ├── QcInspectionScreen.kt  # Main inspection screen
│   │   │   │   ├── EvidenceGalleryScreen.kt # View evidence
│   │   │   │   └── ChecklistWidget.kt     # Checklist component
│   │   │   ├── viewmodel/
│   │   │   │   ├── QcQueueViewModel.kt
│   │   │   │   └── QcInspectionViewModel.kt
│   │   │   ├── usecase/
│   │   │   │   ├── StartQcInspectionUseCase.kt
│   │   │   │   ├── CapturePhotoEvidenceUseCase.kt
│   │   │   │   ├── PassQcUseCase.kt
│   │   │   │   └── FailQcUseCase.kt
│   │   │   │   # `StartQcInspectionUseCase` builds a `QC_STARTED` event using the current QC user, `TimeProvider.nowMillis()`,
│   │   │   │   # and `DeviceInfoProvider.deviceId`; Android implementations live in `core-data/.../system/`.
│   │   │   └── camera/
│   │   │       └── PhotoCaptureManager.kt
│   │   └── build.gradle.kts
│   │
│   ├── supervisor/                        # Supervisor workflows
│   │   ├── src/main/kotlin/com/example/arweld/feature/supervisor/
│   │   │   ├── ui/
│   │   │   │   ├── SupervisorDashboardScreen.kt # Dashboard + KPIs
│   │   │   │   ├── WorkItemListScreen.kt        # Filterable list
│   │   │   │   ├── WorkItemDetailScreen.kt      # Timeline + evidence
│   │   │   │   ├── ReportsScreen.kt             # Aggregated reports
│   │   │   │   └── ExportScreen.kt              # Export center
│   │   │   ├── viewmodel/
│   │   │   │   ├── SupervisorDashboardViewModel.kt
│   │   │   │   ├── WorkItemListViewModel.kt
│   │   │   │   ├── WorkItemDetailViewModel.kt
│   │   │   │   ├── ReportsViewModel.kt
│   │   │   │   └── ExportViewModel.kt
│   │   │   ├── usecase/
│   │   │   │   ├── CalculateKpisUseCase.kt
│   │   │   │   ├── ExportReportUseCase.kt
│   │   │   │   └── GenerateEvidencePackageUseCase.kt
│   │   │   └── export/
│   │   │       ├── JsonExporter.kt
│   │   │       ├── CsvExporter.kt
│   │   │       └── ChecksumFileGenerator.kt
│   │   └── build.gradle.kts
│   │
│   └── arview/                            # AR visualization
│       ├── src/main/kotlin/com/example/arweld/feature/arview/
│       │   ├── ui/
│       │   │   └── arview/
│       │   │       └── ARViewScreen.kt    # Compose AR view with lifecycle wiring, tracking quality indicator, manual alignment controls, and error overlay
│       │   ├── arcore/
│       │   │   ├── ARViewController.kt    # Hosts AR surface, forwards lifecycle callbacks, computes TrackingStatus from camera state/markers/feature points
│       │   │   ├── ARSceneRenderer.kt     # ARCore → Filament bridge for fixed test model pose (S2-15)
│       │   │   ├── ARCoreSessionManager.kt # Creates/configures ARCore Session; handles resume/pause/destroy
│       │   │   ├── ArCoreMappers.kt       # Maps ARCore Pose/intrinsics → domain Pose3D/CameraIntrinsics for pose estimation
│       │   │   └── ARViewLifecycleHost.kt # Forwards lifecycle events to controller
│       │   ├── alignment/
│       │   │   ├── ArAlignmentPayload.kt  # Serialized payload for AR_ALIGNMENT_SET event (method, marker/manual metadata, pose summary)
│       │   │   ├── AlignmentEventLogger.kt # Uses EventRepository/AuthRepository to append AR alignment events on success
│       │   │   └── ManualAlignmentState.kt # UI-facing manual alignment progress holder
│       │   ├── pose/
│       │   │   └── MarkerPoseEstimator.kt # Computes T_world_marker using planar PnP (homography) + camera intrinsics (S2-17)
│       │   ├── tracking/
│       │   │   └── TrackingQuality.kt     # UI-facing `TrackingQuality` enum + `TrackingStatus` reason text
│       │   └── render/
│       │       ├── ModelLoader.kt         # Abstraction for loading GLB assets from src/main/assets
│       │       └── AndroidFilamentModelLoader.kt # Filament-based implementation returning LoadedModel
│       ├── src/main/assets/models/test_node.glb # Sprint test node model packaged in APK
│       └── build.gradle.kts
│
│       ARCore session lifecycle wiring:
│       - `ARViewLifecycleHost` observes lifecycle events and calls `ARViewController.onResume/onPause/onDestroy`.
│       - `ARViewController` lazily initializes `ARCoreSessionManager` and forwards display rotation + surface size.
│       - `ARCoreSessionManager` creates/configures `Session` on first resume (world tracking, horizontal plane finding) and handles pause/destroy with error logging surfaced to the Compose overlay.
│       - `ArCoreMappers` adapts `camera.imageIntrinsics` into domain `CameraIntrinsics` and ARCore `Pose` into `Pose3D` for pose estimation.
│       - Marker pose estimation lives in `pose/MarkerPoseEstimator.kt`, composing `T_world_camera` from ARCore with PnP-derived `T_camera_marker`.
│       - Tracking quality indicator: `ARViewController` evaluates camera tracking state, recent marker detections, and feature point counts to emit `TrackingStatus` consumed by `ARViewScreen` overlay.
│       - AR alignment events: `ARViewController` logs `AR_ALIGNMENT_SET` via `AlignmentEventLogger` after marker or manual transforms succeed (payload defined in `alignment/ArAlignmentPayload.kt`).
│
├── docs/                                  # Documentation
│   ├── stage.md                           # Sprint roadmap (this!)
│   ├── PROJECT_OVERVIEW.md                # High-level overview
│   ├── MODULES.md                         # Module descriptions
│   └── FILE_OVERVIEW.md                   # This file
│
├── build.gradle.kts                       # Root project build config
├── settings.gradle.kts                    # Module includes
└── gradle.properties                      # Gradle properties
```

### Auth flow entry points
- `app/src/main/kotlin/com/example/arweld/ui/auth/LoginRoute.kt` — Compose UI with four role buttons (Assembler/QC/Supervisor/Director) that triggers mock login and navigates to Home.
- `app/src/main/kotlin/com/example/arweld/ui/auth/LoginViewModel.kt` — Hilt ViewModel injecting `AuthRepository` and invoking `loginMock(role)` before navigation to Home.
- `app/src/main/kotlin/com/example/arweld/navigation/AppNavigation.kt` — NavHost start destination (Login) and wiring for Home/WorkItemSummary/Timeline.

### Scanner entry points and ownership
- **Scanner UI/logic lives in** `feature-scanner` (`ui/ScanCodeScreen.kt`, `ui/ScannerPreview.kt`, `camera/CameraPreviewController.kt`, `camera/BarcodeAnalyzer.kt`).
- **Navigation into scanner** is declared in `app/src/main/kotlin/com/example/arweld/navigation/Routes.kt` (`ROUTE_SCAN_CODE`) and `AppNavigation.kt`, with the wrapper composable in `app/src/main/kotlin/com/example/arweld/ui/scanner/ScanCodeRoute.kt`. The Assembler home tile triggers this route via `app/src/main/kotlin/com/example/arweld/ui/home/HomeRoute.kt`.
- **ResolveWorkItemByCode use case** lives in `core-domain/src/main/kotlin/com/example/arweld/core/domain/work/ResolveWorkItemByCodeUseCase.kt` with data implementation in `core-data/src/main/kotlin/com/example/arweld/core/data/work/ResolveWorkItemByCodeUseCaseImpl.kt`.
- **Invocation:** `ScanCodeViewModel` (`app/src/main/kotlin/com/example/arweld/ui/scanner/ScanCodeViewModel.kt`) calls the use case when "Continue" is tapped on `ScanCodeScreen` and `ScanCodeRoute` navigates to `workItemSummaryRoute(workItemId)` on success.
- **Assembler action use cases** live in `core-domain/src/main/kotlin/com/example/arweld/core/domain/work/usecase/` (`ClaimWorkUseCase`, `StartWorkUseCase`, `MarkReadyForQcUseCase`) with data implementations in `core-data/src/main/kotlin/com/example/arweld/core/data/work/WorkActionUseCases.kt` that append domain `Event`s.
- **Invocation:** `AssemblerQueueViewModel` (`feature-work/.../viewmodel/AssemblerQueueViewModel.kt`) triggers `ClaimWorkUseCase`, while `WorkItemSummaryViewModel` (`feature-work/.../viewmodel/WorkItemSummaryViewModel.kt`) triggers `ClaimWorkUseCase`, `StartWorkUseCase`, and `MarkReadyForQcUseCase` before refreshing derived state for the UI screens.

### Photo capture infrastructure (S3-06)
- **Interface:** `feature-work/src/main/kotlin/com/example/arweld/feature/work/camera/PhotoCaptureService.kt` returns `PhotoCaptureResult` with the saved `Uri` and file size.
- **Implementation:** `app/src/main/kotlin/com/example/arweld/camera/CameraXPhotoCaptureService.kt` uses CameraX still capture, writing JPEGs to `filesDir/evidence/photos/` (app-private storage).
- **DI binding:** `app/src/main/kotlin/com/example/arweld/di/CameraModule.kt` binds the interface to the CameraX implementation for injection into UI or use cases.
- **Permissions:** Callers must request `Manifest.permission.CAMERA` (see `ScannerPreview` pattern) before invoking the service; the implementation validates permission before capturing.

**WorkItem models:**
- Domain definitions live in `core-domain/src/main/kotlin/com/example/arweld/domain/work/` (`WorkItemType.kt`, `WorkItem.kt`).
- Extend WorkItem schema here first (e.g., project/zone fields); map database entities in `core-data` to these domain types.

**Evidence models:**
- Domain definitions live in `core-domain/src/main/kotlin/com/example/arweld/core/domain/evidence/` (`EvidenceKind.kt`, `Evidence.kt`).
- To add a new evidence type (e.g., sensor log), extend `EvidenceKind` and update downstream clients (policies, storage, UI).
- `metaJson` stores flexible metadata (camera params, AR alignment, units). `createdAt` uses milliseconds since epoch.
- `EvidenceRepository` exposes `savePhoto(eventId, file)` and `saveArScreenshot(eventId, uri, meta)` implemented in `core-data/src/main/kotlin/com/example/arweld/core/data/repository/EvidenceRepositoryImpl.kt` to hash the file, persist metadata, and return the domain `Evidence`.
- `ArScreenshotMeta` (markerIds, trackingState, alignmentQualityScore, distanceToMarker) lives alongside the contract in `core-domain/src/main/kotlin/com/example/arweld/core/domain/evidence/EvidenceRepository.kt` and is serialized into `metaJson` for AR screenshots.
- Photo capture is provided by `PhotoCaptureService` (CameraX) which saves files to `filesDir/evidence/photos/`. AR screenshots are produced by `ArScreenshotService` (ARViewController) into `filesDir/evidence/ar_screenshots/` and passed to `saveArScreenshot` with alignment metadata. Future video capture will follow the same pattern.
- SHA-256 hashing for evidence files lives in `core-data/src/main/kotlin/com/example/arweld/core/data/file/ChecksumCalculator.kt`
  via `computeSha256(file: File)`; `savePhoto` stores the resulting checksum on the `Evidence.sha256` field for integrity checks.

## Repositories and derived state
- **WorkRepository (domain interface):** `core-domain/src/main/kotlin/com/example/arweld/core/domain/work/WorkRepository.kt`
- **WorkRepositoryImpl (data):** `core-data/src/main/kotlin/com/example/arweld/core/data/work/WorkRepositoryImpl.kt` — maps Room entities to domain models and uses the reducer to derive `WorkItemState`, including queue filters. Update here to change queue logic or mappings.

---

## Room entities (core-data)

**Package:** `core-data/src/main/kotlin/com/example/arweld/core/data/db/entity/`

- Room schema exports: `core-data/schemas/` (generated by KSP when `exportSchema=true`)

- `WorkItemEntity` ↔ `core-domain` `WorkItem` (id, projectId, zoneId, type, code; also retains description/nodeId for mapping)
- `EventEntity` ↔ `core-domain` `Event` (workItemId, type, timestamp, actorId, actorRole, deviceId, payloadJson) with indexes on `workItemId`, `actorId`
- `EvidenceEntity` ↔ `core-domain` `Evidence` (eventId, kind, uri, sha256, metaJson, createdAt) with index on `eventId`
- `UserEntity` ↔ `core-domain` `User` (id, name/displayName, role, optional lastSeen, isActive flag)
- `SyncQueueEntity` ↔ sync queue items (id, payloadJson, createdAt, status, retryCount) with index on `status`

## DAOs (core-data)

**Package:** `core-data/src/main/kotlin/com/example/arweld/core/data/db/dao/`

- `WorkItemDao` — lookup by id or code and bulk inserts for seeding/scans
- `EventDao` — insert single/bulk events, query timeline by workItemId (ASC), and recent actions by actorId (DESC)
- `EvidenceDao` — insert evidence and fetch attachments for a given event
- `UserDao` — fetch a single user, list all users, and seed/update user roster
- `SyncQueueDao` — enqueue single/bulk sync items and fetch earliest pending items by status/limit

### Instrumentation tests (core-data)

- `core-data/src/androidTest/java/com/example/arweld/core/data/db/dao/EventDaoInstrumentedTest.kt` — builds an in-memory `AppDatabase`,
  inserts an `EventEntity`, and reads it back through `EventDao` to validate Room wiring.

---

## DI / Hilt Configuration

ARWeld uses **Hilt** for dependency injection. This section explains where DI code lives and how to add new providers.

### Application Entry Point

**Location:** `app/src/main/kotlin/com/example/arweld/ArWeldApplication.kt`

```kotlin
@HiltAndroidApp
class ArWeldApplication : Application() {
    // Entry point for Hilt DI graph
}
```

**Registered in:** `app/src/main/AndroidManifest.xml`
```xml
<application android:name=".ArWeldApplication" ...>
```

### DI Modules

#### DataModule (core-data)

**Location:** `core-data/src/main/kotlin/com/example/arweld/core/data/di/DataModule.kt`

**Provides:**
- `AppDatabase` — Room database singleton built with `Room.databaseBuilder`
- DAOs: `WorkItemDao`, `EventDao`, `EvidenceDao`, `UserDao`, `SyncQueueDao`

**Where to add new database providers:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideNewDao(database: AppDatabase): NewDao {
        return database.newDao()
    }
}
```

#### RepositoryModule (core-data)

**Location:** `core-data/src/main/kotlin/com/example/arweld/core/data/di/DataModule.kt` (same file)

**Binds:**
- `WorkItemRepository` → `WorkItemRepositoryImpl`
- `EventRepository` (core-domain/event) → `EventRepositoryImpl` (core-data)
- `EvidenceRepository` → `EvidenceRepositoryImpl`

**Where to add new repository bindings:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNewRepository(
        impl: NewRepositoryImpl
    ): NewRepository
}
```

**EventRepository + mapping:**
- Domain contract: `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/EventRepository.kt`
- Mapping helpers: `core-data/src/main/kotlin/com/example/arweld/core/data/event/EventMappers.kt` handle `EventEntity` ↔ `Event` (enum name ↔ value conversions)

#### AuthRepository binding (core-data)

**Location:** `core-data/src/main/kotlin/com/example/arweld/core/data/di/DataModule.kt` → `RepositoryModule`

**Binds:**
- `AuthRepository` (domain) → `AuthRepositoryImpl` (mock login + SharedPreferences cache)

**Where to adjust auth providers:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
```

### ViewModel Injection

**Location:** Feature modules (e.g., `feature-home/src/main/kotlin/.../viewmodel/`)

**How to create a Hilt ViewModel:**
```kotlin
@HiltViewModel
class NewViewModel @Inject constructor(
    private val repository: SomeRepository
) : ViewModel() {
    // ViewModel logic
}
```

**How to use in Composable:**
```kotlin
@Composable
fun NewScreen(
    viewModel: NewViewModel = hiltViewModel()
) {
    // Use viewModel here
}
```

### Quick Reference: Where to Add DI Code

| What | Where |
|------|-------|
| Database provider | `core-data/di/DataModule.kt` → Add @Provides method |
| DAO provider | `core-data/di/DataModule.kt` → Add @Provides method |
| Repository binding | `core-data/di/DataModule.kt` (RepositoryModule) → Add @Binds method |
| Auth service binding | `core-data/di/DataModule.kt` (RepositoryModule) → Bind `AuthRepository` |
| New ViewModel | Feature module → Annotate with @HiltViewModel, use @Inject constructor |
| Activity injection | Annotate with @AndroidEntryPoint |
| Fragment injection | Annotate with @AndroidEntryPoint |

### Event sourcing map

- **Event models** → `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/Event.kt`
- **EventType enum** → `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/EventType.kt`
- **WorkItem reducer/state** → `core-domain/src/main/kotlin/com/example/arweld/core/domain/reducer/WorkItemStateReducer.kt`

---

## Where to Add New Functionality

### "I need to add a new EventType"

**Location:** `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/EventType.kt`
- Event data class lives alongside the enum: `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/Event.kt` (timestamp as milliseconds since epoch, actorRole: Role, optional payloadJson)

**Steps:**
1. Add new value to `EventType` enum:
   ```kotlin
   enum class EventType {
       WORK_CLAIMED,
       WORK_STARTED,
       // ... existing types
       NEW_CUSTOM_EVENT  // Add here
   }
   ```

2. Update `state/WorkItemState.kt` to handle new event in the `reduce(events)` state derivation logic

3. Add use case in appropriate feature module to create the event

**Example:** Adding "WORK_PAUSED" event:
- Add to `EventType` enum
- Update reducer: `WORK_STARTED + WORK_PAUSED → status = PAUSED`
- Add `PauseWorkUseCase.kt` in `feature:work/usecase/`

---

### State & reducers

- **Location:** `core/domain/src/main/kotlin/com/example/arweld/core/domain/state/WorkItemState.kt`
  - Contains `WorkStatus`, `QcStatus`, `WorkItemState` model, and the pure `reduce(events: List<Event>)` function.
- **Initial state:** `status = WorkStatus.NEW`, `qcStatus = QcStatus.NOT_STARTED`, `currentAssigneeId = null`, `lastEvent = null`.
- **When adding new EventType that affects state:**
  - Update `reduce` with the transition logic.
  - Add/adjust unit tests in `core-domain/src/test/kotlin/com/example/arweld/core/domain/state/WorkItemReducerTest.kt`.
  - Keep reducer deterministic: sort by `timestamp` then `id` before folding.

---

### "I need to add a new WorkItem filter for Supervisor"

**Location:** `feature/supervisor/src/main/kotlin/com/example/arweld/feature/supervisor/`

**Steps:**
1. **UI:** Add filter controls in `ui/WorkItemListScreen.kt`
   - Example: Add "Zone" dropdown, "Assignee" dropdown

2. **ViewModel:** Update `viewmodel/WorkItemListViewModel.kt`
   - Add state for filter values
   - Update query logic to filter by new criteria

3. **Data Layer:** If needed, add query method to `core/data/db/dao/WorkItemDao.kt`
   ```kotlin
   @Query("SELECT * FROM work_items WHERE zone = :zone AND status = :status")
   suspend fun findByZoneAndStatus(zone: String, status: String): List<WorkItemEntity>
   ```

4. **Repository:** Expose new query via `core/data/repository/WorkItemRepositoryImpl.kt`

---

### "I need to change QC evidence policy requirements"

**Location:** `core-domain/src/main/kotlin/com/example/arweld/core/domain/policy/QcEvidencePolicy.kt`

**What it does today (v1):** `check(workItemId, events, evidenceList)` ensures **at least one AR screenshot** and **at least one photo** exist **after the latest `QC_STARTED` event** for that WorkItem. Returns `QcEvidencePolicyResult.Ok` or `Failed(reasons)` with human-readable messages for UI/error handling.

**Call sites:** `PassQcUseCase` and `FailQcUseCase` load timeline events + evidence for a WorkItem, invoke `check(...)`, and throw `QcEvidencePolicyException` when `Failed` instead of emitting `QC_PASSED`/`QC_FAILED_REWORK`. The shared helper `ensureEvidencePolicySatisfied(...)` gathers evidence per event before gating the QC outcome, and UI surfaces `Failed.reasons` in banner/dialogs.

**To extend requirements:** add new entries to the requirement list inside `QcEvidencePolicy.check` (e.g., minimum video count or metadata validations) so new rules append to `missingReasons`. Keep reasons descriptive so the UI can present guidance without extra mapping.

---

### "I need to add a new screen to Assembler workflow"

**Location:** `feature/work/src/main/kotlin/com/example/arweld/feature/work/ui/`

**Steps:**
1. **Create Screen:** Add `NewScreen.kt` in `ui/` folder
   ```kotlin
   @Composable
   fun NewScreen(
       viewModel: NewViewModel = hiltViewModel(),
       onNavigateBack: () -> Unit
   ) {
       // Compose UI here
   }
   ```

2. **Create ViewModel:** Add `NewViewModel.kt` in `viewmodel/`
   ```kotlin
   @HiltViewModel
   class NewViewModel @Inject constructor(
       private val repository: WorkItemRepository
   ) : ViewModel() {
       // State and logic
   }
   ```

3. **Add Navigation Route:** Update `app/navigation/AppNavigation.kt`
   ```kotlin
   composable("new_screen/{workItemId}") { backStackEntry ->
       val workItemId = backStackEntry.arguments?.getString("workItemId")
       NewScreen(onNavigateBack = { navController.popBackStack() })
   }
   ```

4. **Link from Existing Screen:** Add button/link in `MyWorkScreen.kt` to navigate:
   ```kotlin
   Button(onClick = { navController.navigate("new_screen/$workItemId") }) {
       Text("Open New Screen")
   }
   ```

---

### "I need to add a new use case for QC workflow"

**Location:** `feature/qc/src/main/kotlin/com/example/arweld/feature/qc/usecase/`

**Steps:**
1. **Create Use Case Class:**
   ```kotlin
           class NewQcActionUseCase @Inject constructor(
               private val eventRepository: EventRepository,
               private val authRepository: AuthRepository
           ) {
               suspend operator fun invoke(workItemId: String, params: SomeParams) {
                   // Validate permissions
                   val user = authRepository.currentUser() ?: error("User must be logged in")
                   require(user.role == Role.QC) { "QC only" }

           // Create event
           val event = Event(
               id = generateId(),
               workItemId = workItemId,
               type = EventType.NEW_QC_ACTION,
               timestamp = System.currentTimeMillis(),
               actorId = user.id,
               actorRole = user.role,
               deviceId = getDeviceId(),
               payloadJson = "{\"data\":\"${'$'}{params.value}\"}"
           )
           eventRepository.insert(event)
       }
   }
   ```

2. **Wire into ViewModel:** Inject and call from `QcInspectionViewModel.kt`

3. **Add UI Trigger:** Add button in `QcInspectionScreen.kt`

---

### "I need to modify the Room database schema"

**Location:** `core/data/src/main/kotlin/com/example/arweld/core/data/db/`

**Steps:**
1. **Update Entity:** Modify `entity/WorkItemEntity.kt` (or relevant entity)
   ```kotlin
   @Entity(tableName = "work_items")
   data class WorkItemEntity(
       @PrimaryKey val id: String,
       val code: String,
       // ... existing fields
       val newField: String?  // Add new column
   )
   ```

2. **Increment Database Version:** Update `AppDatabase.kt`
   ```kotlin
   @Database(
       entities = [WorkItemEntity::class, /* ... */],
       version = 2,  // Increment version
       exportSchema = true
   )
   abstract class AppDatabase : RoomDatabase() {
       // ...
   }
   ```

3. **Add Migration:** Define migration from version 1 to 2
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("ALTER TABLE work_items ADD COLUMN newField TEXT")
       }
   }
   ```

4. **Register Migration:** In database builder (in `core-data` `DataModule.kt`)
   ```kotlin
   Room.databaseBuilder(context, AppDatabase::class.java, "arweld.db")
       .addMigrations(MIGRATION_1_2)
       .build()
   ```

5. **Update Mapper:** Modify `mapper/EntityMappers.kt` to handle new field

---

### "I need to add a new report to Supervisor"

**Location:** `feature/supervisor/src/main/kotlin/com/example/arweld/feature/supervisor/`

**Steps:**
1. **Create Use Case:** Add `usecase/GenerateNewReportUseCase.kt`
   ```kotlin
   class GenerateNewReportUseCase @Inject constructor(
       private val eventRepository: EventRepository
   ) {
       suspend operator fun invoke(dateRange: DateRange): NewReport {
           // Query events, aggregate data
           // Return report data
       }
   }
   ```

2. **Add to ViewModel:** Inject into `ReportsViewModel.kt`, expose as state

3. **Add UI Section:** Update `ui/ReportsScreen.kt` to display new report

**Example:** "Average QC Time per WorkItem" report:
- Query all QC_STARTED and QC_PASSED/FAILED events
- Calculate time delta for each WorkItem
- Display average in ReportsScreen

---

### "I need to add a new permission check"

**Location:** `core/domain/src/main/kotlin/com/example/arweld/core/domain/auth/` ✅ Updated in S1-04

**Steps:**
1. **Add Permission to Enum:** Edit `Permission.kt`
   ```kotlin
   enum class Permission {
       CLAIM_WORK,
       START_QC,
       PASS_QC,
       FAIL_QC,
       VIEW_ALL,
       NEW_PERMISSION  // Add your new permission here
   }
   ```

2. **Update RolePolicy:** Edit `RolePolicy.kt` to assign permission to roles
   ```kotlin
   private val rolePermissions: Map<Role, Set<Permission>> = mapOf(
       Role.ASSEMBLER to setOf(
           Permission.CLAIM_WORK,
           Permission.NEW_PERMISSION  // Grant to ASSEMBLER if needed
       ),
       // ... update other roles as needed
   )
   ```

3. **Check in Use Case:** Enforce in relevant use case
   ```kotlin
   require(user.role.hasPermission(Permission.NEW_PERMISSION)) {
       "User does not have permission"
   }
   ```

4. **Update UI:** Hide/disable buttons based on permission (via ViewModel)

5. **Add Tests:** Update `RolePolicyTest.kt` to verify permission behavior

---

## Naming Conventions

### Event Types
- Past tense: `WORK_CLAIMED`, `QC_STARTED`, `EVIDENCE_CAPTURED`
- Prefix by domain: `WORK_*`, `QC_*`, `AR_*`

### WorkItem Status
- Present/state-based: `IN_PROGRESS`, `READY_FOR_QC`, `PASSED`
- Avoid verbs in status names (use events for actions)

### Evidence Kinds
- Noun-based: `PHOTO`, `AR_SCREENSHOT`, `VIDEO`, `SENSOR_DATA`

### Use Case Classes
- Verb + noun + "UseCase": `ClaimWorkUseCase`, `PassQcUseCase`, `ExportReportUseCase`
- Single responsibility: one use case = one action

### Screen Files
- `ScreenName` + "Screen.kt": `HomeScreen.kt`, `QcInspectionScreen.kt`
- Compose functions: `@Composable fun QcInspectionScreen(...)`

### ViewModel Files
- `ScreenName` + "ViewModel.kt": `QcInspectionViewModel.kt`
- Annotate with `@HiltViewModel` if using Hilt

---

## Navigation

**Implementation Status:** ✅ Implemented in S1-03

### Overview

ARWeld uses **Compose Navigation** with a single-activity architecture. Navigation is organized into two conceptual graphs:
- **AuthGraph:** Splash → Login
- **MainGraph:** Home → WorkItemSummary → Timeline

### NavHost Location

**File:** `app/src/main/kotlin/com/example/arweld/navigation/AppNavigation.kt`

The main NavHost is defined in `AppNavigation` composable, which is called from `MainActivity`.

### Route Constants

**File:** `app/src/main/kotlin/com/example/arweld/navigation/Routes.kt`

All route constants are centralized:
```kotlin
object Routes {
    // Auth Graph
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_LOGIN = "login"

    // Main Graph
    const val ROUTE_HOME = "home"
    const val ROUTE_ASSEMBLER_QUEUE = "assembler_queue"
    const val ROUTE_WORK_ITEM_SUMMARY = "work_item_summary"
    const val ROUTE_TIMELINE = "timeline"
}
```

### Screen Locations

**Auth Screens (app module):**
- `app/src/main/kotlin/com/example/arweld/ui/auth/SplashScreen.kt`
  - Start destination; renders centered branding and auto-redirects to Login on launch
  - Uses `LaunchedEffect` for navigation with `popUpTo` to prevent back
- `app/src/main/kotlin/com/example/arweld/ui/auth/LoginScreen.kt`
  - Mock authentication with role selection buttons
  - Navigates to Home with `popUpTo` to clear auth stack

**Home Screen (feature-home module):**
- `feature-home/src/main/kotlin/com/example/arweld/feature/home/ui/HomeScreen.kt`
  - Accepts `onNavigateToWorkSummary`, `onNavigateToTimeline`, `onNavigateToAssemblerQueue`, and `onNavigateToScan` callbacks
  - Called from `HomeRoute` wrapper in `AppNavigation.kt`
  - Does not directly depend on navigation library

**Work Screens (feature-work module):**
- `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/WorkItemSummaryScreen.kt`
  - Stub implementation for S1-03
- `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/TimelineScreen.kt`
  - Stub implementation for S1-21 showing centered `Text("Timeline stub")`
- `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/AssemblerQueueScreen.kt`
  - S2-07 queue screen grouped by IN_PROGRESS/READY_FOR_QC/REWORK_REQUIRED; uses `AssemblerQueueViewModel`
  - ViewModel: `feature-work/src/main/kotlin/com/example/arweld/feature/work/viewmodel/AssemblerQueueViewModel.kt` injects `AuthRepository` + `WorkRepository.getMyQueue(userId)`
  - Navigation wrapper: `app/src/main/kotlin/com/example/arweld/ui/work/AssemblerQueueRoute.kt` via `ROUTE_ASSEMBLER_QUEUE`

### Navigation Flow

**Current Implementation (S1-03):**
1. **App Launch:** MainActivity → NavHost with startDestination = ROUTE_SPLASH
2. **Splash:** Immediately navigates to LOGIN (clears splash from back stack)
3. **Login:** User selects role → navigates to HOME (clears login from back stack)
4. **Home:** User can navigate to WORK_ITEM_SUMMARY or TIMELINE
5. **Back from Home:** Does NOT return to Login/Splash (popUpTo prevents this)
6. **Back from WorkItemSummary/Timeline:** Returns to Home

**Planned Enhancements (Sprint 2+):**
- Deep linking for work items: `"work_item/{workItemId}"`
- Conditional navigation based on user role
- Bottom navigation or drawer for main screens
- AR view integration: `"ar_view/{workItemId}"`

### Adding New Destinations

**Step 1: Define Route Constant**

Edit `app/src/main/kotlin/com/example/arweld/navigation/Routes.kt`:
```kotlin
const val NEW_SCREEN = "new_screen"
// Or with parameter:
const val NEW_SCREEN_WITH_ID = "new_screen/{itemId}"
```

**Step 2: Create Screen Composable**

In appropriate feature module (e.g., `feature-work`):
```kotlin
@Composable
fun NewScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    // Screen implementation
}
```

**Step 3: Add to NavHost**

Edit `app/src/main/kotlin/com/example/arweld/navigation/AppNavigation.kt`:
```kotlin
composable(Routes.NEW_SCREEN) {
    NewScreen()
}

// Or with parameter:
composable(
    route = Routes.NEW_SCREEN_WITH_ID,
    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString("itemId")
    NewScreen(itemId = itemId)
}
```

**Step 4: Navigate to New Screen**

From another screen:
```kotlin
navController.navigate(Routes.NEW_SCREEN)
// Or with parameter:
navController.navigate("new_screen/$itemId")
```

### Route Naming Convention

- **Lowercase with underscores:** `"home"`, `"my_work"`, `"qc_start?workItemId={workItemId}"`
- **Path parameters:** `{workItemId}`, `{userId}`
- **AuthGraph routes:** `"splash"`, `"login"`
- **MainGraph routes:** Feature-specific names like `"work_item_summary"`, `"timeline"`

### Current and Upcoming Routes

```kotlin
// Sprint 2+
"my_work"                        → MyWorkScreen (Assembler)
"work_item/{workItemId}"         → WorkItemSummaryScreen (with data)
"scan"                           → ScannerScreen
"ar_view?workItemId={workItemId}"→ ARViewScreen (Assembler/QC AR surface)

// Sprint 3+
"qc_queue"                       → QcQueueScreen
"qc_start?workItemId={workItemId}"→ QcStartScreen (QC inspection entry)

// Sprint 4+
"supervisor_dashboard"           → SupervisorDashboardScreen
"work_item_list"                 → WorkItemListScreen
"work_item_detail/{workItemId}"  → WorkItemDetailScreen (Supervisor)
"reports"                        → ReportsScreen
"export"                         → ExportScreen
```

---

## Testing Conventions

### Unit Tests (core:domain)
**Location:** `core/domain/src/test/kotlin/com/example/arweld/core/domain/`

**Naming:** `ClassNameTest.kt` (e.g., `WorkItemReducerTest.kt`)

**Example:**
```kotlin
class WorkItemReducerTest {
    @Test
    fun `reduce events from NEW to APPROVED`() {
        val events = listOf(
            Event(type = EventType.WORK_CLAIMED, /* ... */),
            Event(type = EventType.WORK_READY_FOR_QC, /* ... */),
            Event(type = EventType.QC_STARTED, /* ... */),
            Event(type = EventType.QC_PASSED, /* ... */)
        )
        val state = reduce(events)
        assertEquals(WorkStatus.APPROVED, state.status)
    }
}
```

### Repository Tests (core:data)
**Location:** `core/data/src/test/kotlin/com/example/arweld/core/data/`

**Use In-Memory Room Database:**
```kotlin
@Before
fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
}
```

### UI Tests (feature modules)
**Location:** `feature/*/src/androidTest/kotlin/`

**Compose Testing:**
```kotlin
@Test
fun qcInspectionScreen_passButtonDisabled_whenNoEvidence() {
    composeTestRule.setContent {
        QcInspectionScreen(/* ... */)
    }
    composeTestRule.onNodeWithText("Pass").assertIsNotEnabled()
}
```

---

## Build and Dependencies

### Adding a Dependency to a Module

**Edit:** `module/build.gradle.kts`

**Common Dependencies:**
```kotlin
// Core modules
implementation(project(":core:domain"))
implementation(project(":core:data"))
implementation(project(":core:auth"))

// AndroidX
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.viewmodel.ktx)

// Compose
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)

// Room
implementation(libs.androidx.room.runtime)
ksp(libs.androidx.room.compiler)

// Hilt
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)

// CameraX
implementation(libs.androidx.camera.camera2)
implementation(libs.androidx.camera.lifecycle)
implementation(libs.androidx.camera.view)

// ARCore
implementation(libs.arcore)

// Testing
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
```

**Version Catalog:** Defined in `gradle/libs.versions.toml` (recommended)

---

## Gradle Tasks

**Build Commands:**
```bash
# Build entire project
./gradlew build

# Build specific module
./gradlew :core:domain:build

# Run unit tests
./gradlew test

# Run Android instrumentation tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Install debug APK
./gradlew installDebug
```

---

## Common Workflows

### Adding a New Feature Module

1. **Create Directory:**
   ```bash
   mkdir -p feature/newfeature/src/main/kotlin/com/example/arweld/feature/newfeature
   mkdir -p feature/newfeature/src/test/kotlin/com/example/arweld/feature/newfeature
   ```

2. **Create `build.gradle.kts`:**
   ```kotlin
   plugins {
       alias(libs.plugins.android.library)
       alias(libs.plugins.kotlin.android)
       alias(libs.plugins.ksp)
   }

   android {
       namespace = "com.example.arweld.feature.newfeature"
       compileSdk = 36
       // ... (copy from another feature module)
   }

   dependencies {
       implementation(project(":core:domain"))
       implementation(project(":core:data"))
       // ... other deps
   }
   ```

3. **Add to `settings.gradle.kts`:**
   ```kotlin
   include(":feature:newfeature")
   ```

4. **Add to App Dependencies:** In `app/build.gradle.kts`:
   ```kotlin
   implementation(project(":feature:newfeature"))
   ```

5. **Sync Project:** In Android Studio, click "Sync Now"

### Creating a New DAO Query

1. **Add Query Method:** In `core/data/db/dao/WorkItemDao.kt`:
   ```kotlin
   @Query("SELECT * FROM work_items WHERE status = :status ORDER BY createdAt DESC")
   fun observeByStatus(status: String): Flow<List<WorkItemEntity>>
   ```

2. **Expose via Repository:** In `WorkItemRepositoryImpl.kt`:
   ```kotlin
   override fun getByStatus(status: WorkStatus): Flow<List<WorkItem>> {
       return workItemDao.observeByStatus(status.name)
           .map { entities -> entities.map { it.toDomain() } }
   }
   ```

3. **Use in ViewModel:**
   ```kotlin
   val workItems: StateFlow<List<WorkItem>> = workItemRepository
       .getByStatus(WorkStatus.IN_PROGRESS)
       .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
   ```

---

## Quick Reference

### "Where is...?"

| What | Location |
|------|----------|
| Domain models (WorkItem, Event, etc.) | `core-domain/src/main/kotlin/com/example/arweld/core/domain/` |
| Role enum | `core-domain/src/main/kotlin/com/example/arweld/core/domain/model/Role.kt` ✅ S1-04 |
| Permission enum | `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/Permission.kt` ✅ S1-04 |
| RolePolicy (permission checking) | `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/RolePolicy.kt` ✅ S1-04 |
| RolePolicy tests | `core-domain/src/test/kotlin/com/example/arweld/core/domain/auth/RolePolicyTest.kt` ✅ S1-24 |
| AuthRepository interface | `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/AuthRepository.kt` ✅ S1-16 |
| AuthRepositoryImpl (mock) | `core-data/src/main/kotlin/com/example/arweld/core/data/auth/AuthRepositoryImpl.kt` ✅ S1-16 |
| Database entities | `core/data/db/entity/` |
| DAOs | `core/data/db/dao/` |
| Repositories | `core/data/repository/` |
| Use cases | `feature/*/usecase/` |
| Screens (Compose) | `feature/*/ui/` |
| ViewModels | `feature/*/viewmodel/` |
| Navigation routes | `app/navigation/AppNavigation.kt` |
| DI setup | `app/di/AppModule.kt` |
| Reducer logic | `core/domain/reducer/` |
| QC evidence policies | `core/domain/policy/` |
| AR rendering | `feature/arview/ar/` and `rendering/` |
| Pose + CameraIntrinsics domain types | `core-domain/src/main/kotlin/com/example/arweld/core/domain/spatial/PoseTypes.kt` |
| Export logic | `feature/supervisor/export/` |

### "How do I...?"

| Task | See Section |
|------|-------------|
| Add new EventType | "I need to add a new EventType" |
| Add database column | "I need to modify the Room database schema" |
| Add screen to workflow | "I need to add a new screen to Assembler workflow" |
| Add Supervisor filter | "I need to add a new WorkItem filter for Supervisor" |
| Change QC evidence rules | "I need to change QC evidence policy requirements" |
| Create new use case | "I need to add a new use case for QC workflow" |
| Add permission check | "I need to add a new permission check" |
| Add new report | "I need to add a new report to Supervisor" |

---

## Tips for New Developers

1. **Start with Reading:**
   - Read `PROJECT_OVERVIEW.md` to understand the problem and solution
   - Read `stage.md` to see the implementation roadmap
   - Read `MODULES.md` to understand code organization

2. **Understand the Event-Driven Model:**
   - Events are immutable and append-only
   - State is derived, not stored
   - Every action creates an Event

3. **Follow the Dependency Flow:**
   - UI (Screen) → ViewModel → Use Case → Repository → DAO → Database
   - Never skip layers (e.g., don't call DAO directly from ViewModel)

4. **Test as You Go:**
   - Write unit tests for reducers and policies (core:domain)
   - Write integration tests for repositories (core:data)
   - Write UI tests for critical flows (feature modules)

5. **Keep Modules Focused:**
   - If a file feels out of place, it might belong in a different module
   - Feature modules should be self-contained (don't share UI between features)

6. **Use the Type System:**
   - Kotlin's type safety prevents many bugs
   - Use sealed classes for results (`sealed class Result`)
   - Use enums for fixed sets (`enum class EventType`)

7. **Leverage Flow for Reactivity:**
   - Database queries return `Flow<List<T>>`
   - ViewModels expose `StateFlow` or `SharedFlow`
   - UI collects flows and recomposes automatically

---

## Getting Help

**For Architecture Questions:**
- Refer to `PROJECT_OVERVIEW.md` → Core Principles
- Check `MODULES.md` for module responsibilities

**For Implementation Questions:**
- Check this file (`FILE_OVERVIEW.md`) for "Where to add..."
- Look at `stage.md` for sprint-specific guidance
- Review existing code in similar feature module

**For Android/Kotlin Questions:**
- Official Android documentation: https://developer.android.com
- Kotlin documentation: https://kotlinlang.org/docs

**For AR Questions:**
- ARCore documentation: https://developers.google.com/ar
- Sceneform (archived): https://github.com/google-ar/sceneform-android-sdk

---

**This guide is a living document.** As the project evolves, update this file to reflect new patterns, modules, and conventions.
