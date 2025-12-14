package com.example.arweld.core.domain.evidence

import kotlinx.serialization.Serializable

/**
 * Types of evidence that can be captured during QC or related events.
 */
@Serializable
enum class EvidenceKind {
    PHOTO,
    AR_SCREENSHOT,
    VIDEO,
    MEASUREMENT,
}
