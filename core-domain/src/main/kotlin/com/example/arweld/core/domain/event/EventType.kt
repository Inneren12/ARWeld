package com.example.arweld.core.domain.event

/**
 * Enumerates all domain events captured in the ARWeld workflow.
 */
enum class EventType {
    WORK_CLAIMED,
    WORK_STARTED,
    WORK_READY_FOR_QC,
    QC_STARTED,
    QC_PASSED,
    QC_FAILED_REWORK,
    REWORK_STARTED,
    ISSUE_CREATED,
    EVIDENCE_CAPTURED,
    AR_ALIGNMENT_SET,
}
