# ARWeld — AR-Assisted QA System for Fabrication

## Problem Statement

Modern fabrication and assembly operations face critical challenges in quality assurance:

- **Traceability Gap:** When defects are discovered downstream, it's difficult to determine who performed which operations, when they occurred, and what the quality control process revealed
- **Evidence Fragmentation:** QA inspections rely on manual notes, separate photo storage, and paper checklists, making it hard to reconstruct inspection context later
- **Offline Requirements:** Shop-floor environments often lack reliable network connectivity, but work cannot stop
- **Inspection Consistency:** Different QC inspectors may apply different standards without clear, enforceable policies
- **Supervisor Blindness:** Management lacks real-time visibility into shop-floor status, bottlenecks, and quality trends

ARWeld solves these problems by providing an **offline-first, AR-assisted quality assurance system** that creates an immutable audit trail, enforces evidence policies, and provides real-time visibility across the entire fabrication workflow.

## Solution Overview

ARWeld is an Android application that combines:

- **Event-driven architecture** — Every action (claim work, start QC, pass/fail) is logged as an immutable event, creating a complete audit trail
- **AR visualization** — Augmented reality overlay of 3D models helps operators verify geometry, alignment, and assembly correctness
- **Evidence enforcement** — QC inspectors cannot approve or reject work without capturing mandatory photographic and AR evidence
- **Offline-first design** — All data stored locally in Room database; network sync is additive, not required
- **Role-based workflows** — Tailored interfaces for Assemblers, QC Inspectors, and Supervisors
- **Mocked role-based login** — MVP authentication lets users pick their role on the Login screen (Assembler, QC, Supervisor, Director) without a backend

## Personas and Workflows

### 1. Assembler

**Role:** Performs fabrication or assembly work on parts and assemblies.

**Typical Workflow:**
1. Scan barcode/QR code on part or work order
2. Claim work item (assigns it to themselves)
3. Start work (begins tracking time)
4. Optionally use AR view to:
   - Verify part placement against 3D model
   - Check alignment using marker-based tracking
   - Validate geometry before welding/assembly
5. Mark work as "Ready for QC" when complete
6. If QC rejects: receive rework item, fix issues, re-submit

**Key Benefits:**
- Clear ownership: only one person responsible for each work item at a time
- AR guidance reduces errors during complex assembly
- Instant feedback when work is ready for next stage

### 2. QC Inspector

**Role:** Performs quality inspections and decides whether work passes or fails quality standards.

**Typical Workflow:**
1. View QC queue (list of items ready for inspection, sorted by wait time)
2. Select work item to inspect
3. System automatically starts QC inspection (logs QC_STARTED event)
4. Perform inspection:
   - Use AR view to compare actual part against 3D model
   - Capture AR screenshot showing alignment and tracking quality
   - Take photos of critical features, welds, or potential defects
5. Complete checklist (3–8 inspection points specific to work type)
6. Decide: **PASS** or **FAIL**
   - System enforces evidence policy: cannot proceed without minimum evidence (≥1 AR screenshot, ≥1 photo)
   - If FAIL: document reason codes (porosity, penetration, alignment, etc.) and severity
7. Work item moves to PASSED (complete) or FAILED (returns to Assembler for rework)

**Key Benefits:**
- Cannot bypass evidence requirements (prevents "rubber stamping")
- All inspection evidence automatically linked to decision
- Checklist ensures consistent inspection criteria
- Complete audit trail: who inspected, when, what they saw, why they decided pass/fail

### 3. Supervisor

**Role:** Monitors overall shop-floor operations, identifies bottlenecks, reviews quality trends, and exports reports for management.

**Typical Workflow:**
1. View dashboard with real-time KPIs:
   - Work items in progress
   - QC queue backlog and average wait time
   - Today's pass/fail counts and QC pass rate
   - Current activity per operator
2. Drill down into specific areas:
   - View all work items with filters (status, zone, assignee, date)
   - Inspect individual work item timelines to understand delays
   - Review evidence for any inspection
3. Analyze quality trends:
   - Top rejection reasons
   - Most problematic nodes/parts (high failure rates)
   - Completion rates by assembler/inspector
4. Export reports:
   - Daily or shift reports with full event history
   - Evidence package (all photos/AR screenshots with checksums)
   - Share export via email or save to network drive

**Key Benefits:**
- Real-time visibility across entire shop (no more walking around to check status)
- Data-driven decisions: identify which parts need design/process improvements
- Complete traceability: can reconstruct any work item's history with evidence
- Offline exports: no server required for pilot deployment

## Core Principles

### 1. Event Log as Source of Truth

ARWeld uses **event sourcing** architecture:

- Every state change is recorded as an immutable Event
- Events have:
  - Timestamp (when)
  - Actor (who)
  - Device (where)
  - Type (what action)
  - Payload (additional context)
- WorkItem "status" is not stored directly; it's **derived** from the event log using a reducer (`reduce(events) → WorkItemState` with `WorkStatus`/`QcStatus`)
- This provides:
  - Complete audit trail (every action is logged)
  - Time-travel debugging (can reconstruct state at any point)
  - Conflict-free sync (events can be merged deterministically)

**Example Event Sequence:**
```
NEW (initial state)
  → WORK_CLAIMED (Assembler A, 9:30 AM)
  → WORK_STARTED (Assembler A, 9:32 AM)
  → WORK_READY_FOR_QC (Assembler A, 10:45 AM)
  → QC_STARTED (QC Inspector B, 11:20 AM)
  → EVIDENCE_CAPTURED (QC Inspector B, 11:35 AM)
  → QC_PASSED (QC Inspector B, 11:42 AM)
PASSED (final derived state)
```

### 2. WorkItem as Unified Concept

ARWeld uses a single **WorkItem** model to represent any trackable unit:

- Physical part (e.g., "Bracket A-123")
- Assembly node (e.g., "Weld joint W-45")
- Operation (e.g., "Paint station 3")

Barcodes, QR codes, and NFC tags are just **identifiers** that resolve to a WorkItem. This unified model simplifies workflows and allows flexible tracking at different granularities.

### 3. Offline-First Design

ARWeld is designed to function **completely without network connectivity**:

- **Local storage:** Room database stores WorkItems, Events, Evidence entities
- **File storage:** Photos and AR screenshots saved to device with SHA-256 checksums
- **Sync queue:** Changes are queued locally; network sync is additive (when available)
- **Export capability:** Supervisor can export complete report packages as JSON/CSV + evidence files

This design is critical for shop-floor environments where:
- WiFi may be unreliable or unavailable
- Work cannot stop due to network issues
- Data must be captured in real-time, not "when we get back online"

**Note:** The MVP delivers offline-first with local export. Future versions may add server sync for multi-device coordination, but the system is **pilot-ready without a backend**.

### 4. AR-Assisted, Not AR-Required

Augmented reality is a **powerful tool**, not a requirement:

- If WorkItem has no linked 3D model → AR button hidden, workflow continues normally
- AR provides:
  - Visual confirmation (does the part match the model?)
  - Alignment verification (is the part positioned correctly?)
  - Inspection guidance (what should I check?)
  - Documented evidence (AR screenshot shows what the inspector saw)
- System still works as a barcode scanner + evidence logger + checklist tool without AR

This makes ARWeld adaptable to:
- Parts with CAD models (full AR capability)
- Parts without models (traditional photo-based QC)
- Gradual rollout (start with scanner/QC, add AR later)

## MVP Scope

The MVP (Minimum Viable Product) delivers a **pilot-ready system in 6 sprints (~12 weeks)**.

### What's Included in MVP

**Sprint 1–2: Foundation + Assembler Workflow**
- Local database with event-driven architecture
- Role-based authentication (local users)
- Barcode/QR scanning with camera
- Assembler workflow: scan → claim → start → mark ready for QC
- WorkItemSummary screen stub (S1-20) ready to show detail/state/evidence in later sprints
- Timeline screen stub (S1-21) reachable from Home for future event history
- AR v1: marker-based alignment, basic overlay visualization
- Unit tests for core reducers and role policies

**Sprint 3–4: QC Workflow + Supervisor Dashboard**
- QC queue (items awaiting inspection)
- Evidence capture: photos + AR screenshots with metadata
- QC policy gate: enforced minimum evidence requirements
- Simple checklist (3–8 inspection points)
- PASS/FAIL decision with reason codes
- Supervisor dashboard: KPIs, backlogs, user activity
- WorkItem detail view with full timeline and evidence

**Sprint 5–6: Export + Hardening**
- Offline sync queue implementation
- Export center: JSON/CSV reports + evidence packages
- Aggregated reports (top rejection reasons, problematic nodes)
- AR performance optimization (multi-marker, FPS targets)
- UX polish (reduce clicks, clear status/error messages)
- Pilot test scenarios (15 manual test cases)

### What's Explicitly Out of Scope for MVP

The following features are **not** in the 12-week MVP but may be considered for future releases:

**Server Infrastructure:**
- Real-time sync to backend server
- Multi-device coordination
- Centralized user/role management (LDAP/SSO integration)

**Advanced Analytics:**
- Predictive quality models (ML-based defect detection)
- Statistical process control (SPC) charts
- Root cause analysis tools

**Enhanced Evidence:**
- Video recording (increases storage complexity)
- Sensor data integration (temperature, vibration, etc.)
- Automated defect detection (computer vision)

**Collaboration Features:**
- In-app comments and @mentions
- QC ↔ Assembler chat within work item
- Shift handoff notes

**Advanced AR:**
- Measurement tools (distance, angle)
- AR annotations (draw on model)
- X-ray view (see internal structure)
- Animated assembly sequences

**Enterprise Features:**
- Mobile device management (MDM)
- Remote configuration and policy updates
- Multi-language support
- Integration with ERP/MES systems

**Why These Are Deferred:**
- MVP focuses on **core value**: traceable QA with evidence enforcement and offline capability
- Server infrastructure requires ops/devops resources not available for pilot
- Advanced features add complexity that could delay pilot deployment
- Pilot feedback will inform which enhancements deliver highest ROI

## Success Criteria

The MVP is considered successful if:

1. **Operators work independently:** Assemblers, QC, and Supervisor can complete workflows without engineer assistance
2. **Evidence is enforced:** QC cannot bypass evidence policy (verified via pilot testing)
3. **Offline works:** Full shift can be completed without network, with complete export at end
4. **AR is usable:** Operators can align and inspect in <2 minutes on target device
5. **Traceability is complete:** Every work item has full timeline with who/when/what
6. **Pilot deploys successfully:** 1–2 days of on-site testing with no critical blockers

## Technology Stack

**Platform:** Android (target: Pixel 6+ class devices with ARCore support)

**Language:** Kotlin

**Architecture:**
- Event sourcing + CQRS (Command Query Responsibility Segregation)
- Multi-module Android project (app, core:domain, core:data, feature:*)
- Dependency injection: Hilt (recommended) or Koin

**Key Libraries:**
- **Room** — Local SQLite database with compile-time SQL verification
- **CameraX** — Camera preview and photo capture
- **MLKit Barcode Scanner** or **ZXing** — QR/barcode recognition
- **ARCore** + **Sceneform** (or filament) — Augmented reality rendering
- **Jetpack Compose** (recommended) or XML layouts — UI
- **Kotlin Coroutines + Flow** — Async operations and reactive data
- **Gson** or **Kotlinx Serialization** — JSON serialization for export

**Hardware Requirements:**
- Android device with:
  - ARCore support (for AR features)
  - Good camera (barcode scanning + evidence photos)
  - 4+ GB RAM (AR rendering)
  - 64+ GB storage (evidence files)

## Next Steps

**For Developers:**
1. Read this overview to understand the problem and solution
2. Review `stage.md` for detailed sprint-by-sprint implementation plan
3. Review `MODULES.md` to understand code organization
4. Review `FILE_OVERVIEW.md` for practical "where to find/add things" guide
5. Set up development environment (Android Studio, ARCore emulator or test device)
6. Begin Sprint 1 tasks

**For Project Managers:**
1. Confirm MVP scope aligns with pilot requirements
2. Identify 3–5 operators for pilot testing (1–2 per role)
3. Prepare test environment: parts with barcodes/QR codes, AR markers
4. Plan 1–2 day on-site pilot testing window after Sprint 6
5. Define success criteria for pilot → production decision

**For Stakeholders:**
1. Understand that MVP is offline-first (no server required)
2. Plan for export package handling (where to store daily reports)
3. Consider future server infrastructure if real-time multi-device sync is needed
4. Review out-of-scope features and prioritize for post-MVP roadmap

---

## Questions?

For detailed implementation questions, see:
- **stage.md** — Sprint-by-sprint roadmap
- **MODULES.md** — Module structure and responsibilities
- **FILE_OVERVIEW.md** — Where to find/add code

For architectural questions, refer back to the **Core Principles** section above.
