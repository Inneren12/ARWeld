package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import java.util.UUID

/**
 * Marks a work item as requiring rework after QC failure, enforcing evidence requirements first.
 */
class FailQcUseCase(
    private val eventRepository: EventRepository,
    private val evidenceRepository: EvidenceRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val qcEvidencePolicy: QcEvidencePolicy,
) {

    /**
     * @param payloadJson optional JSON payload containing notes, checklist, or other QC metadata.
     */
    suspend operator fun invoke(
        workItemId: String,
        payloadJson: String? = null,
    ) {
        val events = eventRepository.getEventsForWorkItem(workItemId)
        ensureEvidencePolicySatisfied(
            workItemId = workItemId,
            events = events,
            evidenceRepository = evidenceRepository,
            qcEvidencePolicy = qcEvidencePolicy,
        )

        val inspector = authRepository.currentUser() ?: error("User must be logged in")
        require(inspector.role == Role.QC) { "Only QC inspectors can mark fail" }

        val event = Event(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            type = EventType.QC_FAILED_REWORK,
            timestamp = timeProvider.nowMillis(),
            actorId = inspector.id,
            actorRole = inspector.role,
            deviceId = deviceInfoProvider.deviceId,
            payloadJson = payloadJson,
        )

        eventRepository.appendEvent(event)
    }
}
