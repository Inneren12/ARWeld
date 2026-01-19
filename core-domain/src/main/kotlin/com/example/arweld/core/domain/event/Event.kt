package com.example.arweld.core.domain.event

import com.example.arweld.core.domain.model.Role
import kotlinx.serialization.Serializable

/**
 * Immutable domain event. Acts as the source of truth for state reconstruction.
 *
 * @param timestamp milliseconds since Unix epoch
 * @param payloadJson optional JSON-encoded payload with event-specific details
 */
@Serializable
data class Event(
    val id: String,
    val workItemId: String,
    val type: EventType,
    val timestamp: Long,
    val actorId: String,
    val actorRole: Role,
    val deviceId: String,
    val payloadJson: String? = null,
)

/**
 * Domain helpers for working with events.
 */
fun Event.isQcEvent(): Boolean = type in setOf(
    EventType.QC_STARTED,
    EventType.QC_PASSED,
    EventType.QC_FAILED_REWORK,
)
