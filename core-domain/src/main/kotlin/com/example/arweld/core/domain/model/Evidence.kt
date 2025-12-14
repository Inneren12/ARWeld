package com.example.arweld.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a piece of evidence captured during QC inspection.
 */
@Serializable
data class Evidence(
    val id: String,
    val eventId: String,
    val workItemId: String,
    val kind: EvidenceKind,
    val filePath: String,
    val fileHash: String,          // SHA-256 hash for integrity verification
    val capturedAt: Long           // Unix timestamp in milliseconds
)

enum class EvidenceKind {
    PHOTO,
    AR_SCREENSHOT,
    VIDEO,
    SENSOR_DATA
}
