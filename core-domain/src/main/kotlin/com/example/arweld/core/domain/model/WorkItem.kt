package com.example.arweld.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a trackable work unit (part, assembly, or operation).
 * WorkItem is the core domain concept that all workflows revolve around.
 */
@Serializable
data class WorkItem(
    val id: String,
    val code: String,              // Barcode/QR/NFC identifier
    val type: WorkItemType,
    val description: String,
    val zone: String? = null,
    val nodeId: String? = null,    // Links to AR 3D model node
    val createdAt: Long            // Unix timestamp in milliseconds
)

enum class WorkItemType {
    PART,
    ASSEMBLY,
    OPERATION
}

/**
 * Derived status of a WorkItem (computed from event log).
 */
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
