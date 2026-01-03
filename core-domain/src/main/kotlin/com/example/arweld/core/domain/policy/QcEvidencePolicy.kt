package com.example.arweld.core.domain.policy

import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository

class QcEvidencePolicy(
    private val evidenceRepository: EvidenceRepository,
) {

    data class PolicyState(
        val satisfied: Boolean,
        val missing: Set<EvidenceKind>,
    )

    suspend fun evaluate(workItemId: String): PolicyState {
        val counts = evidenceRepository.countsByKindForWorkItem(workItemId)
        val missing = REQUIRED_KINDS.filter { kind -> (counts[kind] ?: 0) <= 0 }.toSet()
        return PolicyState(
            satisfied = missing.isEmpty(),
            missing = missing,
        )
    }

    companion object {
        private val REQUIRED_KINDS = setOf(
            EvidenceKind.PHOTO,
            EvidenceKind.AR_SCREENSHOT,
        )
    }
}
