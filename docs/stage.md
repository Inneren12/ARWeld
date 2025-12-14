# AR Shop-Floor MVP — Stage Roadmap

This document defines the 6-sprint roadmap for the AR-assisted QA system MVP. Each sprint is approximately 2 weeks.

## Cross-Cutting Principles

Before diving into sprints, understand these foundational rules that apply across all development:

### Event Log as Single Source of Truth
- Every state transition is recorded as an immutable Event
- WorkItem state is **derived** from Events via a deterministic reducer
- Never directly mutate WorkItem status; always create an Event

### QC Policy Gate
- QC inspectors **cannot** mark PASS/FAIL without mandatory evidence
- Minimum requirements (configurable via policy):
  - At least 1 AR screenshot captured after QC_STARTED
  - At least 1 photo (or as defined by QcEvidencePolicy)
- System enforces this at use-case level

### Unified WorkItem Model
- WorkItem represents any trackable unit: part, assembly node, operation
- Barcodes, QR codes, NFC tags are just identifiers that resolve to a WorkItem
- Single model supports multiple workflows

### Offline-First Architecture
- All Events and Evidence stored locally in Room database
- File-based evidence stored on device with SHA-256 checksums
- Network independence: system must function without connectivity
- Sync/export capabilities are additive, not required for core functionality

---

## Sprint 1 (Weeks 1–2): Data and Roles Foundation

**Result:** App has users/roles, local Room storage for WorkItem, Events, Evidence, timeline for a single WorkItem derived from events, and basic navigation by role.

---

### **S1-01: Create Gradle Modules** ✅ COMPLETED

**Implementation Date:** 2025-12-14

**Goal:** Create a minimal but correct Gradle multi-module structure for the project.

**What Was Implemented:**

1. **Version Catalog Updates** (`gradle/libs.versions.toml`):
   - Added Jetpack Compose dependencies (BOM, UI, Material3)
   - Added Room database dependencies with KSP compiler
   - Added Hilt dependency injection framework
   - Added Kotlinx Serialization and Coroutines
   - Added all necessary AndroidX Lifecycle and Navigation components

2. **Core Modules Created:**
   - **core-domain**: Pure Kotlin module with domain models:
     - `WorkItem`, `WorkItemType`, `WorkItemStatus`
     - `Event`, `EventType`
     - `Evidence`, `EvidenceKind`
     - `User`, `Role`
     - All models use Kotlinx Serialization
   - **core-data**: Android library with repository interfaces:
     - `WorkItemRepository`
     - `EventRepository`
     - `EvidenceRepository`
     - Dependencies: Room (ready for DAO implementation in Sprint 1.3)
   - **core-auth**: Android library with authentication interface:
     - `AuthRepository` (stub for local user management)

3. **Feature Modules Created (Scaffolded):**
   - **feature-home**: Home screen with Compose UI placeholder
   - **feature-work**: Assembler workflow screens (placeholder)
   - **feature-scanner**: Barcode/QR scanner screen (placeholder)
   - **feature-arview**: AR visualization screen (placeholder)

4. **App Module Configuration:**
   - Updated to Android Library with Compose support
   - Wired all module dependencies correctly
   - Created `MainActivity.kt` with Compose setup
   - Created minimal Material3 theme (`Theme.kt`)
   - Updated `AndroidManifest.xml` with launcher activity

5. **Module Registration:**
   - All 8 modules registered in `settings.gradle.kts`
   - Dependency graph follows clean architecture principles:
     - app → feature-* → core-* → core-domain
     - No circular dependencies

**Acceptance Criteria Status:**
- ✅ All modules created and registered
- ✅ Dependencies wired correctly
- ✅ Module structure follows docs/MODULES.md specification
- ✅ Minimal scaffolding code compiles (verified configuration)
- ⏳ Full build pending (network-dependent Gradle download deferred)
- ⏳ Documentation updates in progress

**Next Steps (Remaining Sprint 1 Tasks):**
- Implement Room database entities and DAOs (Task 1.3)
- Implement state reducer logic (Task 1.2 completion)
- Create basic navigation and DI setup (Task 1.4)
- Add unit tests for domain models and reducer (Task 1.5)

---

### 1.1 Project Structure and Modules

**Planned Modules:**
- `app` — Android application entry point, navigation host, DI wiring
- `core:domain` — Domain models, business logic, reducers, policies
- `core:data` — Room database, DAOs, repositories, offline queue
- `core:auth` — User authentication and role management
- `feature:home` — Home screen with role-based navigation
- `feature:work` — Assembler workflows (claiming, starting work)

**DI Choice:** Hilt (recommended) or Koin for dependency injection.

**Artifact:** Project structure created with skeleton screens for each module.

### 1.2 Domain Models (core:domain)

Create the following domain models:

**Role and Permissions:**
```kotlin
enum class Role {
    ASSEMBLER,
    QC_INSPECTOR,
    SUPERVISOR
}

data class Permission(
    val action: String,
    val resource: String
)

object RolePolicy {
    // Defines which roles can perform which actions
    // Examples:
    // - ASSEMBLER can CLAIM, START_WORK, MARK_READY_FOR_QC
    // - QC can START_QC, PASS, FAIL
    // - SUPERVISOR can VIEW_ALL, EXPORT
}
```

**WorkItem:**
```kotlin
data class WorkItem(
    val id: String,
    val code: String,              // Barcode/QR/NFC identifier
    val type: WorkItemType,
    val description: String,
    val zone: String?,
    val nodeId: String?,           // Links to AR model node
    val createdAt: Instant
)

enum class WorkItemType {
    PART,
    ASSEMBLY,
    OPERATION
}
```

**Event:**
```kotlin
data class Event(
    val id: String,
    val workItemId: String,
    val type: EventType,
    val actorId: String,           // User who performed action
    val deviceId: String,
    val timestamp: Instant,
    val payload: Map<String, Any>  // Event-specific data
)

enum class EventType {
    WORK_CLAIMED,
    WORK_STARTED,
    WORK_READY_FOR_QC,
    QC_STARTED,
    QC_PASSED,
    QC_FAILED,
    REWORK_STARTED,
    AR_ALIGNMENT_SET,
    EVIDENCE_CAPTURED
}
```

**Evidence:**
```kotlin
data class Evidence(
    val id: String,
    val eventId: String,
    val workItemId: String,
    val kind: EvidenceKind,
    val filePath: String,
    val fileHash: String,          // SHA-256 for integrity
    val metadata: EvidenceMetadata,
    val capturedAt: Instant
)

enum class EvidenceKind {
    PHOTO,
    AR_SCREENSHOT,
    VIDEO,
    SENSOR_DATA
}

data class EvidenceMetadata(
    val markerIds: List<String>?,
    val trackingState: String?,
    val alignmentQuality: Float?,
    val deviceOrientation: String?,
    val lightingCondition: String?
)
```

**WorkItemState (Derived):**
```kotlin
data class WorkItemState(
    val workItemId: String,
    val status: WorkItemStatus,
    val currentAssignee: String?,
    val lastUpdated: Instant,
    val qcAttempts: Int,
    val hasRequiredEvidence: Boolean
)

enum class WorkItemStatus {
    NEW,
    CLAIMED,
    IN_PROGRESS,
    READY_FOR_QC,
    QC_IN_PROGRESS,
    PASSED,
    FAILED,
    REWORK_REQUIRED
}
```

**WorkItemStateReducer:**
- Pure function: `reduce(events: List<Event>): WorkItemState`
- Deterministic: same events always produce same state
- Unit-testable: crucial for correctness
- Handles state transitions based on EventType sequence

### 1.3 Room Database (core:data)

**Tables:**

```kotlin
@Entity(tableName = "work_items")
data class WorkItemEntity(
    @PrimaryKey val id: String,
    val code: String,
    val type: String,
    val description: String,
    val zone: String?,
    val nodeId: String?,
    val createdAt: Long
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val workItemId: String,
    val type: String,
    val actorId: String,
    val deviceId: String,
    val timestamp: Long,
    val payload: String              // JSON serialized
)

@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val workItemId: String,
    val kind: String,
    val filePath: String,
    val fileHash: String,
    val metadata: String,            // JSON serialized
    val capturedAt: Long
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val role: String,
    val displayName: String
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,          // "event" or "evidence"
    val entityId: String,
    val operation: String,           // "insert", "update"
    val status: String,              // "pending", "failed", "synced"
    val retryCount: Int,
    val lastAttempt: Long?,
    val error: String?
)
```

**DAOs:**

```kotlin
@Dao
interface WorkItemDao {
    @Query("SELECT * FROM work_items WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): WorkItemEntity?

    @Query("SELECT * FROM work_items WHERE id = :id")
    suspend fun getById(id: String): WorkItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkItemEntity)
}

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity): Long

    @Query("SELECT * FROM events WHERE workItemId = :workItemId ORDER BY timestamp ASC")
    fun observeByWorkItem(workItemId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestEvents(limit: Int): List<EventEntity>
}

@Dao
interface EvidenceDao {
    @Insert
    suspend fun insert(evidence: EvidenceEntity): Long

    @Query("SELECT * FROM evidence WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: String): List<EvidenceEntity>

    @Query("SELECT * FROM evidence WHERE workItemId = :workItemId")
    suspend fun getByWorkItemId(workItemId: String): List<EvidenceEntity>
}
```

**Repositories:**

- `WorkItemRepository` — CRUD for WorkItems, code lookup
- `EventRepository` — Insert events, query by workItem, derive state
- `EvidenceRepository` — Store evidence files, manage checksums
- `AuthRepository` — Local user management

**Artifact:** Local database initialized with seed demo data (5–10 WorkItems, 2–3 users per role).

### 1.4 Navigation and Minimal Screens

**Navigation Flow:**
1. **Splash** → Check if user is logged in
2. **Login** → Select user (MVP: local user picker, no password)
3. **Home** → Role-based tiles:
   - Assembler: "My Work", "Scan New Part"
   - QC: "QC Queue", "Scan to Inspect"
   - Supervisor: "Dashboard", "All Work Items"
4. **Scan** → Camera view with code recognition
5. **WorkItemSummary** → Shows derived status, action buttons
6. **Timeline** → Event history for a WorkItem

**Acceptance Examples:**
- User can log in as Assembler, QC, or Supervisor
- User can navigate to Home and see role-appropriate options
- User can open a WorkItem by test code and see an empty timeline
- User can create a test WORK_CLAIMED event via debug button and see it appear in timeline

### 1.5 Tests

**Unit Tests for Reducer:**
Test state transitions:
- NEW → WORK_CLAIMED → IN_PROGRESS → READY_FOR_QC → QC_STARTED → PASSED
- NEW → WORK_CLAIMED → IN_PROGRESS → READY_FOR_QC → QC_STARTED → FAILED → REWORK_REQUIRED
- Invalid transitions should not change state

**Unit Tests for RolePolicy:**
- Assembler can CLAIM, START, MARK_READY_FOR_QC
- Assembler **cannot** PASS or FAIL
- QC can START_QC, PASS, FAIL
- QC **cannot** CLAIM work (Assembler-only)
- Supervisor can VIEW_ALL but not perform production actions

**Artifact:** Test coverage ≥80% for core:domain reducers and policies.

---

## Sprint 2 (Weeks 3–4): Assembler Workflow + AR v1

**Result:** Assembler can scan, claim, mark ready for QC, and use AR screen v1 as a validation tool.

### 2.1 Scanner (feature:scanner or core:scanner)

**Implementation:**
- Use CameraX for camera preview
- Integrate MLKit Barcode Scanner or ZXing for code recognition
- Support QR codes, barcodes, NFC (if device supports)

**ResolveWorkItemUseCase:**
```kotlin
class ResolveWorkItemUseCase(
    private val workItemRepository: WorkItemRepository
) {
    suspend operator fun invoke(code: String): ResolveResult {
        val workItem = workItemRepository.findByCode(code)
        return when {
            workItem != null -> ResolveResult.Found(workItem)
            else -> ResolveResult.NotFound(code)
        }
    }
}

sealed class ResolveResult {
    data class Found(val workItem: WorkItem) : ResolveResult()
    data class NotFound(val code: String) : ResolveResult()
}
```

**Screen Flow:**
1. User taps "Scan"
2. Camera opens with code recognition
3. When code detected:
   - If found → Navigate to WorkItemSummary
   - If not found → Show "Not Found" dialog (for MVP, do not allow creation)

**Acceptance Criteria:**
- 9 out of 10 scans successfully read code on target device (e.g., Pixel 6+)
- Scan-to-result latency < 2 seconds in good lighting
- Clear error message for unrecognized codes

### 2.2 Assembler Queue (feature:assembler or feature:work)

**"My Work" Screen:**
- Lists WorkItems where current user is assignee
- Filters:
  - IN_PROGRESS — Currently working
  - READY_FOR_QC — Handed over, awaiting inspection
  - REWORK_REQUIRED — QC rejected, needs fixes
- Sorting: by last updated (most recent first)

**Actions:**

**ClaimWork:**
```kotlin
class ClaimWorkUseCase(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(workItemId: String) {
        val currentUser = authRepository.getCurrentUser()
        val event = Event(
            id = generateId(),
            workItemId = workItemId,
            type = EventType.WORK_CLAIMED,
            actorId = currentUser.id,
            deviceId = getDeviceId(),
            timestamp = Clock.System.now(),
            payload = emptyMap()
        )
        eventRepository.insert(event)
    }
}
```

**StartWork:**
```kotlin
// Creates WORK_STARTED event
// May be merged with ClaimWork in UI (single "Start Work" button)
```

**MarkReadyForQC:**
```kotlin
// Creates WORK_READY_FOR_QC event
// Changes WorkItem status to READY_FOR_QC
// Removes from Assembler's active queue
```

**Acceptance Criteria:**
- Every action immediately produces an Event
- Event instantly reflects in queue (via Flow/LiveData)
- WorkItem disappears from "In Progress" when marked ready for QC
- Timeline shows who claimed, when started, when marked ready

### 2.3 WorkItemSummary (shared)

**Displayed Information:**
- WorkItem code, description, type
- Current derived status (from reducer)
- Current assignee (if any)
- Last action summary: "QC inspection started by Jane Doe at 14:23"
- Timeline button → Navigate to full event timeline

**Role-Specific Action Buttons:**
- **Assembler:**
  - "Claim" (if NEW)
  - "Start Work" (if CLAIMED)
  - "Mark Ready for QC" (if IN_PROGRESS)
  - "Open AR" (if WorkItem has nodeId)
- **QC:**
  - "Start Inspection" (if READY_FOR_QC)
  - "Open AR" (if nodeId present)
  - "View Evidence" (always visible)
- **Supervisor:**
  - "View Timeline" (always)
  - "View Evidence" (always)
  - No production actions

**Acceptance:**
- Buttons only appear when action is valid for current user + WorkItem state
- Tapping action creates Event and updates UI instantly

### 2.4 ARView v1 (feature:arview)

**Assembler Mode:**
- Load 3D node model from assets (initially mock/demo models)
- Alignment methods:
  - **Marker-based:** ArUco/AprilTag for quick alignment
  - **Manual 3-point:** User taps 3 known points on real object
- Quality indicator:
  - Green: Good alignment, model fits well
  - Yellow: Moderate alignment, some drift
  - Red: Poor alignment, re-align recommended

**AR_ALIGNMENT_SET Event (Optional):**
```kotlin
// When user completes alignment, optionally log:
Event(
    type = EventType.AR_ALIGNMENT_SET,
    payload = mapOf(
        "markerIds" to listOf("marker_A1"),
        "alignmentQuality" to 0.87,
        "method" to "marker"
    )
)
```

**Features:**
- Display overlay model over camera feed (ARCore/Sceneform)
- Simple visualization: edges, key features highlighted
- No complex interactions yet (Sprint 6 adds refinements)

**Acceptance Criteria:**
- On Pixel-class device with ARCore support, operator can align and inspect in 30–60 seconds
- Quality indicator updates in real-time based on tracking state
- User can exit AR and return to WorkItemSummary without losing context

**Artifact:** Video demo of Assembler workflow: Scan → Claim → AR align → Mark ready for QC.

---

## Sprint 3 (Weeks 5–6): QC Workflow with Evidence Gate

**Result:** QC inspector can accept/reject WorkItems and cannot do so without required evidence.

### 3.1 QC Queue (feature:qc)

**QC Queue Screen:**
- Lists all WorkItems with status READY_FOR_QC
- Sorted by waiting time (earliest WORK_READY_FOR_QC event first)
- Shows:
  - WorkItem code
  - Description
  - Who marked it ready (Assembler name)
  - Wait time (e.g., "Waiting 1h 23m")

**Acceptance:**
- QC sees items appear immediately when Assembler marks ready
- Queue updates in real-time (Flow from database)

### 3.2 Start QC Inspection

**StartQcInspection Use Case:**
```kotlin
class StartQcInspectionUseCase(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(workItemId: String) {
        val inspector = authRepository.getCurrentUser()
        require(inspector.role == Role.QC_INSPECTOR) {
            "Only QC inspectors can start QC"
        }

        val event = Event(
            id = generateId(),
            workItemId = workItemId,
            type = EventType.QC_STARTED,
            actorId = inspector.id,
            deviceId = getDeviceId(),
            timestamp = Clock.System.now(),
            payload = mapOf("inspectorName" to inspector.displayName)
        )
        eventRepository.insert(event)
    }
}
```

**Screen Flow:**
1. QC taps WorkItem in queue
2. WorkItemSummary shows "Start Inspection" button
3. Tap → Creates QC_STARTED event
4. UI transitions to QC inspection mode:
   - Evidence capture section
   - AR button (if nodeId present)
   - Checklist (Sprint 3.5)
   - Pass/Fail buttons (initially disabled)

### 3.3 Evidence Capture

**Photo Capture:**
- CameraX photo capture
- Save to app-specific storage: `files/evidence/photos/{workItemId}/{timestamp}.jpg`
- Compute SHA-256 hash
- Create Evidence entity:

```kotlin
data class PhotoEvidence(
    val id: String,
    val eventId: String,        // Links to QC_STARTED or custom EVIDENCE_CAPTURED event
    val workItemId: String,
    val kind: EvidenceKind.PHOTO,
    val filePath: String,
    val fileHash: String,
    val metadata: EvidenceMetadata(
        markerIds = null,
        trackingState = null,
        alignmentQuality = null,
        deviceOrientation = getOrientation(),
        lightingCondition = estimateLighting()  // Optional
    ),
    val capturedAt: Instant.now()
)
```

**AR Screenshot:**
- Capture current ARCore frame buffer
- Save as PNG: `files/evidence/ar_screenshots/{workItemId}/{timestamp}.png`
- Include AR-specific metadata:

```kotlin
data class ArScreenshotMetadata(
    val markerIds: List<String>,
    val trackingState: String,          // "TRACKING", "PAUSED", "STOPPED"
    val alignmentQuality: Float,        // 0.0 to 1.0
    val deviceOrientation: String,
    val nodeId: String?
)
```

**Evidence Gallery:**
- Show thumbnails of captured evidence
- Tap to view full-screen
- Delete button (creates compensating event if already logged)

**Acceptance:**
- QC can capture 3–5 photos during inspection
- QC can open AR, inspect, and capture screenshot
- Evidence appears immediately in gallery
- File hash verification works (test with file corruption scenario)

### 3.4 QC Policy Gate (core:domain)

**QcEvidencePolicy v1:**

```kotlin
object QcEvidencePolicy {
    data class Requirements(
        val minArScreenshots: Int = 1,
        val minPhotos: Int = 1,
        val requireAfterQcStarted: Boolean = true
    )

    fun validateEvidence(
        evidence: List<Evidence>,
        events: List<Event>,
        requirements: Requirements = Requirements()
    ): ValidationResult {
        val qcStartedEvent = events.firstOrNull { it.type == EventType.QC_STARTED }
            ?: return ValidationResult.NoQcStarted

        val evidenceAfterQc = evidence.filter {
            it.capturedAt >= qcStartedEvent.timestamp
        }

        val arScreenshots = evidenceAfterQc.count { it.kind == EvidenceKind.AR_SCREENSHOT }
        val photos = evidenceAfterQc.count { it.kind == EvidenceKind.PHOTO }

        return when {
            arScreenshots < requirements.minArScreenshots ->
                ValidationResult.InsufficientArScreenshots(arScreenshots, requirements.minArScreenshots)
            photos < requirements.minPhotos ->
                ValidationResult.InsufficientPhotos(photos, requirements.minPhotos)
            else -> ValidationResult.Valid
        }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    object NoQcStarted : ValidationResult()
    data class InsufficientArScreenshots(val actual: Int, val required: Int) : ValidationResult()
    data class InsufficientPhotos(val actual: Int, val required: Int) : ValidationResult()
}
```

**Pass/Fail Use Cases:**

```kotlin
class PassQcUseCase(
    private val eventRepository: EventRepository,
    private val evidenceRepository: EvidenceRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        workItemId: String,
        notes: String,
        checklist: Map<String, Boolean>
    ) {
        // 1. Validate policy
        val events = eventRepository.getByWorkItem(workItemId)
        val evidence = evidenceRepository.getByWorkItem(workItemId)

        val validation = QcEvidencePolicy.validateEvidence(evidence, events)
        if (validation !is ValidationResult.Valid) {
            throw InsufficientEvidenceException(validation)
        }

        // 2. Create PASS event
        val inspector = authRepository.getCurrentUser()
        val event = Event(
            id = generateId(),
            workItemId = workItemId,
            type = EventType.QC_PASSED,
            actorId = inspector.id,
            deviceId = getDeviceId(),
            timestamp = Clock.System.now(),
            payload = mapOf(
                "notes" to notes,
                "checklist" to checklist
            )
        )
        eventRepository.insert(event)
    }
}
```

**UI Enforcement:**
- Pass/Fail buttons disabled until policy satisfied
- Banner shows: "Required: 1 AR screenshot, 1 photo" (updates as evidence captured)
- Attempting to PASS/FAIL without evidence shows error dialog

**Acceptance:**
- QC **cannot** mark PASS without ≥1 AR screenshot + ≥1 photo
- Attempting to bypass shows clear error message
- Once evidence requirements met, Pass/Fail buttons enable
- Supervisor later sees all evidence linked to QC decision

### 3.5 QC Checklist v1

**Minimal Checklist:**
Define 3–8 inspection points (configurable per WorkItemType):

```kotlin
data class ChecklistItem(
    val id: String,
    val description: String,
    val required: Boolean
)

// Example for welding inspection:
val weldChecklist = listOf(
    ChecklistItem("weld_penetration", "Weld penetration adequate", required = true),
    ChecklistItem("no_cracks", "No visible cracks", required = true),
    ChecklistItem("no_porosity", "No porosity or voids", required = true),
    ChecklistItem("bead_uniformity", "Bead uniformity acceptable", required = false),
    ChecklistItem("cleanup", "Spatter cleaned", required = false)
)
```

**QC Screen Includes:**
- Checklist with checkboxes
- Notes field (free text)
- Severity selector (if FAIL): Minor, Major, Critical
- Reason codes (if FAIL): dropdown of common defect types

**PASS Payload:**
```kotlin
mapOf(
    "checklist" to mapOf(
        "weld_penetration" to true,
        "no_cracks" to true,
        "no_porosity" to true
    ),
    "notes" to "Good quality weld. Minor spatter."
)
```

**FAIL Payload:**
```kotlin
mapOf(
    "checklist" to mapOf(
        "weld_penetration" to false,
        "no_cracks" to true,
        "no_porosity" to false
    ),
    "notes" to "Insufficient penetration. Porosity on left edge.",
    "severity" to "Major",
    "reasonCodes" to listOf("PENETRATION", "POROSITY")
)
```

**Acceptance:**
- Required checklist items must be checked to enable PASS
- FAIL can be selected even with partial checklist (QC documents what's wrong)
- Checklist + notes + evidence package allows Assembler/Supervisor to understand rejection

---

## Sprint 4 (Weeks 7–8): Supervisor Dashboard and Control

**Result:** Supervisor sees the whole shop like a dispatcher and can inspect any WorkItem in detail.

### 4.1 Supervisor Dashboard v1 (feature:supervisor)

**Dashboard Sections:**

**KPIs (Derived from Events):**
```kotlin
data class ShopKpis(
    val totalWorkItems: Int,
    val inProgress: Int,
    val readyForQc: Int,
    val qcInProgress: Int,
    val passed: Int,
    val failed: Int,
    val rework: Int,
    val avgQcWaitTime: Duration,
    val qcPassRate: Float           // passed / (passed + failed)
)
```

Display as cards:
- "In Progress: 12"
- "QC Queue: 8 (Avg wait: 1h 34m)"
- "Passed Today: 45"
- "Failed Today: 3"
- "QC Pass Rate: 93.8%"

**QC Backlog:**
- List of WorkItems with status READY_FOR_QC
- Sorted by wait time (longest wait first)
- Shows assignee (Assembler), wait time, description
- Tap to drill into WorkItemSummary

**Rework Backlog:**
- List of FAILED or REWORK_REQUIRED items
- Shows:
  - Original Assembler
  - QC inspector who rejected
  - Rejection reason
  - Time in rework status

**Active Users ("Who does what"):**
```kotlin
data class UserActivity(
    val userId: String,
    val userName: String,
    val role: Role,
    val currentWorkItem: WorkItem?,
    val lastAction: Event,
    val lastActionTime: Instant
)
```

Display as list:
- "John Smith (Assembler) — Working on WI-1234 (Started 45m ago)"
- "Jane Doe (QC) — Inspecting WI-5678 (Started 12m ago)"
- "Bob Lee (Assembler) — Idle (Last action 1h ago)"

**Acceptance:**
- Dashboard updates in real-time as events occur
- KPIs accurate (verify against raw event counts)
- Supervisor can see which WorkItems are bottlenecks

### 4.2 WorkItem List + Filters (feature:supervisor)

**All WorkItems Screen:**
- Paginated list (or lazy load) of all WorkItems
- Default sort: most recently updated first

**Filters:**
- Status: multi-select (NEW, IN_PROGRESS, READY_FOR_QC, PASSED, FAILED, REWORK)
- Zone: dropdown (if WorkItems have zones)
- Assignee: dropdown (current Assembler/QC assigned)
- Date range: created/updated between dates

**Search:**
- By code (exact or prefix match)
- By WorkItem ID
- By description (contains text)

**Acceptance:**
- Supervisor can find any WorkItem quickly
- Filters are persistent during session
- Search returns results in <1 second for 1000+ WorkItems

### 4.3 WorkItem Detail (Supervisor View)

**Summary Section:**
- WorkItem code, description, type, zone
- Current status (derived from events)
- Current assignee
- Created date, last updated

**Timeline Section:**
- Full event history, chronological
- Each event shows:
  - Timestamp
  - Actor (user who performed action)
  - Event type (human-readable)
  - Payload summary (e.g., "QC notes: Weld quality excellent")

Example timeline:
```
2025-12-01 08:15 — Created (System)
2025-12-01 09:30 — Claimed by John Smith
2025-12-01 09:32 — Work started by John Smith
2025-12-01 10:45 — Marked ready for QC by John Smith
2025-12-01 11:20 — QC started by Jane Doe
2025-12-01 11:35 — Evidence captured: 1 AR screenshot, 2 photos
2025-12-01 11:42 — QC passed by Jane Doe
  Notes: "Weld quality excellent. No defects."
  Checklist: All items passed
```

**Evidence Viewer:**
- Thumbnails of all photos and AR screenshots
- Click to view full-screen
- Metadata overlay:
  - Capture time
  - Captured by (user)
  - File hash (for verification)
  - AR metadata (if AR screenshot): marker IDs, tracking state, alignment quality

**Acceptance:**
- Supervisor can open any WorkItem and immediately understand its full history
- Evidence viewer shows high-quality images with zoom capability
- Timeline is clear and actionable (Supervisor can trace where delays occurred)

**Artifact:** Screenshot/video walkthrough of Supervisor reviewing a completed WorkItem with full timeline and evidence.

---

## Sprint 5 (Weeks 9–10): Offline Queue and Export Reports

**Result:** System is pilot-ready without backend — everything stored locally and exportable.

### 5.1 Offline Queue (core:data)

**sync_queue Table:**
Already defined in Sprint 1. Now implement SyncManager:

```kotlin
class SyncManager(
    private val syncQueueDao: SyncQueueDao,
    private val eventRepository: EventRepository,
    private val evidenceRepository: EvidenceRepository
) {
    suspend fun enqueueEvent(eventId: String) {
        syncQueueDao.insert(
            SyncQueueEntity(
                entityType = "event",
                entityId = eventId,
                operation = "insert",
                status = "pending",
                retryCount = 0,
                lastAttempt = null,
                error = null
            )
        )
    }

    suspend fun enqueueEvidence(evidenceId: String) {
        syncQueueDao.insert(
            SyncQueueEntity(
                entityType = "evidence",
                entityId = evidenceId,
                operation = "insert",
                status = "pending",
                retryCount = 0,
                lastAttempt = null,
                error = null
            )
        )
    }

    // Future: processQueue() will attempt sync to server
    // For MVP, queue exists but sync can be disabled
}
```

**Sync Status UI:**
- Badge on Supervisor dashboard: "Offline Queue: 12 items pending"
- Tap to see details:
  - 8 events pending
  - 4 evidence files pending
  - Last sync attempt: Never (or timestamp)
- For MVP pilot, this is informational only

**Acceptance:**
- Events and evidence are enqueued after creation
- Queue persists across app restarts
- No crashes if network unavailable (offline is expected state)

### 5.2 Export Center (feature:supervisor or feature:export)

**Export Functionality:**

**Export Daily/Shift Report:**
- Supervisor selects date range (e.g., "Today" or "Shift 2025-12-01 06:00–14:00")
- System generates export package containing:

**JSON Export:**
```json
{
  "export_metadata": {
    "export_id": "exp-20251201-143022",
    "exported_at": "2025-12-01T14:30:22Z",
    "exported_by": "supervisor@example.com",
    "period_start": "2025-12-01T06:00:00Z",
    "period_end": "2025-12-01T14:00:00Z"
  },
  "work_items": [
    {
      "id": "wi-1234",
      "code": "QR-ABC-001",
      "description": "Weld assembly A1",
      "status": "PASSED",
      "events": [
        {
          "id": "evt-001",
          "type": "WORK_CLAIMED",
          "actor": "John Smith",
          "timestamp": "2025-12-01T09:30:00Z"
        },
        {
          "id": "evt-002",
          "type": "QC_PASSED",
          "actor": "Jane Doe",
          "timestamp": "2025-12-01T11:42:00Z",
          "payload": {
            "notes": "Excellent quality",
            "checklist": { "weld_penetration": true, "no_cracks": true }
          }
        }
      ],
      "evidence": [
        {
          "id": "evi-001",
          "kind": "AR_SCREENSHOT",
          "file_path": "evidence/ar_screenshots/wi-1234/20251201_114035.png",
          "file_hash": "a1b2c3d4e5f6...",
          "captured_at": "2025-12-01T11:40:35Z"
        }
      ]
    }
  ],
  "summary": {
    "total_work_items": 52,
    "passed": 49,
    "failed": 3,
    "qc_pass_rate": 0.942
  }
}
```

**CSV Export (Optional):**
- Flattened view for Excel import:
  - WorkItem ID, Code, Status, Assembler, QC Inspector, Pass/Fail, Notes, Evidence Count

**Evidence Files:**
- Option 1: Separate ZIP of all evidence files (maintains original paths)
- Option 2: Inline base64 in JSON (for small exports)
- Include checksums file: `evidence_checksums.txt` listing SHA-256 for each file

**Export Actions:**
- "Export to Files" → Saves to `Documents/ARWeld/exports/`
- "Share Export" → Android share sheet (send via email, cloud storage)

**Acceptance:**
- Without network, Supervisor can complete shift and export full report
- Export includes all WorkItems, events, QC decisions, and evidence
- Checksums allow verification that evidence files haven't been tampered with
- Export can be imported into Excel or custom reporting tool

### 5.3 Reports v1 (feature:supervisor)

**Supervisor Report Views:**

**Completion Report:**
- Total completed (PASSED + FAILED)
- By Assembler: "John Smith: 15 completed, 14 passed, 1 failed"
- By QC Inspector: "Jane Doe: 20 inspections, 19 passed, 1 failed"
- Time-based: hourly completion rate chart

**Top Rejection Reasons:**
```kotlin
data class RejectionReason(
    val reasonCode: String,
    val description: String,
    val count: Int
)

// Example:
// POROSITY: 5 occurrences
// PENETRATION: 3 occurrences
// ALIGNMENT: 1 occurrence
```

**Most Problematic Nodes:**
- If WorkItems have nodeId, aggregate failures by node:
  - "Node A1: 3 failures (60% fail rate)"
  - "Node B2: 1 failure (10% fail rate)"
- Helps identify design or process issues

**Acceptance:**
- Reports update in real-time as events occur
- Supervisor can drill down: tap "POROSITY: 5" to see list of affected WorkItems
- Export includes aggregated report data

**Artifact:** Demo video showing Supervisor completing shift, viewing reports, and exporting package without network connection.

---

## Sprint 6 (Weeks 11–12): AR Hardening and Pilot Readiness

**Result:** Assembler and QC can work without engineer support. AR is robust. UX is clear and efficient.

### 6.1 AR Hardening

**Multi-Marker Refinement:**
- If 2+ markers visible simultaneously, refine transformation matrix:
  - Average pose estimates
  - Reduce drift from single-marker tracking
  - Improve stability when operator moves around part

**Re-align Recommendation:**
```kotlin
data class AlignmentQuality(
    val score: Float,              // 0.0 to 1.0
    val trackingState: String,     // "TRACKING", "PAUSED", "STOPPED"
    val markerVisibility: Int,     // Number of markers currently visible
    val driftEstimate: Float       // mm of estimated drift
)

fun shouldRecommendRealignment(quality: AlignmentQuality): Boolean {
    return quality.score < 0.6 ||
           quality.trackingState != "TRACKING" ||
           quality.driftEstimate > 10.0  // 10mm threshold
}
```

**UI Indicator:**
- Green: Good alignment
- Yellow + hint: "Alignment drifting. Tap to re-align." (quality 0.4–0.6)
- Red + banner: "Tracking lost. Please re-align." (quality < 0.4 or STOPPED)

**Performance Optimizations:**

**Layer-Based Culling:**
- Only render model components relevant to current inspection step
- Example: If inspecting weld zone A, hide zones B, C, D
- Reduces polygon count, improves FPS

**Model Caching:**
- Load and parse 3D models once, cache in memory
- Pre-process models on background thread during app initialization

**FPS Guardrails:**
- Target: 30 FPS minimum on Pixel 6-class device
- If FPS drops below 20, reduce model complexity:
  - Switch to low-poly version
  - Disable shadows/reflections
  - Show warning: "Performance low. Consider simplifying model."

**Acceptance:**
- AR maintains ≥30 FPS on target device during typical inspection
- Multi-marker tracking reduces drift by ≥40% vs single-marker
- Re-align hints appear when needed, not excessively
- Operator can complete AR inspection in <2 minutes

### 6.2 UX Polish for Key Flows

**Reduce Clicks:**

**Assembler Flow Optimization:**
- Current: Scan → Summary → Claim → Summary → Start → Summary → Ready for QC (5 screens)
- Optimized: Scan → Auto-claim + start → AR (optional) → Mark Ready (3 screens)
- "Claim and Start" button combines two actions

**QC Flow Optimization:**
- Current: Queue → Tap item → Summary → Start → AR → Evidence → Summary → Pass/Fail
- Optimized: Queue → Tap item → Auto-start + AR → Evidence + Checklist → Pass/Fail
- QC screen integrates checklist, evidence thumbnails, and decision buttons on one screen

**Clear Status Indicators:**
- Status badges with color coding:
  - NEW: Gray
  - IN_PROGRESS: Blue
  - READY_FOR_QC: Orange
  - QC_IN_PROGRESS: Purple
  - PASSED: Green
  - FAILED: Red
  - REWORK: Yellow
- Consistent across all screens

**Clear Error Messages:**
- "Marker not found" → "QR marker not detected. Ensure marker is clean and well-lit. Tap to retry."
- "Evidence missing" → "Required: 1 AR screenshot, 1 photo. Current: 0 AR screenshots, 1 photo."
- "Offline mode" → "No network connection. Working offline. 12 items in sync queue."

**Loading States:**
- Skeleton screens while loading WorkItem details
- Spinner during AR model load: "Loading 3D model… (2.3 MB)"
- Progress indicator during export: "Exporting 52 WorkItems… 34/52 complete"

**Acceptance:**
- User testing with 3 operators (1 Assembler, 1 QC, 1 Supervisor):
  - Each completes typical workflow without assistance
  - No confusion about current status or next action
  - Errors are self-explanatory

### 6.3 Pilot Checklist and Test Scenarios

**Manual Test Scenarios (10–15 cases):**

1. **Happy Path — Full Cycle:**
   - Assembler: Scan → Claim → AR align → Mark ready
   - QC: Start → AR inspect → Capture evidence (1 AR screenshot, 2 photos) → Pass
   - Supervisor: View timeline, see all evidence

2. **QC Rejection:**
   - QC: Inspect → FAIL with reason "Porosity"
   - WorkItem enters REWORK state
   - Assembler: See rework item in queue → Fix → Mark ready again
   - QC: Re-inspect → PASS

3. **Bad Lighting:**
   - Attempt scan in dim lighting → Barcode recognition fails
   - System shows hint: "Improve lighting or move closer"
   - Move to better lighting → Scan succeeds

4. **Dirty Marker:**
   - Marker partially obscured or dirty
   - AR tracking quality degrades (yellow/red indicator)
   - System prompts: "Clean marker or re-align"
   - Clean marker → Tracking improves

5. **Network Drop (Offline Mode):**
   - Start with network connected
   - Disable network mid-workflow
   - Complete full cycle (scan, claim, QC, pass) offline
   - Check sync queue: all events and evidence enqueued
   - Re-enable network → (Future: sync occurs)

6. **QC Cannot PASS Without Evidence:**
   - QC starts inspection
   - Attempts to tap "Pass" without capturing evidence
   - System blocks action, shows error banner
   - Capture required evidence → "Pass" button enables

7. **Supervisor Export:**
   - Supervisor selects date range
   - Exports report to Documents folder
   - Open JSON file → Verify structure, checksums
   - Open CSV in Excel → Verify data matches app

8. **Multiple Assemblers Same WorkItem:**
   - Assembler A claims WorkItem
   - Assembler B attempts to claim same item
   - System prevents duplicate claim or transfers ownership
   - (Policy decision: enforce single assignee or allow transfer)

9. **AR Performance Stress Test:**
   - Load large 3D model (e.g., 5 MB, 100k polygons)
   - Measure FPS on target device
   - Verify FPS ≥30 or graceful degradation (low-poly fallback)

10. **Multi-Marker Alignment:**
    - Use part with 2+ visible markers
    - Align using marker A
    - Walk around to show marker B
    - Verify alignment refinement occurs, drift reduces

11. **Checklist Validation:**
    - QC starts inspection, leaves required checklist item unchecked
    - Attempts to PASS
    - System blocks, highlights missing item
    - Complete checklist → PASS succeeds

12. **Evidence File Integrity:**
    - After export, manually corrupt an evidence file
    - Run checksum verification tool
    - System detects hash mismatch, flags file as tampered

13. **Long Timeline (20+ Events):**
    - Create WorkItem with complex history: claim, unclaim, re-claim, start, pause, resume, QC fail, rework, QC pass
    - View timeline in app
    - Verify all events visible, chronological, readable

14. **Supervisor Filter Performance:**
    - Seed database with 1000+ WorkItems
    - Apply filters: status=PASSED, date=today
    - Measure response time < 1 second

15. **Session Continuity:**
    - Assembler starts work on WorkItem
    - App crashes or is force-closed
    - Reopen app
    - Verify WorkItem still shows IN_PROGRESS, event logged

**Acceptance Criteria for Pilot Readiness:**
- All 15 scenarios pass without critical blockers
- 1–2 days of on-site testing with real operators:
  - Operators can complete workflows independently
  - No data loss or corruption
  - Offline mode functions correctly
  - AR alignment works in shop-floor lighting conditions
- Documentation complete:
  - User guide (how to use app for each role)
  - Admin guide (how to export, interpret reports)
  - Known issues list (acceptable minor bugs)

**Artifact:** Pilot readiness report documenting:
- Test results (pass/fail for each scenario)
- Operator feedback notes
- Performance benchmarks (FPS, scan latency, export time)
- Decision: GO/NO-GO for pilot deployment

---

## Post-Sprint 6: Future Enhancements (Out of MVP Scope)

The following are explicitly **NOT** in the MVP but may be roadmapped for future releases:

- **Server Sync:** Real-time sync to backend server for multi-device coordination
- **Advanced Analytics:** Predictive quality models, defect pattern detection
- **User Management Server:** Centralized user/role provisioning, LDAP/SSO integration
- **NFC Support:** Tap-to-scan for parts with embedded NFC tags
- **Video Evidence:** Record short inspection videos (increases storage complexity)
- **Augmented Checklists:** Dynamic checklists based on WorkItem type and historical defects
- **Collaboration Tools:** Comments, @mentions, QC ↔ Assembler chat within WorkItem
- **Mobile Device Management:** Remote configuration, app updates, policy enforcement
- **Multi-Language Support:** UI translations for non-English operators
- **Advanced AR:** Measurements, annotations, X-ray view, animation of assembly sequence

---

## Summary

This 6-sprint roadmap delivers a **production-ready offline-first AR-assisted QA system** for fabrication shops:

- **Sprint 1:** Data foundation, event-driven architecture, basic navigation
- **Sprint 2:** Assembler workflow with AR v1 alignment
- **Sprint 3:** QC workflow with mandatory evidence gate
- **Sprint 4:** Supervisor control, full visibility, drill-down
- **Sprint 5:** Offline queue, export/reporting without server
- **Sprint 6:** AR hardening, UX polish, pilot readiness

**Delivery:** After Sprint 6, the system is ready for on-site pilot deployment with real operators, real parts, and no network dependency.
