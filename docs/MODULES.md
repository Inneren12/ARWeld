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
- Auth screens (Splash, Login) ✅ Implemented in S1-03
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
- **Routes:** Defined in `navigation/Routes.kt`
- **AuthGraph:** Splash → Login
- **MainGraph:** Home → WorkItemSummary → Timeline
- **Auth Screens:**
  - `ui/auth/SplashScreen.kt` — Entry point, auto-redirects to Login
  - `ui/auth/LoginScreen.kt` — Role selection (Assembler/QC/Supervisor)
- **Navigation Flow:**
  - Splash → Login → Home (with popUpTo to prevent back to auth)
  - Home → WorkItemSummary or Timeline

**Key Files:**
- `ArWeldApplication.kt` — Application class with @HiltAndroidApp
- `MainActivity.kt` — Single-activity architecture with @AndroidEntryPoint and NavHost
- `navigation/AppNavigation.kt` — ✅ Navigation graph and routes
- `navigation/Routes.kt` — ✅ Route constants
- `ui/auth/SplashScreen.kt` — ✅ Splash screen
- `ui/auth/LoginScreen.kt` — ✅ Login screen

**Notes:**
- Thin layer; most logic lives in feature or core modules
- Provides "assembly" of the app from reusable components
- Hilt DI graph root is established here
- Navigation uses Compose Navigation for single-activity architecture

---

### core:domain

**Status:** ✅ Implemented (S1-01, S1-05 domain models, S1-07 evidence model, S1-08 reducer)

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
- Use case interfaces (implementations may live in core:data or feature modules)

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

**Status:** ✅ Implemented (S1-02 - partial, DAOs and repositories only) — 📌 Updated with S1-10 Room entities and S1-14 EventRepository

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
  - `WorkRepositoryImpl` ✅ S1-13: derives WorkItemState and queues using Room + reducer
  - `SyncQueueRepository` 📋 Planned
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

**Status:** ✅ Implemented (S1-03 - with navigation callbacks)

**Description:**
Home screen with role-based navigation tiles. **Demonstrates Hilt ViewModel injection** working end-to-end.

**Key Responsibilities:**
- Display user's name and role ✅ Implemented
- Show work item count from database ✅ Implemented (demonstrates DI working)
- Navigation to WorkItemSummary and Timeline ✅ Implemented in S1-03 (stub buttons)
- Show tiles based on current user role (📋 Planned for Sprint 2):
  - **Assembler:** "My Work", "Scan New Part"
  - **QC Inspector:** "QC Queue", "Scan to Inspect"
  - **Supervisor:** "Dashboard", "All Work Items", "Export"

**Dependencies:**
- `core:domain` (for Role, User, WorkItem)
- `core:data` (for WorkItemRepository)
- `core:auth` (for AuthRepository)

**DI Configuration:**
- **ViewModel:** `HomeViewModel` annotated with `@HiltViewModel`
- **Constructor injection:** Receives `WorkItemRepository` and `AuthRepository` via `@Inject`
- **Screen:** Uses `hiltViewModel()` to obtain ViewModel instance
- **Demonstrates:** Full Hilt DI flow from app → feature → core modules

**Navigation (S1-03):**
- Accepts `onNavigateToWorkSummary` and `onNavigateToTimeline` callbacks
- Called from `HomeRoute` wrapper in app module's `AppNavigation.kt`
- Demonstrates proper separation: feature module doesn't depend on navigation library

**Key Files:**
- `ui/HomeScreen.kt` — ✅ Compose UI with hiltViewModel() and navigation callbacks
- `viewmodel/HomeViewModel.kt` — ✅ @HiltViewModel with injected repositories
- `viewmodel/HomeUiState.kt` — ✅ Sealed class for UI state management

**Notes:**
- Simple navigation hub demonstrating DI integration
- UI adapts based on role (example of role-based UI)
- S1-03 adds navigation buttons for demo purposes

---

### feature:work

**Status:** ✅ Partially Implemented (S1-03 - stub screens only)

**Description:**
Assembler workflows: "My Work" queue, claim work, start work, mark ready for QC.

**Key Responsibilities:**
- Display list of WorkItems assigned to current Assembler (📋 Sprint 2)
- Filter by status: IN_PROGRESS, READY_FOR_QC, REWORK_REQUIRED (📋 Sprint 2)
- Actions (📋 Sprint 2):
  - Claim work (creates WORK_CLAIMED event)
  - Start work (creates WORK_STARTED event)
  - Mark ready for QC (creates WORK_READY_FOR_QC event)
- WorkItemSummary screen ✅ Stub implemented in S1-03
- Timeline screen ✅ Stub implemented in S1-03
- Navigate to ARView if WorkItem has nodeId (📋 Sprint 2)

**Dependencies:**
- `core:domain` (WorkItem, Event models)
- `core:data` (WorkItemRepository, EventRepository)
- `core:auth` (get current Assembler)

**Navigation (S1-03):**
- Screens are accessible from Home via navigation buttons
- Full integration planned for Sprint 2

**Key Files/Packages:**
- `ui/` — Screens
  - `WorkScreen.kt` — ✅ Placeholder from S1-01
  - `WorkItemSummaryScreen.kt` — ✅ Stub implemented in S1-03
  - `TimelineScreen.kt` — ✅ Stub implemented in S1-03
  - `MyWorkScreen.kt` — 📋 Planned for Sprint 2
- `viewmodel/` — 📋 Planned for Sprint 2
  - `MyWorkViewModel.kt`
  - `WorkItemSummaryViewModel.kt`
- `usecase/` — 📋 Planned for Sprint 2
  - `ClaimWorkUseCase.kt`
  - `StartWorkUseCase.kt`
  - `MarkReadyForQcUseCase.kt`

**Notes:**
- "feature:work" may also be called "feature:assembler"
- WorkItemSummaryScreen and TimelineScreen are currently stubs for navigation demo
- Full implementation with business logic planned for Sprint 2

---

### feature:scanner

**Status:** 📋 Planned (Sprint 2)

**Description:**
Barcode/QR code scanning with camera.

**Key Responsibilities:**
- CameraX preview with code detection overlay
- Integrate MLKit Barcode Scanner or ZXing
- Resolve scanned code to WorkItem (via `ResolveWorkItemUseCase`)
- Navigate to WorkItemSummary if found, or show "Not Found" dialog

**Dependencies:**
- `core:domain` (WorkItem)
- `core:data` (WorkItemRepository)

**Key Files:**
- `ScannerScreen.kt` — Camera preview + code detection
- `ScannerViewModel.kt` — Handles scan results
- `ResolveWorkItemUseCase.kt` — Looks up WorkItem by code
- `camera/CameraManager.kt` — CameraX setup

**Notes:**
- Supports QR codes, barcodes (Code 128, Code 39, etc.)
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

**Status:** 📋 Planned (Sprint 2, hardened in Sprint 6)

**Description:**
Augmented reality visualization for alignment and inspection.

**Key Responsibilities:**
- Load 3D models from assets or remote storage
- Render AR overlay using ARCore + Sceneform/Filament
- Alignment methods:
  - Marker-based (ArUco/AprilTag detection)
  - Manual 3-point alignment
- Track alignment quality in real-time
- Display quality indicator (green/yellow/red)
- Capture AR screenshot with metadata (markers, tracking state, alignment quality)
- Optionally log AR_ALIGNMENT_SET event
- Multi-marker refinement (Sprint 6)
- Performance optimizations (Sprint 6): culling, caching, FPS monitoring

**Dependencies:**
- `core:domain` (Evidence, EvidenceMetadata)
- `core:data` (EvidenceRepository, EventRepository)

**Key Files/Packages:**
- `ui/`
  - `ArViewScreen.kt` — Main AR view
  - `AlignmentIndicatorWidget.kt` — Quality indicator overlay
- `viewmodel/`
  - `ArViewViewModel.kt`
- `ar/`
  - `ArSessionManager.kt` — ARCore session lifecycle
  - `ModelLoader.kt` — Load and parse 3D models (glTF, OBJ)
  - `MarkerDetector.kt` — Detect ArUco/AprilTag markers
  - `AlignmentCalculator.kt` — Compute alignment quality score
  - `ScreenshotCapture.kt` — Capture ARCore frame buffer
- `rendering/`
  - `SceneRenderer.kt` — Render 3D model overlay
  - `ModelCache.kt` — Cache parsed models in memory

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
| `feature:scanner` | 📋 Planned | Sprint 2 |
| `feature:arview` | 📋 Planned | Sprint 2 |
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
