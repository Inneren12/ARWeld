package com.example.arweld.core.domain.policy

import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType

/**
 * Policy guard that validates QC evidence requirements before allowing pass/fail decisions.
 */
class QcEvidencePolicy {

    fun check(
        workItemId: String,
        events: List<Event>,
        evidenceList: List<Evidence>,
    ): QcEvidencePolicyResult {
        val requiredEvidence = listOf(
            EvidenceRequirement(
                kind = EvidenceKind.AR_SCREENSHOT,
                reason = "Capture at least one AR screenshot after QC start.",
            ),
            EvidenceRequirement(
                kind = EvidenceKind.PHOTO,
                reason = "Capture at least one photo after QC start.",
            ),
        )

        val qcStartedAt = events
            .asSequence()
            .filter { it.workItemId == workItemId && it.type == EventType.QC_STARTED }
            .maxByOrNull { it.timestamp }
            ?.timestamp
            ?: return QcEvidencePolicyResult.Failed(
                listOf("QC_STARTED event is required before evaluating evidence for work item $workItemId."),
            )

        val evidenceAfterQcStart = evidenceList.filter { it.createdAt >= qcStartedAt }

        val missingReasons = requiredEvidence
            .filter { requirement -> evidenceAfterQcStart.none { it.kind == requirement.kind } }
            .map { it.reason }

        return if (missingReasons.isEmpty()) {
            QcEvidencePolicyResult.Ok
        } else {
            QcEvidencePolicyResult.Failed(missingReasons)
        }
    }
}

sealed class QcEvidencePolicyResult {
    object Ok : QcEvidencePolicyResult()
    data class Failed(val reasons: List<String>) : QcEvidencePolicyResult()
}

private data class EvidenceRequirement(
    val kind: EvidenceKind,
    val reason: String,
)
