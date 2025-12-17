package com.example.arweld.core.domain.state

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType

/**
 * Derived state of a WorkItem computed from the ordered event log.
 */
data class WorkItemState(
    val status: WorkStatus,
    val lastEvent: Event?,
    val currentAssigneeId: String?,
    val qcStatus: QcStatus?,
    val readyForQcSince: Long?,
)

/**
 * High-level lifecycle for a WorkItem.
 */
enum class WorkStatus {
    NEW,
    IN_PROGRESS,
    READY_FOR_QC,
    QC_IN_PROGRESS,
    APPROVED,
    REWORK_REQUIRED,
}

/**
 * Quality-control specific state, tracked separately for clarity.
 */
enum class QcStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PASSED,
    REWORK_REQUIRED,
}

private val initialState = WorkItemState(
    status = WorkStatus.NEW,
    lastEvent = null,
    currentAssigneeId = null,
    qcStatus = QcStatus.NOT_STARTED,
    readyForQcSince = null,
)

/**
 * Pure reducer that derives WorkItemState from an arbitrary list of events.
 *
 * - Sorts events chronologically (timestamp, then id for stability)
 * - Applies deterministic transition rules based on EventType
 * - Does not perform any I/O
 */
fun reduce(events: List<Event>): WorkItemState {
    if (events.isEmpty()) return initialState

    val sortedEvents = events.sortedWith(compareBy<Event> { it.timestamp }.thenBy { it.id })

    return sortedEvents.fold(initialState) { state, event ->
        val updated = when (event.type) {
            EventType.WORK_CLAIMED -> state.copy(
                status = WorkStatus.IN_PROGRESS,
                currentAssigneeId = event.actorId,
                readyForQcSince = null,
            )

            EventType.WORK_STARTED -> state.copy(
                status = WorkStatus.IN_PROGRESS,
                currentAssigneeId = state.currentAssigneeId ?: event.actorId,
                readyForQcSince = null,
            )

            EventType.WORK_READY_FOR_QC -> state.copy(
                status = WorkStatus.READY_FOR_QC,
                qcStatus = QcStatus.NOT_STARTED,
                readyForQcSince = event.timestamp,
            )

            EventType.QC_STARTED -> state.copy(
                status = WorkStatus.QC_IN_PROGRESS,
                qcStatus = QcStatus.IN_PROGRESS,
                readyForQcSince = state.readyForQcSince,
            )

            EventType.QC_PASSED -> state.copy(
                // QC outcome: approved
                status = WorkStatus.APPROVED,
                qcStatus = QcStatus.PASSED,
                readyForQcSince = null,
            )

            EventType.QC_FAILED_REWORK -> state.copy(
                // QC outcome: rework requested
                status = WorkStatus.REWORK_REQUIRED,
                qcStatus = QcStatus.REWORK_REQUIRED,
                readyForQcSince = null,
            )

            EventType.REWORK_STARTED -> state.copy(
                status = WorkStatus.IN_PROGRESS,
                qcStatus = QcStatus.NOT_STARTED,
                readyForQcSince = null,
            )

            EventType.AR_ALIGNMENT_SET, EventType.EVIDENCE_CAPTURED -> state
        }

        updated.copy(lastEvent = event)
    }
}
