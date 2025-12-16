# ARWeld — Module Structure

This document describes the planned module architecture for ARWeld. The project follows a **multi-module** approach for better separation of concerns, testability, and build performance.

## Module Dependency Graph

```
app
 ├─> feature:home
 ├─> feature:work (assembler)
 ├─> feature:scanner
 ├─> feature:qc
 ├─> feature:supervisor
 ├─> feature:arview
 ├─> core:auth
 ├─> core:data
 └─> core:domain

feature:* modules
 ├─> core:data
 └─> core:domain

core:data
 └─> core:domain

core:domain
 └─ (no dependencies, pure domain logic)
```

**Dependency Rules:**
- `app` depends on all feature modules and core modules
- Feature modules depend only on core modules (never on other features)
- `core:data` depends on `core:domain`
- `core:domain` has no dependencies (pure Kotlin, no Android framework)

---

## Module Descriptions

### app

**Status:** ✅ Implemented (S1-03)

**Description:**
The Android application module. Entry point for the app, hosts navigation, and wires up dependency injection.

**Key Responsibilities:**
- Application class initialization (@HiltAndroidApp)
- Navigation host setup (Compose Navigation) ✅ Implemented in S1-03
- Dependency injection configuration via Hilt
- Auth screens (Splash, Login) ✅ Implemented in S1-03; Login uses AuthRepository.loginMock via LoginViewModel for role-based mock sign-in with four role buttons (Assembler/QC/Supervisor/Director)
- Global app configuration (theme, error handling, analytics)

**Dependencies:**
- All feature modules
- All core modules

**DI Configuration:**
- **Framework:** Hilt (configured in S1-02)
- **Application class:** `ArWeldApplication.kt` annotated with `@HiltAndroidApp`
- **MainActivity:** Annotated with `@AndroidEntryPoint`
- **Graph entry point:** Application-level Hilt component provides singleton instances

**Navigation Structure (S1-03):**
- **NavHost:** Defined in `navigation/AppNavigation.kt`
- **Routes:** Defined in `navigation/Routes.kt` (`ROUTE_SPLASH`, `ROUTE_LOGIN`, `ROUTE_HOME`, `ROUTE_WORK_ITEM_SUMMARY`, `ROUTE_TIMELINE`)
- **AuthGraph:** Splash → Login
- **MainGraph:** Home → WorkItemSummary → Timeline
- **Auth Screens:**
  - `ui/auth/SplashScreen.kt` — Entry point/start destination, auto-redirects to Login
  - `ui/auth/LoginScreen.kt` — Role selection (Assembler/QC/Supervisor/Director) using `AuthRepository.loginMock` via `LoginViewModel`
- **Navigation Flow:**
  - Splash → Login → Home (with popUpTo to prevent back to auth)
  - Home → WorkItemSummary or Timeline

**Key Files:**
- `ArWeldApplication.kt` — Application class with @HiltAndroidApp
- `MainActivity.kt` — Single-activity architecture with @AndroidEntryPoint and NavHost
- `navigation/AppNavigation.kt` — ✅ Navigation graph and routes
- `navigation/Routes.kt` — ✅ Route constants
- `ui/auth/SplashScreen.kt` — ✅ Splash screen
- `ui/auth/LoginScreen.kt` — ✅ Login screen with role buttons
- `ui/auth/LoginViewModel.kt` — ✅ Injects AuthRepository.loginMock for mock role sign-in

**Notes:**
- Thin layer; most logic lives in feature or core modules
- Provides "assembly" of the app from reusable components
- Hilt DI graph root is established here
- Navigation uses Compose Navigation for single-activity architecture

---

### core:domain

**Status:** ✅ Implemented (S1-01, S1-05 domain models, S1-07 evidence model, S1-08 reducer, S1-16 AuthRepository contract)

**Description:**
Pure domain logic with no Android dependencies. Contains business models, use cases, reducers, and policies. **No Hilt/DI code inside this module** — it remains a pure Kotlin library.

**Key Responsibilities:**
- Define enums: `EventType`, `WorkItemType`, `EvidenceKind`, `Role`, `Permission`, **WorkStatus**, **QcStatus**
- Define domain models: `WorkItem` (typed by `WorkItemType`), event log entries (`Event` + `EventType`), `Evidence`, `Role`, `User`
- Event log contract:
  - `Event` keeps `actorRole: Role`, `timestamp` as milliseconds since epoch, and optional `payloadJson` with event-specific JSON
  - `EventType` enumerates workflow milestones (claim, QC, evidence, alignment, rework)
- Business logic:
  - `reduce(events)` — Derives `WorkItemState` from the ordered event list (pure, deterministic)
  - `RolePolicy` — ✅ Implemented in S1-04: Defines which roles can perform which actions via `hasPermission(role, permission)` and extension function `Role.hasPermission(permission)`
  - `QcEvidencePolicy` — Validates evidence requirements for QC decisions
- Repository contracts:
  - `WorkRepository` — Domain-facing interface for fetching WorkItems by code and deriving WorkItemState/queues from the event log
  - `EventRepository` — Domain-facing interface for appending events (single/batch) and querying timelines by WorkItem
  - `AuthRepository` — Domain-facing authentication/session contract with `loginMock(role)`, `currentUser()`, and `logout()`
- Use case interfaces (implementations may live in core:data or feature modules)
- Assembler actions: `ClaimWorkUseCase`, `StartWorkUseCase`, `MarkReadyForQcUseCase` to emit domain events for queue actions
  - `ResolveWorkItemByCodeUseCase` (S2-04) — resolves scanned codes to `WorkItem`

**Dependencies:**
- None (pure Kotlin, no DI framework)

**DI Configuration:**
- **None** — This module is pure Kotlin with no DI annotations
- Repository interfaces defined here are bound to implementations in core-data via Hilt

**Key Files/Packages:**
- `domain/work/` — Work tracking domain models
  - `WorkItemType.kt` — ✅ S1-05: PART, NODE, OPERATION
  - `WorkItem.kt` — ✅ S1-05: Base model (id, projectId, optional zoneId, type, optional code)
- `event/` — Event log taxonomy and helpers
  - `Event.kt` — Immutable event record with actorRole, device, and payloadJson
  - `EventType.kt` — Enum for workflow milestones (claim, QC, evidence, alignment, rework)
  - `EventExtensions.kt` (optional) — Helper functions such as `isQcEvent()`
- `model/` — Data classes for other domain entities
  - `Event.kt`
  - `Evidence.kt`
  - `User.kt`, `Role.kt`
- `state/` — ✅ Added in S1-08: Derived WorkItem state
  - `WorkItemState.kt` — `WorkStatus`, `QcStatus`, `WorkItemState`, and `reduce(events)`
- `evidence/` — ✅ S1-07: QC evidence domain models
  - `EvidenceKind.kt` — PHOTO, AR_SCREENSHOT, VIDEO, MEASUREMENT
  - `Evidence.kt` — Evidence metadata (id, eventId, uri, sha256, metaJson, createdAt)
- `auth/` — ✅ Added in S1-04: Authentication and authorization models
  - `Permission.kt` — Enum of permissions (CLAIM_WORK, START_QC, PASS_QC, FAIL_QC, VIEW_ALL)
  - `RolePolicy.kt` — Central policy for role-based permissions with extension function
- `policy/` — Business rules
  - `QcEvidencePolicy.kt`
- `validation/` — Domain validation logic
  - `ValidationResult.kt`

**Notes:**
- 100% unit testable (no Android framework)
- Shared truth for all other modules
- Changes here impact entire app; design carefully

---

### core:data

**Status:** ✅ Implemented (S1-02 - partial, DAOs and repositories only) — 📌 Updated with S1-10 Room entities, S1-14 EventRepository, S1-16 AuthRepositoryImpl

**Description:**
Data layer providing local storage, repositories, and data access abstractions. **Hilt DI modules configured here** to provide database and repository instances.

**Key Responsibilities:**
- Room database setup (`AppDatabase`)
- Entity definitions (Room schema) — **WorkItemEntity, EventEntity, EvidenceEntity, UserEntity, SyncQueueEntity** (S1-10)
- DAOs (Data Access Objects) for CRUD operations: `WorkItemDao`, `EventDao`, `EvidenceDao`, `UserDao`, `SyncQueueDao`
- Repository implementations:
  - `WorkItemRepository` ✅ Implemented
  - `EventRepository` ✅ Implemented (Room-backed with centralized EventEntity ↔ Event mappers)
  - `EvidenceRepository` ✅ Implemented for metadata-only storage (no file I/O yet)
  - `AuthRepositoryImpl` ✅ S1-16: mock login with in-memory + SharedPreferences caching
  - `WorkRepositoryImpl` ✅ S1-13: derives WorkItemState and queues using Room + reducer
  - `ResolveWorkItemByCodeUseCaseImpl` ✅ S2-04: delegates to `WorkRepository.getWorkItemByCode` for scanner flows
  - `ClaimWorkUseCaseImpl`, `StartWorkUseCaseImpl`, `MarkReadyForQcUseCaseImpl` ✅ S2-08/S2-09/S2-10: append `WORK_CLAIMED`/`WORK_STARTED`/`WORK_READY_FOR_QC` events with current actor/device metadata
  - `SyncQueueRepository` 📋 Planned
- Seed infrastructure: `DbSeedInitializer` inserts `SeedWorkItems` and `SeedUsers` into Room when the tables are empty (MVP mock data)
- File management for evidence (photos, AR screenshots)
- Evidence storage metadata (URIs, SHA-256 hashes) aligned to `core-domain` `Evidence`
- Offline queue management (`SyncManager`)
- Data mappers (Entity ↔ Domain model)
- Persist Event log using domain `Event`/`EventType` (including actorRole and payloadJson)
- **Hilt DI modules** that bind repositories to implementations

**Dependencies:**
- `core:domain` (for domain models)

**DI Configuration:**
- **Module:** `DataModule` (`core-data/src/main/kotlin/.../di/DataModule.kt`)
  - Provides `AppDatabase` singleton via Room.databaseBuilder()
  - Provides DAOs (`WorkItemDao`, `EventDao`, `EvidenceDao`, `UserDao`, `SyncQueueDao`) from database instance
  - **Module:** `RepositoryModule` (`core-data/src/main/kotlin/.../di/DataModule.kt`)
    - Binds `WorkItemRepository` → `WorkItemRepositoryImpl`
    - Binds `EventRepository` → `EventRepositoryImpl`
    - Binds `EvidenceRepository` → `EvidenceRepositoryImpl` (metadata-only in S1)
    - Binds `AuthRepository` → `AuthRepositoryImpl` (mock login backed by seeded users + SharedPreferences cache)
    - Binds `WorkRepository` (core-domain) → `WorkRepositoryImpl` (core-data)
- **Scope:** `@Singleton` — All repositories and database are application-scoped
- **Where to add new bindings:** Add @Binds or @Provides methods to these modules

**Key Files/Packages:**
- `db/` — Room database
  - `AppDatabase.kt`
  - `entity/` — Room entities
    - `WorkItemEntity.kt`
    - `EventEntity.kt`
    - `EvidenceEntity.kt`
    - `UserEntity.kt`
    - `SyncQueueEntity.kt`
  - `dao/` — Data Access Objects
    - `WorkItemDao.kt`
    - `EventDao.kt`
    - `EvidenceDao.kt`
    - `SyncQueueDao.kt`
- `event/` — EventEntity ↔ Event mappers shared across repositories
  - `EventMappers.kt`
- `repository/` — Repository implementations
  - `WorkItemRepositoryImpl.kt`
  - `EventRepositoryImpl.kt`
  - `EvidenceRepositoryImpl.kt`
- `seed/` — Mock data seeds
  - `SeedWorkItems.kt`
  - `SeedUsers.kt`
  - `DbSeedInitializer.kt`
- `file/` — File storage management
  - `EvidenceFileManager.kt` — Save/load photos and AR screenshots
  - `ChecksumCalculator.kt` — SHA-256 hashing
- `sync/` — Offline queue
  - `SyncManager.kt` — Enqueue changes, process sync queue
- `mapper/` — Entity ↔ Domain conversions

**Notes:**
- This is the **persistence layer** for the app
- All feature modules interact with data via repositories (never directly with DAOs)
- EvidenceRepositoryImpl currently persists only metadata in Room; actual photo/AR/video files will be handled by a later file
  manager component.
- Repositories expose Flow or suspend functions for reactive/async data
- DAOs cover core queries: WorkItem lookup by id/code, ordered event timelines and per-actor history, evidence by event, user roster retrieval, and pending sync queue fetches ordered by creation time

---

### core:auth

**Status:** ✅ Implemented (S1-02 - basic MVP version)

**Description:**
User authentication and role management. For MVP, uses local user storage (no server). **Hilt DI module configured here** to provide auth services.

**Key Responsibilities:**
- Store current logged-in user in-memory (MVP: no persistence)
- Provide `AuthRepository` with:
  - `getCurrentUser(): User?` ✅ Implemented
  - `login(userId: String)` ✅ Implemented (stub users)
  - `logout()` ✅ Implemented
  - `hasPermission(permission: Permission): Boolean` 📋 Planned
- Role-based permission checking (delegates to `RolePolicy` in core:domain)

**Dependencies:**
- `core:domain` (for User, Role models)

**DI Configuration:**
- **Module:** `AuthModule` (`core-auth/src/main/kotlin/.../di/AuthModule.kt`)
  - Binds `AuthRepository` → `LocalAuthRepository`
- **Scope:** `@Singleton` — Single AuthRepository instance per app
- **Implementation:** `LocalAuthRepository` with hardcoded stub users (assembler1, qc1, supervisor1, director1) ✅ Updated in S1-04
- **Where to add auth providers:** Add @Binds or @Provides methods to AuthModule

**Key Files:**
- `AuthRepository.kt` — Main auth interface
- `LocalAuthRepository.kt` — ✅ MVP implementation (in-memory, stub users)
- `di/AuthModule.kt` — ✅ Hilt DI module
- `SessionManager.kt` — 📋 Planned
- `PermissionChecker.kt` — 📋 Planned

**Notes:**
- MVP uses hardcoded or seeded local users (no password, just user selection)
- Future versions may add:
  - Server-based auth (OAuth, LDAP, SSO)
  - Biometric authentication
  - Session expiry

---

### feature:home

**Status:** ✅ Implemented (S1-19 — role-based tiles)

**Description:**
Home screen with greeting and role-specific navigation tiles powered by domain `User` + `Role`.

**Key Responsibilities:**
- Display user's name and role ✅ Implemented
- Show tiles based on current user role ✅ Implemented
  - **Assembler:** "My Work Queue", "Timeline"
  - **QC Inspector:** "QC Queue", "Timeline"
  - **Supervisor/Director:** "Shop overview", "Timeline"
- Navigation to WorkItemSummary and Timeline via callbacks ✅ Implemented

**Dependencies:**
- `core:domain` (for Role, User)
- Called from app module navigation wrapper; no direct navigation dependency

**Navigation:**
- Accepts `onOpenWorkSummary` and `onOpenTimeline` callbacks supplied by app module
- Separation preserved: feature module stays navigation-agnostic

**Key Files:**
- `ui/HomeScreen.kt` — Compose UI exposing role-based tiles and callback wiring

---

### feature:work

**Status:** 🚧 Sprint 2 (S2-07 Assembler queue wiring, plus S1-20/21 stubs)

**Description:**
Assembler workflows: "My Work" queue, claim work, start work, mark ready for QC. Sprint 2 introduces the AssemblerQueue screen grouped by status.

**Key Responsibilities:**
- Host screens for assembler workflows (to be implemented in Sprint 2+)
- Provide WorkItem detail and timeline views (currently stubbed)
- Surface AR entry points when a WorkItem has associated spatial data (future)

**Dependencies:**
- `core:domain` (WorkItem, Event models)
- `core:data` (WorkItemRepository, EventRepository)
- `core:auth` (get current Assembler)

**Navigation (S1-20/S1-21/S2-07/S2-11):**
- WorkItemSummary shows computed state + assembler actions (claim/start/ready for QC) and is reachable via `ROUTE_WORK_ITEM_SUMMARY` from ScanCode or AssemblerQueue
- Timeline stub composable lives in `feature-work` and is wired to `ROUTE_TIMELINE`
- AssemblerQueueScreen lists the current user's queue grouped by IN_PROGRESS/READY_FOR_QC/REWORK_REQUIRED and is reachable via `ROUTE_ASSEMBLER_QUEUE`

**Key Files/Packages:**
- `ui/` — Screens
  - `WorkItemSummaryScreen.kt` — Shows WorkItem id/code/type, derived state, and role-aware assembler actions
  - `TimelineScreen.kt` — ✅ S1-21 stub showing `Text("Timeline stub")`
  - `AssemblerQueueScreen.kt` — 🚧 S2-07 grouped queue view with clickable items
- `viewmodel/` — `AssemblerQueueViewModel.kt` derives grouped lists from `WorkRepository.getMyQueue`
  - `QcQueueViewModel.kt` — Sprint 3 view model that loads READY_FOR_QC/QC_IN_PROGRESS items from `WorkRepository.getQcQueue()`
- `app` wrapper — `ui/work/WorkItemSummaryRoute.kt` forwards `workItemId` into the Hilt ViewModel and renders feature UI
  - `ui/work/AssemblerQueueRoute.kt` wires Hilt VM + navigation to WorkItemSummary

**Notes:**
- "feature:work" may also be called "feature:assembler"
- Additional screens/viewmodels/use-cases arrive in Sprint 2

---

### feature:scanner

**Status:** ✅ Implemented (S2-03 — preview + barcode/QR decoding + ScanCode screen; S2-04 — resolve code → WorkItemSummary)

**Description:**
Barcode/QR code scanning with CameraX preview surface exposed to Compose. The scanner module owns camera setup, permission handling, ML Kit decoding, and exposes the ScanCode screen so navigation modules can remain thin.

**Key Responsibilities:**
- CameraX preview with lifecycle-aware binding
- ML Kit barcode/QR decoding via ImageAnalysis analyzer with duplicate suppression
- Present ScanCode screen that surfaces live preview, shows the last decoded value, and exposes navigation callbacks
- Route scanned codes through `ResolveWorkItemByCodeUseCase` and navigate to `WorkItemSummary` when a match is found

**Dependencies:**
- `core:domain` (WorkItem, ResolveWorkItemByCodeUseCase contract)
- `core:data` (WorkItemRepository / ResolveWorkItemByCodeUseCaseImpl)

**Key Files:**
- `ui/ScanCodeScreen.kt` — Public composable combining `ScannerPreview`, the last decoded code, and a "Continue" action plus error snackbar
- `ui/ScannerPreview.kt` — Composable wrapping `PreviewView` with permission handling and decoded-code callback
- `camera/CameraPreviewController.kt` — CameraX setup and lifecycle binding for preview and analysis
- `camera/BarcodeAnalyzer.kt` — ML Kit analyzer that emits deduplicated barcode/QR values
- `app/src/main/kotlin/com/example/arweld/ui/scanner/ScanCodeRoute.kt` — App wrapper that injects the resolver use case, handles errors, and navigates
- `app/src/main/kotlin/com/example/arweld/ui/scanner/ScanCodeViewModel.kt` — Hilt VM that calls `ResolveWorkItemByCodeUseCase` and exposes snackbar errors

**Notes:**
- Supports QR codes, barcodes (Code 128, Code 39, etc.) once decoding is added
- NFC support can be added later (requires NFC-enabled device)
- Keep UI simple: just camera preview + scan indicator

---

### feature:qc

**Status:** 📋 Planned (Sprint 3)

**Description:**
QC inspector workflows: queue, start inspection, capture evidence, checklist, pass/fail.

**Key Responsibilities:**
- Display QC queue (WorkItems with status READY_FOR_QC)
- Start QC inspection (creates QC_STARTED event)
- Capture evidence:
  - Take photos (CameraX)
  - Capture AR screenshots (from ARView)
  - Store with metadata and checksums
- Display checklist (3–8 inspection points)
- Enforce QC evidence policy before allowing PASS/FAIL
- Create QC_PASSED or QC_FAILED_REWORK event with notes, checklist, reason codes

**Dependencies:**
- `core:domain` (Event, Evidence, QcEvidencePolicy)
- `core:data` (EventRepository, EvidenceRepository)
- `core:auth` (get current QC Inspector)

**Key Files/Packages:**
- `ui/`
  - `QcQueueScreen.kt` — List of items awaiting QC
  - `QcInspectionScreen.kt` — Main QC workflow screen
  - `EvidenceGalleryScreen.kt` — View captured evidence
  - `ChecklistWidget.kt` — Reusable checklist component
- `viewmodel/`
  - `QcQueueViewModel.kt`
  - `QcInspectionViewModel.kt`
- `usecase/`
  - `StartQcInspectionUseCase.kt`
  - `CapturePhotoEvidenceUseCase.kt`
  - `PassQcUseCase.kt`
  - `FailQcUseCase.kt`
- `camera/`
  - `PhotoCaptureManager.kt` — CameraX photo capture

**Notes:**
- Most complex feature module (inspection + evidence + policy enforcement)
- QC screen should be single-page with integrated checklist, evidence thumbnails, and decision buttons for efficiency

---

### feature:supervisor

**Status:** 📋 Planned (Sprint 4)

**Description:**
Supervisor workflows: dashboard, WorkItem list/filters, detail view with timeline/evidence, reports, export.

**Key Responsibilities:**
- Display real-time dashboard with KPIs:
  - In progress, QC queue, passed, failed counts
  - QC backlog wait times
  - Active users and their current work
- List all WorkItems with filters (status, zone, assignee, date)
- Search WorkItems by code or description
- Drill down into WorkItem detail:
  - Full event timeline
  - Evidence viewer (photos + AR screenshots with metadata)
- Generate reports:
  - Top rejection reasons
  - Most problematic nodes
  - Completion rates by user
- Export functionality:
  - JSON/CSV export with date range
  - Evidence file package with checksums

**Dependencies:**
- `core:domain` (all models)
- `core:data` (all repositories)
- `core:auth` (verify supervisor role)

**Key Files/Packages:**
- `ui/`
  - `SupervisorDashboardScreen.kt` — Main dashboard with KPIs
  - `WorkItemListScreen.kt` — Filterable list of all WorkItems
  - `WorkItemDetailScreen.kt` — Full detail with timeline + evidence
  - `ReportsScreen.kt` — Aggregated reports
  - `ExportScreen.kt` — Export center
- `viewmodel/`
  - `SupervisorDashboardViewModel.kt`
  - `WorkItemListViewModel.kt`
  - `WorkItemDetailViewModel.kt`
  - `ReportsViewModel.kt`
  - `ExportViewModel.kt`
- `usecase/`
  - `CalculateKpisUseCase.kt`
  - `ExportReportUseCase.kt`
  - `GenerateEvidencePackageUseCase.kt`
- `export/`
  - `JsonExporter.kt` — Export to JSON
  - `CsvExporter.kt` — Export to CSV
  - `ChecksumFileGenerator.kt` — Generate SHA-256 checksum file

**Notes:**
- Largest feature module by screen count
- Dashboard should use cached/aggregated queries for performance (consider Room views)
- Export should be background task (coroutine) with progress indicator

---

### feature:arview

**Status:** 🚧 In progress (Sprint 2 foundation; hardened in Sprint 6)

**Description:**
Augmented reality visualization for alignment and inspection. Sprint 2 introduces the baseline `ARViewScreen` with lifecycle-aware hosting for the AR surface and an ARCore-backed session lifecycle; later sprints add overlays and evidence capture.

**Key Responsibilities:**
- Host AR rendering surface and lifecycle hooks via `ARViewScreen` + `ARViewController`
- Manage ARCore Session creation/resume/pause through `ARCoreSessionManager` (Pixel 9 target)
- Surface ARCore availability errors to the UI overlay for quick troubleshooting
- Run marker detection pipeline (pluggable) to return markerId + four ordered corners per frame (S2-16 stub in place)
- Retrieve camera intrinsics from `Camera.imageIntrinsics` and adapt them into domain `CameraIntrinsics`
- Load GLB assets from `src/main/assets/models` via `render/AndroidFilamentModelLoader.kt` using Filament gltfio
- Estimate marker pose in world coordinates using PnP/homography (`pose/MarkerPoseEstimator.kt`), composing with the tracked camera pose from ARCore (S2-17)
- Marker-based zone alignment: compute `T_world_zone = T_world_marker * T_marker_zone` via `ZoneAligner` and apply it to the model root when a known marker is seen (S2-18)
- Manual fallback alignment: collect 3 hitTest world points from user taps, pair them with hardcoded model landmarks, and solve `T_world_model` via `RigidTransformSolver` (S2-19)
- Track AR tracking quality (camera state + marker recency + feature points) and expose `TrackingStatus` to UI overlays
- Capture AR screenshots with metadata (planned)
- Emit AR alignment audit events (`AR_ALIGNMENT_SET`) after marker or manual alignment succeeds (S2-21)

**Dependencies:**
- `core:domain` (Evidence, EvidenceMetadata)
- `core:data` (EvidenceRepository, EventRepository)

**Key Files/Packages:**
- `ui/arview/`
  - `ARViewScreen.kt` — Compose screen hosting AR surface + lifecycle observer, tracking quality indicator, and error overlay
- `arcore/`
  - `ARViewController.kt` — Provides AR rendering surface and forwards lifecycle callbacks
  - `ARSceneRenderer.kt` — ARCore → Filament bridge; renders the test model using either a fixed pose (pre-alignment) or the computed zone/world pose once marker alignment succeeds
  - `ARCoreSessionManager.kt` — Lazily creates/configures ARCore Session and handles resume/pause/destroy
  - `ARViewLifecycleHost.kt` — Bridges Android lifecycle events to the AR controller
  - `ArCoreMappers.kt` — Converts ARCore `Pose`/`CameraIntrinsics` into domain spatial types for pose estimation
- `alignment/`
  - `ArAlignmentPayload.kt` — JSON payload schema for AR alignment events (method, marker, point count, pose summary)
  - `AlignmentEventLogger.kt` — Builds and appends `AR_ALIGNMENT_SET` events via `EventRepository`
  - `RigidTransformSolver.kt` — Quaternion-based solver (Horn method) to recover `T_world_model` from 3D-3D correspondences
  - `ManualAlignmentState.kt` — UI-facing state for tap collection progress and status messages
- `marker/`
  - `MarkerDetector.kt` — Interface + `DetectedMarker` result with ordered corners; operates on ARCore frames
  - `StubMarkerDetector.kt` — Placeholder implementation returning no detections (S2-16)
- `pose/`
  - `MarkerPoseEstimator.kt` — Computes T_world_marker via planar PnP (homography) using camera intrinsics and AR camera pose
- `tracking/`
  - `TrackingQuality.kt` — Defines `TrackingQuality` + `TrackingStatus` surfaced from AR controller heuristics
- `zone/`
  - `ZoneRegistry.kt` — Hardcoded marker → `ZoneTransform` mapping for marker-based zone alignment (S2-18)
  - `ZoneAligner.kt` — Computes `T_world_zone` from detected marker poses and registry lookups and hands it to the renderer
- `render/`
  - `ModelLoader.kt`, `AndroidFilamentModelLoader.kt` — GLB loader returning Filament assets; entrypoint for `models/test_node.glb`

**Planned Additions:** (kept from earlier roadmap)
- Model rendering pipeline with SceneRenderer/ModelCache
- Marker detection and alignment calculator
- AR screenshot capture flow

**Notes:**
- ARCore requires device support; gracefully handle non-AR devices (show error or fallback)
- AR is **optional** for workflow (app works without AR if no nodeId)
- Performance critical: target 30 FPS on Pixel 6+ devices
- Sprint 6 adds multi-marker and performance hardening

---

## Module Implementation Status

| Module | Status | Sprint |
|--------|--------|--------|
| `app` | ✅ Exists (basic) | Sprint 1 (expand) |
| `core:domain` | 📋 Planned | Sprint 1 |
| `core:data` | 📋 Planned | Sprint 1 |
| `core:auth` | 📋 Planned | Sprint 1 |
| `feature:home` | 📋 Planned | Sprint 1 |
| `feature:work` | 📋 Planned | Sprint 2 |
| `feature:scanner` | ✅ Implemented | Sprint 2 |
| `feature:arview` | 🚧 In progress (ARViewScreen + lifecycle) | Sprint 2 |
| `feature:qc` | 📋 Planned | Sprint 3 |
| `feature:supervisor` | 📋 Planned | Sprint 4 |

**Legend:**
- ✅ Exists — Module is created with basic structure
- 📋 Planned — Module does not exist yet, will be created in specified sprint

---

## Creating a New Module

When creating a new module (e.g., during Sprint 1–6 implementation):

1. **Add to `settings.gradle.kts`:**
   ```kotlin
   include(":core:domain")
   include(":core:data")
   // etc.
   ```

2. **Create module directory structure:**
   ```
   core/domain/
     ├── build.gradle.kts
     └── src/
         ├── main/
         │   └── kotlin/
         │       └── com/example/arweld/core/domain/
         └── test/
             └── kotlin/
                 └── com/example/arweld/core/domain/
   ```

3. **Configure `build.gradle.kts`:**
   - For `core:domain`: Pure Kotlin library
   - For `core:data`: Android library with Room dependencies
   - For `feature:*`: Android library with Compose dependencies

4. **Add dependencies in dependent modules:**
   ```kotlin
   // In app/build.gradle.kts:
   dependencies {
       implementation(project(":core:domain"))
       implementation(project(":core:data"))
       implementation(project(":feature:home"))
       // etc.
   }
   ```

5. **Follow package naming convention:**
   - `com.example.arweld.core.domain.*`
   - `com.example.arweld.core.data.*`
   - `com.example.arweld.feature.home.*`

---

## Module Best Practices

**1. Dependency Direction:**
- Always depend "inward": feature → core, data → domain
- Never: core → feature, domain → data

**2. Single Responsibility:**
- Each module has a clear, single purpose
- If a module does too much, split it

**3. Interface Segregation:**
- core:domain defines interfaces (e.g., `WorkItemRepository`)
- core:data provides implementations
- Features depend on interfaces, not implementations (enables testing)

**4. Testing:**
- core:domain: 100% unit testable (pure Kotlin)
- core:data: Unit test repositories with in-memory Room DB
- feature:*: UI tests with Compose test framework or Espresso

**5. Build Performance:**
- Modularization allows parallel builds
- Clean builds are faster (only rebuild changed modules)
- Keep modules focused to minimize cross-module dependencies

---

## Future Module Considerations

If project grows beyond MVP, consider adding:

- **core:ui** — Shared UI components (buttons, cards, theme)
- **core:network** — API client for server sync (post-MVP)
- **core:analytics** — Logging, crash reporting, telemetry
- **feature:settings** — App settings, user preferences
- **feature:reports** — Extract from supervisor if reporting grows complex
- **feature:sync** — Dedicated sync UI and conflict resolution

For MVP, the 10 modules listed above are sufficient.

---

## Questions?

For implementation guidance:
- See `stage.md` for sprint-by-sprint implementation order
- See `FILE_OVERVIEW.md` for specific file locations and conventions
