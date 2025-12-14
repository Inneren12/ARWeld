package com.example.arweld.core.domain.evidence

import kotlinx.serialization.Serializable

/**
 * Domain model representing a captured piece of evidence attached to a QC or other event.
 */
@Serializable
data class Evidence(
    val id: String,
    val eventId: String,
    val kind: EvidenceKind,
    val uri: String,
    val sha256: String,
    val metaJson: String?,
    val createdAt: Long,
)

/**
 * Helper to quickly determine if the evidence is visual (contains imagery or video).
 */
fun Evidence.isVisual(): Boolean = when (kind) {
    EvidenceKind.PHOTO,
    EvidenceKind.AR_SCREENSHOT,
    EvidenceKind.VIDEO -> true
    EvidenceKind.MEASUREMENT -> false
}
