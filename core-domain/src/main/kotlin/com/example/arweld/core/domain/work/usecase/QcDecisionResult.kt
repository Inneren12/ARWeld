package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.evidence.EvidenceKind

sealed interface QcDecisionResult {
    data object Success : QcDecisionResult
    data class MissingEvidence(val missing: Set<EvidenceKind>) : QcDecisionResult
}
