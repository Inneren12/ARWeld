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
import com.example.arweld.core.domain.work.model.QcChecklistResult
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class FailQcInput(
    val workItemId: String,
    val checklist: QcChecklistResult,
    val reasons: List<String>,
    val priority: Int,
    val comment: String?,
)

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

    suspend operator fun invoke(
        input: FailQcInput,
    ): QcDecisionResult {
        val policyState = qcEvidencePolicy.evaluate(input.workItemId)
        if (!policyState.satisfied) {
            return QcDecisionResult.MissingEvidence(policyState.missing)
        }

        val events = eventRepository.getEventsForWorkItem(input.workItemId)

        val inspector = authRepository.currentUser() ?: error("User must be logged in")
        require(inspector.role == Role.QC) { "Only QC inspectors can mark fail" }

        val event = Event(
            id = UUID.randomUUID().toString(),
            workItemId = input.workItemId,
            type = EventType.QC_FAILED_REWORK,
            timestamp = timeProvider.nowMillis(),
            actorId = inspector.id,
            actorRole = inspector.role,
            deviceId = deviceInfoProvider.deviceId,
            payloadJson = buildPayloadJson(input),
        )

        eventRepository.appendEvent(event)

        return QcDecisionResult.Success
    }

    private fun buildPayloadJson(input: FailQcInput): String {
        val payload = FailQcPayload(
            checklist = buildChecklistSummary(input.checklist),
            reasons = input.reasons,
            priority = input.priority,
            comment = input.comment,
        )
        return Json.encodeToString(payload)
    }
}

private data class FailQcPayload(
    val checklist: ChecklistSummary,
    val reasons: List<String>,
    val priority: Int,
    val comment: String?,
)
