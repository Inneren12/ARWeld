package com.example.arweld.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Immutable event representing a state change in the system.
 * Events are the source of truth for WorkItem state.
 */
@Serializable
data class Event(
    val id: String,
    val workItemId: String,
    val type: EventType,
    val actorId: String,           // User who performed the action
    val deviceId: String,
    val timestamp: Long,           // Unix timestamp in milliseconds
    val payload: Map<String, String> = emptyMap()
)

/**
 * Types of events that can occur in the system.
 */
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
