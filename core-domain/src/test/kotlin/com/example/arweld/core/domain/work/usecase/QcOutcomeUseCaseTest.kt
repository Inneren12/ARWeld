package com.example.arweld.core.domain.work.usecase

import android.net.Uri
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.policy.QcEvidencePolicyException
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistItem
import com.example.arweld.core.domain.work.model.QcChecklistResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class QcOutcomeUseCaseTest {

    private val qcUser = User(
        id = "qc-1",
        username = "qc.one",
        displayName = "QC One",
        role = Role.QC,
    )
    private val timeProvider = TimeProvider { 2_000L }
    private val deviceInfoProvider = DeviceInfoProvider { "device-123" }
    private val qcEvidencePolicy = QcEvidencePolicy()

    @Test
    fun `pass QC throws when evidence policy fails`() = runBlocking {
        val workItemId = "work-1"
        val qcStarted = qcStartedEvent(workItemId)
        val eventRepository = FakeEventRepository(mutableListOf(qcStarted))
        val evidenceRepository = FakeEvidenceRepository()
        val authRepository = FakeAuthRepository(qcUser)

        val useCase = PassQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )

        assertThrows(QcEvidencePolicyException::class.java) {
            runBlocking {
                useCase(
                    PassQcInput(
                        workItemId = workItemId,
                        checklist = sampleChecklist(),
                        comment = "missing evidence",
                    ),
                )
            }
        }
        val events = eventRepository.getEventsForWorkItem(workItemId)
        assertEquals(1, events.size) // Only QC_STARTED remains
    }

    @Test
    fun `fail QC throws when evidence policy fails`() = runBlocking {
        val workItemId = "work-1"
        val qcStarted = qcStartedEvent(workItemId)
        val eventRepository = FakeEventRepository(mutableListOf(qcStarted))
        val evidenceRepository = FakeEvidenceRepository()
        val authRepository = FakeAuthRepository(qcUser)

        val useCase = FailQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )

        assertThrows(QcEvidencePolicyException::class.java) {
            runBlocking {
                useCase(
                    FailQcInput(
                        workItemId = workItemId,
                        checklist = sampleChecklist(),
                        reasons = listOf("missing evidence"),
                        priority = 1,
                        comment = "missing evidence",
                    ),
                )
            }
        }

        val events = eventRepository.getEventsForWorkItem(workItemId)
        assertEquals(1, events.size) // Only QC_STARTED remains
    }

    @Test
    fun `pass QC appends event when evidence policy is satisfied`() = runBlocking {
        val workItemId = "work-2"
        val qcStarted = qcStartedEvent(workItemId)
        val eventRepository = FakeEventRepository(mutableListOf(qcStarted))
        val evidenceRepository = FakeEvidenceRepository(
            mapOf(
                qcStarted.id to listOf(
                    photoEvidence(eventId = qcStarted.id, createdAt = 1_500L),
                    arScreenshotEvidence(eventId = qcStarted.id, createdAt = 1_600L),
                ),
            ),
        )
        val authRepository = FakeAuthRepository(qcUser)

        val useCase = PassQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )

        useCase(
            PassQcInput(
                workItemId = workItemId,
                checklist = sampleChecklist(),
                comment = "all good",
            ),
        )

        val events = eventRepository.getEventsForWorkItem(workItemId)
        assertEquals(2, events.size)
        val qcPassed = events.last()
        assertEquals(EventType.QC_PASSED, qcPassed.type)
        val payload = Json.parseToJsonElement(qcPassed.payloadJson!!).jsonObject
        val checklist = payload.getValue("checklist").jsonObject
        val totals = checklist.getValue("totals").jsonObject
        assertEquals(1, totals.getValue("ok").jsonPrimitive.int)
        assertEquals(1, totals.getValue("notOk").jsonPrimitive.int)
        assertEquals(1, totals.getValue("na").jsonPrimitive.int)

        val items = checklist.getValue("items").jsonArray.map { it.jsonObject }
        assertEquals(
            listOf("geometry", "fasteners", "marking"),
            items.map { it.getValue("id").jsonPrimitive.content },
        )
        assertEquals(
            listOf("OK", "NOT_OK", "NA"),
            items.map { it.getValue("state").jsonPrimitive.content },
        )
        assertEquals("all good", payload.getValue("comment").jsonPrimitive.content)
        assertEquals(deviceInfoProvider.deviceId, qcPassed.deviceId)
    }

    @Test
    fun `fail QC appends event when evidence policy is satisfied`() = runBlocking {
        val workItemId = "work-3"
        val qcStarted = qcStartedEvent(workItemId)
        val eventRepository = FakeEventRepository(mutableListOf(qcStarted))
        val evidenceRepository = FakeEvidenceRepository(
            mapOf(
                qcStarted.id to listOf(
                    photoEvidence(eventId = qcStarted.id, createdAt = 1_200L),
                    arScreenshotEvidence(eventId = qcStarted.id, createdAt = 1_300L),
                ),
            ),
        )
        val authRepository = FakeAuthRepository(qcUser)

        val useCase = FailQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )

        useCase(
            FailQcInput(
                workItemId = workItemId,
                checklist = sampleChecklist(),
                reasons = listOf("fasteners loose", "marking missing"),
                priority = 2,
                comment = "fix before ship",
            ),
        )

        val events = eventRepository.getEventsForWorkItem(workItemId)
        assertEquals(2, events.size)
        val qcFailed = events.last()
        assertEquals(EventType.QC_FAILED_REWORK, qcFailed.type)
        val payload = Json.parseToJsonElement(qcFailed.payloadJson!!).jsonObject
        val checklist = payload.getValue("checklist").jsonObject
        val totals = checklist.getValue("totals").jsonObject
        assertEquals(1, totals.getValue("ok").jsonPrimitive.int)
        assertEquals(1, totals.getValue("notOk").jsonPrimitive.int)
        assertEquals(1, totals.getValue("na").jsonPrimitive.int)

        val items = checklist.getValue("items").jsonArray.map { it.jsonObject }
        assertEquals(
            listOf("geometry", "fasteners", "marking"),
            items.map { it.getValue("id").jsonPrimitive.content },
        )
        assertEquals(
            listOf("OK", "NOT_OK", "NA"),
            items.map { it.getValue("state").jsonPrimitive.content },
        )
        val reasons = payload.getValue("reasons").jsonArray.map { it.jsonPrimitive.content }
        assertEquals(listOf("fasteners loose", "marking missing"), reasons)
        assertEquals(2, payload.getValue("priority").jsonPrimitive.int)
        assertEquals("fix before ship", payload.getValue("comment").jsonPrimitive.content)
        assertEquals(deviceInfoProvider.deviceId, qcFailed.deviceId)
    }

    private fun sampleChecklist(): QcChecklistResult {
        return QcChecklistResult(
            items = listOf(
                QcChecklistItem(id = "geometry", state = QcCheckState.OK),
                QcChecklistItem(id = "fasteners", state = QcCheckState.NOT_OK),
                QcChecklistItem(id = "marking", state = QcCheckState.NA),
            ),
        )
    }

    private fun qcStartedEvent(workItemId: String): Event {
        return Event(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            type = EventType.QC_STARTED,
            timestamp = 1_000L,
            actorId = qcUser.id,
            actorRole = qcUser.role,
            deviceId = deviceInfoProvider.deviceId,
            payloadJson = null,
        )
    }

    private fun photoEvidence(eventId: String, createdAt: Long): Evidence {
        return Evidence(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            kind = EvidenceKind.PHOTO,
            uri = "file://evidence/photo.jpg",
            sha256 = "sha-photo",
            metaJson = null,
            createdAt = createdAt,
        )
    }

    private fun arScreenshotEvidence(eventId: String, createdAt: Long): Evidence {
        return Evidence(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            kind = EvidenceKind.AR_SCREENSHOT,
            uri = "file://evidence/ar.png",
            sha256 = "sha-ar",
            metaJson = """{"trackingState":"TRACKING"}""",
            createdAt = createdAt,
        )
    }
}

private class FakeEventRepository(
    initialEvents: MutableList<Event> = mutableListOf(),
) : EventRepository {
    private val events = initialEvents

    override suspend fun appendEvent(event: Event) {
        events.add(event)
    }

    override suspend fun appendEvents(events: List<Event>) {
        this.events.addAll(events)
    }

    override suspend fun getEventsForWorkItem(workItemId: String): List<Event> {
        return events
            .filter { it.workItemId == workItemId }
            .sortedWith(compareBy<Event> { it.timestamp }.thenBy { it.id })
    }
}

private class FakeEvidenceRepository(
    initialEvidence: Map<String, List<Evidence>> = emptyMap(),
) : EvidenceRepository {

    private val evidenceStore: MutableMap<String, MutableList<Evidence>> =
        initialEvidence.mapValues { it.value.toMutableList() }.toMutableMap()

    override suspend fun saveEvidence(evidence: Evidence) {
        evidenceStore.getOrPut(evidence.eventId) { mutableListOf() }.add(evidence)
    }

    override suspend fun savePhoto(eventId: String, file: File): Evidence {
        error("Not implemented in fake")
    }

    override suspend fun saveArScreenshot(
        eventId: String,
        uri: Uri,
        meta: ArScreenshotMeta,
    ): Evidence {
        error("Not implemented in fake")
    }

    override suspend fun saveAll(evidenceList: List<Evidence>) {
        evidenceList.forEach { saveEvidence(it) }
    }

    override suspend fun getEvidenceForEvent(eventId: String): List<Evidence> {
        return evidenceStore[eventId]?.toList() ?: emptyList()
    }
}

private class FakeAuthRepository(
    private var user: User?,
) : AuthRepository {
    override suspend fun loginMock(role: Role): User {
        throw UnsupportedOperationException("Not used in fake")
    }

    override suspend fun currentUser(): User? = user

    override suspend fun logout() {
        user = null
    }
}
