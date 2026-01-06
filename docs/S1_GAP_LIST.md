# Sprint 1 Gap List

| ItemID | Status | Evidence (paths) | Fix strategy |
| --- | --- | --- | --- |
| S1-01 | Implemented | `settings.gradle.kts`, `feature-assembler/`, `feature-qc/` | Added minimal assembler/QC feature modules and wired them into the build to match the planned module list. |
| S1-02 | Implemented | `app/build.gradle.kts`, `app/src/main/kotlin/com/example/arweld/ArWeldApplication.kt` | Hilt remains configured with application entrypoint and DI modules; no further action required. |
| S1-03 | Implemented | `app/src/main/kotlin/com/example/arweld/navigation/AppNavigation.kt`, `app/src/main/kotlin/com/example/arweld/ui/auth/SplashScreen.kt` | Reworked Compose navigation into AuthGraph (Splash→Login) and MainGraph (Home + feature routes). |
| S1-04 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/model/Role.kt` | Role enum already covered required roles. |
| S1-05 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/Permission.kt`, `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/RolePolicy.kt` | Permissions and RolePolicy defined; reinforced with new unit test. |
| S1-06 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/model/WorkItem.kt` | WorkItemType updated to PART/NODE/OPERATION. |
| S1-07 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/EventType.kt` | EventType includes required events. |
| S1-08 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/model/WorkItem.kt`, `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/Event.kt`, `core-domain/src/main/kotlin/com/example/arweld/core/domain/evidence/Evidence.kt` | Domain models present; no gaps. |
| S1-09 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/state/WorkItemState.kt`, `core-domain/src/test/kotlin/com/example/arweld/core/domain/state/WorkItemStateTest.kt` | Reducer fixed and covered by happy-path + rework flow tests. |
| S1-10 | Implemented | `core-data/src/main/kotlin/com/example/arweld/core/data/db/entity/*Entity.kt` | Room entities for WorkItem, Event, Evidence, User, SyncQueue in place. |
| S1-11 | Implemented | `core-data/src/main/kotlin/com/example/arweld/core/data/db/dao/*.kt` | DAOs for all entities exist. |
| S1-12 | Implemented | `core-data/src/main/kotlin/com/example/arweld/core/data/db/AppDatabase.kt`, `core-data/src/main/kotlin/com/example/arweld/core/data/db/Migrations.kt`, `docs/PROJECT_OVERVIEW.md` | Database version 2 with migration_1_2 documented as S1 baseline. |
| S1-13 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/work/WorkRepository.kt`, `core-data/src/main/kotlin/com/example/arweld/core/data/work/WorkRepositoryImpl.kt` | Added listByStatus/listMyQueue/listQcQueue plus backward-compatible aliases. |
| S1-14 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/event/EventRepository.kt`, `core-data/src/main/kotlin/com/example/arweld/core/data/repository/EventRepositoryImpl.kt` | Added lastEventsByUser query alongside append/query methods. |
| S1-15 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/evidence/EvidenceRepository.kt`, `core-data/src/main/kotlin/com/example/arweld/core/data/repository/EvidenceRepositoryImpl.kt` | Added saveFileAndRecord wrapper and listByEvent alias on top of existing evidence operations. |
| S1-16 | Implemented | `core-domain/src/main/kotlin/com/example/arweld/core/domain/auth/AuthRepository.kt`, `core-data/src/main/kotlin/com/example/arweld/core/data/auth/AuthRepositoryImpl.kt` | Mock login + currentUser implemented. |
| S1-17 | Implemented | `app/src/main/kotlin/com/example/arweld/ui/auth/SplashScreen.kt` | Splash screen routes into auth graph. |
| S1-18 | Implemented | `app/src/main/kotlin/com/example/arweld/ui/auth/LoginRoute.kt` | Login screen uses mock/seeded users. |
| S1-19 | Implemented | `feature-home/src/main/kotlin/com/example/arweld/feature/home/ui/HomeScreen.kt` | Home tiles now conditionally shown by role (Assembler/QC/Supervisor/Director). |
| S1-20 | Implemented | `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/WorkItemSummaryScreen.kt` | WorkItemSummary stub exists. |
| S1-21 | Implemented | `feature-work/src/main/kotlin/com/example/arweld/feature/work/ui/TimelineScreen.kt` | Timeline stub exists. |
| S1-22 | Implemented | `core-domain/src/test/kotlin/com/example/arweld/core/domain/state/WorkItemStateTest.kt` | Reducer happy-path unit test added. |
| S1-23 | Implemented | `core-domain/src/test/kotlin/com/example/arweld/core/domain/state/WorkItemStateTest.kt` | Reducer fail→rework→ready→pass unit test added. |
| S1-24 | Implemented | `core-domain/src/test/kotlin/com/example/arweld/core/domain/auth/RolePolicyTest.kt` | RolePolicy unit test covering QC vs Assembler PASS_QC. |
| S1-25 | Implemented | `core-data/src/androidTest/java/com/example/arweld/core/data/db/dao/EventDaoInstrumentedTest.kt` | Room instrumentation test for Event insert/read already in place. |
