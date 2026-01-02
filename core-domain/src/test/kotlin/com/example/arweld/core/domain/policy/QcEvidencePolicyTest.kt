package com.example.arweld.core.domain.policy

import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QcEvidencePolicyTest {

    private val policy = QcEvidencePolicy()

    @Test
    fun `fails when QC_STARTED is missing`() {
        val result = policy.check(
            workItemId = "work-1",
            events = emptyList(),
            evidenceList = emptyList(),
        )

        assertTrue(result is QcEvidencePolicyResult.Failed)
        val reasons = (result as QcEvidencePolicyResult.Failed).reasons
        assertEquals(
            listOf("QC_STARTED event is required before evaluating evidence for work item work-1."),
            reasons,
        )
    }

    @Test
    fun `requires AR screenshot and photo after QC start`() {
        val qcStarted = event(
            id = "e1",
            workItemId = "work-1",
            type = EventType.QC_STARTED,
            timestamp = 1_000L,
        )

        val result = policy.check(
            workItemId = "work-1",
            events = listOf(qcStarted),
            evidenceList = listOf(
                evidence("ar-before", EvidenceKind.AR_SCREENSHOT, createdAt = 500L),
                evidence("photo-after", EvidenceKind.PHOTO, createdAt = 1_500L),
            ),
        )

        assertTrue(result is QcEvidencePolicyResult.Failed)
        val reasons = (result as QcEvidencePolicyResult.Failed).reasons
        assertEquals(listOf("Capture at least one AR screenshot after QC start."), reasons)
    }

    @Test
    fun `passes when both evidence types captured after QC start`() {
        val qcStarted = event(
            id = "e1",
            workItemId = "work-1",
            type = EventType.QC_STARTED,
            timestamp = 1_000L,
        )

        val result = policy.check(
            workItemId = "work-1",
            events = listOf(qcStarted),
            evidenceList = listOf(
                evidence("ar-after", EvidenceKind.AR_SCREENSHOT, createdAt = 1_500L),
                evidence("photo-after", EvidenceKind.PHOTO, createdAt = 1_600L),
            ),
        )

        assertTrue(result is QcEvidencePolicyResult.Ok)
    }

    private fun event(
        id: String,
        workItemId: String,
        type: EventType,
        timestamp: Long,
    ): Event {
        return Event(
            id = id,
            workItemId = workItemId,
            type = type,
            timestamp = timestamp,
            actorId = "user-1",
            actorRole = Role.QC,
            deviceId = "device-1",
            payloadJson = null,
        )
    }

    private fun evidence(
        id: String,
        kind: EvidenceKind,
        createdAt: Long,
    ): Evidence {
        return Evidence(
            id = id,
            workItemId = workItemId,
            eventId = "event-$id",
            kind = kind,
            uri = "file://$id",
            sha256 = "hash-$id",
            sizeBytes = 1024L,
            metaJson = null,
            createdAt = createdAt,
        )
    }
}
