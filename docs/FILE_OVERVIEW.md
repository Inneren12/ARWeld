# ARWeld — File Overview and Navigation Guide

This document provides a **practical map** of the ARWeld codebase, explaining where files live and where to add new functionality. Use this as a quick reference when implementing features from `stage.md`.

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
│   │   │   │   ├── Event.kt
│   │   │   │   ├── Evidence.kt
│   │   │   │   ├── User.kt
│   │   │   │   ├── Role.kt
│   │   │   │   └── WorkItemState.kt
│   │   │   ├── reducer/                   # State derivation
│   │   │   │   └── WorkItemStateReducer.kt
│   │   │   ├── policy/                    # Business rules
│   │   │   │   ├── RolePolicy.kt
│   │   │   │   └── QcEvidencePolicy.kt
│   │   │   └── validation/                # Domain validation
│   │   │       └── ValidationResult.kt
│   │   └── build.gradle.kts               # Pure Kotlin library
│   │
│   ├── data/                              # Data layer
│   │   ├── src/main/kotlin/com/example/arweld/core/data/
│   │   │   ├── db/                        # Room database
│   │   │   │   ├── ArWeldDatabase.kt      # Database setup
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
│   │   │   │       └── SyncQueueDao.kt
│   │   │   ├── repository/                # Repository implementations
│   │   │   │   ├── WorkItemRepositoryImpl.kt
│   │   │   │   ├── EventRepositoryImpl.kt
│   │   │   │   └── EvidenceRepositoryImpl.kt
│   │   │   ├── file/                      # File storage
│   │   │   │   ├── EvidenceFileManager.kt # Save/load evidence files
│   │   │   │   └── ChecksumCalculator.kt  # SHA-256 hashing
│   │   │   ├── sync/                      # Offline queue
│   │   │   │   └── SyncManager.kt
│   │   │   └── mapper/                    # Entity ↔ Domain mappers
│   │   │       └── EntityMappers.kt
│   │   └── build.gradle.kts               # Android library + Room
│   │
│   └── auth/                              # Authentication
│       ├── src/main/kotlin/com/example/arweld/core/auth/
│       │   ├── AuthRepository.kt          # Auth interface
│       │   ├── LocalAuthRepository.kt     # Local implementation
│       │   ├── SessionManager.kt          # Session tracking
│       │   └── PermissionChecker.kt       # Role permission checking
│       └── build.gradle.kts
│
├── feature/
│   ├── home/                              # Home screen
│   │   ├── src/main/kotlin/com/example/arweld/feature/home/
│   │   │   ├── ui/
│   │   │   │   └── HomeScreen.kt          # Role-based navigation tiles
│   │   │   └── viewmodel/
│   │   │       └── HomeViewModel.kt
│   │   └── build.gradle.kts
│   │
│   ├── work/                              # Assembler workflows (or "assembler")
│   │   ├── src/main/kotlin/com/example/arweld/feature/work/
│   │   │   ├── ui/
│   │   │   │   ├── MyWorkScreen.kt        # Assembler queue
│   │   │   │   └── WorkItemSummaryScreen.kt # WorkItem detail
│   │   │   ├── viewmodel/
│   │   │   │   ├── MyWorkViewModel.kt
│   │   │   │   └── WorkItemSummaryViewModel.kt
│   │   │   └── usecase/
│   │   │       ├── ClaimWorkUseCase.kt
│   │   │       ├── StartWorkUseCase.kt
│   │   │       └── MarkReadyForQcUseCase.kt
│   │   └── build.gradle.kts
│   │
│   ├── scanner/                           # Barcode/QR scanning
│   │   ├── src/main/kotlin/com/example/arweld/feature/scanner/
│   │   │   ├── ui/
│   │   │   │   └── ScannerScreen.kt       # Camera preview + detection
│   │   │   ├── viewmodel/
│   │   │   │   └── ScannerViewModel.kt
│   │   │   ├── usecase/
│   │   │   │   └── ResolveWorkItemUseCase.kt # Code → WorkItem lookup
│   │   │   └── camera/
│   │   │       └── CameraManager.kt       # CameraX setup
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
│       │   │   ├── ArViewScreen.kt        # Main AR view
│       │   │   └── AlignmentIndicatorWidget.kt # Quality indicator
│       │   ├── viewmodel/
│       │   │   └── ArViewViewModel.kt
│       │   ├── ar/
│       │   │   ├── ArSessionManager.kt    # ARCore session
│       │   │   ├── ModelLoader.kt         # Load 3D models
│       │   │   ├── MarkerDetector.kt      # Detect markers
│       │   │   ├── AlignmentCalculator.kt # Alignment quality
│       │   │   └── ScreenshotCapture.kt   # Capture AR frame
│       │   └── rendering/
│       │       ├── SceneRenderer.kt       # Render overlay
│       │       └── ModelCache.kt          # Cache models
│       └── build.gradle.kts
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
- `ArWeldDatabase` — Room database singleton
- `WorkItemDao`, `EventDao` — DAOs from database

**Where to add new database providers:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideNewDao(database: ArWeldDatabase): NewDao {
        return database.newDao()
    }
}
```

#### RepositoryModule (core-data)

**Location:** `core-data/src/main/kotlin/com/example/arweld/core/data/di/DataModule.kt` (same file)

**Binds:**
- `WorkItemRepository` → `WorkItemRepositoryImpl`
- `EventRepository` → `EventRepositoryImpl`
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

#### AuthModule (core-auth)

**Location:** `core-auth/src/main/kotlin/com/example/arweld/core/auth/di/AuthModule.kt`

**Binds:**
- `AuthRepository` → `LocalAuthRepository`

**Where to add new auth providers:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindNewAuthService(
        impl: NewAuthServiceImpl
    ): NewAuthService
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
| Auth service binding | `core-auth/di/AuthModule.kt` → Add @Binds method |
| New ViewModel | Feature module → Annotate with @HiltViewModel, use @Inject constructor |
| Activity injection | Annotate with @AndroidEntryPoint |
| Fragment injection | Annotate with @AndroidEntryPoint |

---

## Where to Add New Functionality

### "I need to add a new EventType"

**Location:** `core/domain/src/main/kotlin/com/example/arweld/core/domain/model/Event.kt`

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

2. Update `WorkItemStateReducer.kt` to handle new event in state derivation logic

3. Add use case in appropriate feature module to create the event

**Example:** Adding "WORK_PAUSED" event:
- Add to `EventType` enum
- Update reducer: `WORK_STARTED + WORK_PAUSED → status = PAUSED`
- Add `PauseWorkUseCase.kt` in `feature:work/usecase/`

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

**Location:** `core/domain/src/main/kotlin/com/example/arweld/core/domain/policy/QcEvidencePolicy.kt`

**Steps:**
1. Modify `Requirements` data class:
   ```kotlin
   data class Requirements(
       val minArScreenshots: Int = 1,
       val minPhotos: Int = 1,
       val minVideos: Int = 0,        // Add new requirement
       val requireAfterQcStarted: Boolean = true
   )
   ```

2. Update `validateEvidence()` logic to check new requirement

3. Update UI in `feature:qc/ui/QcInspectionScreen.kt` to show new requirement banner

**Example:** Require 2 AR screenshots instead of 1:
- Change `minArScreenshots: Int = 2` in Requirements
- UI automatically updates (reads from policy)

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
           val user = authRepository.getCurrentUser()
           require(user.role == Role.QC_INSPECTOR) { "QC only" }

           // Create event
           val event = Event(
               id = generateId(),
               workItemId = workItemId,
               type = EventType.NEW_QC_ACTION,
               actorId = user.id,
               deviceId = getDeviceId(),
               timestamp = Clock.System.now(),
               payload = mapOf("data" to params)
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

2. **Increment Database Version:** Update `ArWeldDatabase.kt`
   ```kotlin
   @Database(
       entities = [WorkItemEntity::class, /* ... */],
       version = 2,  // Increment version
       exportSchema = false
   )
   abstract class ArWeldDatabase : RoomDatabase() {
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

4. **Register Migration:** In database builder (likely in `app/di/AppModule.kt`)
   ```kotlin
   Room.databaseBuilder(context, ArWeldDatabase::class.java, "arweld.db")
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

**Location:** `core/domain/src/main/kotlin/com/example/arweld/core/domain/policy/RolePolicy.kt`

**Steps:**
1. **Define Permission:** Add to RolePolicy
   ```kotlin
   object RolePolicy {
       fun canPerform(role: Role, action: String): Boolean {
           return when (role) {
               Role.ASSEMBLER -> action in listOf("CLAIM", "START", "READY_FOR_QC")
               Role.QC_INSPECTOR -> action in listOf("START_QC", "PASS", "FAIL", "NEW_ACTION")
               Role.SUPERVISOR -> action in listOf("VIEW_ALL", "EXPORT", "NEW_SUPERVISOR_ACTION")
           }
       }
   }
   ```

2. **Check in Use Case:** Enforce in relevant use case
   ```kotlin
   require(RolePolicy.canPerform(user.role, "NEW_ACTION")) {
       "User does not have permission"
   }
   ```

3. **Update UI:** Hide/disable buttons based on permission (via ViewModel)

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

## Navigation Routes

**Defined in:** `app/navigation/AppNavigation.kt`

**Route Naming Convention:**
- Lowercase with underscores: `"home"`, `"my_work"`, `"qc_inspection/{workItemId}"`
- Use path parameters for IDs: `{workItemId}`, `{userId}`

**Example Routes:**
```kotlin
"home"                           → HomeScreen
"my_work"                        → MyWorkScreen (Assembler)
"work_item/{workItemId}"         → WorkItemSummaryScreen
"scan"                           → ScannerScreen
"qc_queue"                       → QcQueueScreen
"qc_inspection/{workItemId}"     → QcInspectionScreen
"ar_view/{workItemId}"           → ArViewScreen
"supervisor_dashboard"           → SupervisorDashboardScreen
"work_item_list"                 → WorkItemListScreen
"work_item_detail/{workItemId}"  → WorkItemDetailScreen (Supervisor)
"reports"                        → ReportsScreen
"export"                         → ExportScreen
```

**Adding a New Route:**
1. Define in `AppNavigation.kt`:
   ```kotlin
   composable("new_route/{param}") { backStackEntry ->
       val param = backStackEntry.arguments?.getString("param")
       NewScreen(param = param)
   }
   ```

2. Navigate from another screen:
   ```kotlin
   navController.navigate("new_route/$paramValue")
   ```

---

## Testing Conventions

### Unit Tests (core:domain)
**Location:** `core/domain/src/test/kotlin/com/example/arweld/core/domain/`

**Naming:** `ClassNameTest.kt` (e.g., `WorkItemStateReducerTest.kt`)

**Example:**
```kotlin
class WorkItemStateReducerTest {
    @Test
    fun `reduce events from NEW to PASSED`() {
        val events = listOf(
            Event(type = EventType.WORK_CLAIMED, /* ... */),
            Event(type = EventType.QC_STARTED, /* ... */),
            Event(type = EventType.QC_PASSED, /* ... */)
        )
        val state = WorkItemStateReducer.reduce(events)
        assertEquals(WorkItemStatus.PASSED, state.status)
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
    database = Room.inMemoryDatabaseBuilder(context, ArWeldDatabase::class.java)
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
   override fun getByStatus(status: WorkItemStatus): Flow<List<WorkItem>> {
       return workItemDao.observeByStatus(status.name)
           .map { entities -> entities.map { it.toDomain() } }
   }
   ```

3. **Use in ViewModel:**
   ```kotlin
   val workItems: StateFlow<List<WorkItem>> = workItemRepository
       .getByStatus(WorkItemStatus.IN_PROGRESS)
       .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
   ```

---

## Quick Reference

### "Where is...?"

| What | Location |
|------|----------|
| Domain models (WorkItem, Event, etc.) | `core/domain/model/` |
| Database entities | `core/data/db/entity/` |
| DAOs | `core/data/db/dao/` |
| Repositories | `core/data/repository/` |
| Use cases | `feature/*/usecase/` |
| Screens (Compose) | `feature/*/ui/` |
| ViewModels | `feature/*/viewmodel/` |
| Navigation routes | `app/navigation/AppNavigation.kt` |
| DI setup | `app/di/AppModule.kt` |
| Reducer logic | `core/domain/reducer/` |
| Role/QC policies | `core/domain/policy/` |
| AR rendering | `feature/arview/ar/` and `rendering/` |
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
