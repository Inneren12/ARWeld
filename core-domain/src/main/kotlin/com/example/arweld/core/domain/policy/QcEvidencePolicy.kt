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
        val qcStartedAt = events
            .asSequence()
            .filter { it.workItemId == workItemId && it.type == EventType.QC_STARTED }
            .maxByOrNull { it.timestamp }
            ?.timestamp
            ?: return QcEvidencePolicyResult.Failed(
                listOf("QC_STARTED event is required before evaluating evidence for work item $workItemId."),
            )

        val evidenceAfterQcStart = evidenceList.filter { it.createdAt >= qcStartedAt }

        val missingReasons = mutableListOf<String>()

        if (evidenceAfterQcStart.none { it.kind == EvidenceKind.AR_SCREENSHOT }) {
            missingReasons += "Capture at least one AR screenshot after QC start."
        }

        if (evidenceAfterQcStart.none { it.kind == EvidenceKind.PHOTO }) {
            missingReasons += "Capture at least one photo after QC start."
        }

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
