package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.policy.QcEvidencePolicyException
import com.example.arweld.core.domain.policy.QcEvidencePolicyResult

internal suspend fun ensureEvidencePolicySatisfied(
    workItemId: String,
    events: List<Event>,
    evidenceRepository: EvidenceRepository,
    qcEvidencePolicy: QcEvidencePolicy,
) {
    val evidence = gatherEvidence(events, evidenceRepository)
    val validation = qcEvidencePolicy.check(workItemId, events, evidence)
    if (validation is QcEvidencePolicyResult.Failed) {
        throw QcEvidencePolicyException(validation.reasons)
    }
}

private suspend fun gatherEvidence(
    events: List<Event>,
    evidenceRepository: EvidenceRepository,
): List<Evidence> {
    if (events.isEmpty()) return emptyList()

    val allEvidence = mutableListOf<Evidence>()
    for (event in events) {
        allEvidence += evidenceRepository.getEvidenceForEvent(event.id)
    }
    return allEvidence
}
