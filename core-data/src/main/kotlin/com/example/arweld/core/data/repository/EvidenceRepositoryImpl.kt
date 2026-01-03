package com.example.arweld.core.data.repository

import android.net.Uri
import androidx.core.net.toFile
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.evidence.toDomain
import com.example.arweld.core.data.evidence.toEntity
import com.example.arweld.core.data.file.computeSha256
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room-backed implementation of EvidenceRepository that stores evidence metadata.
 * File capture is handled upstream; this class computes hashes and persists records.
 */
@Singleton
class EvidenceRepositoryImpl @Inject constructor(
    private val evidenceDao: EvidenceDao,
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val timeProvider: TimeProvider,
) : EvidenceRepository {

    override suspend fun saveEvidence(evidence: Evidence) {
        evidenceDao.insert(evidence.toEntity())
    }

    override suspend fun savePhoto(workItemId: String, eventId: String, file: File): Evidence {
        val sha256 = computeSha256(file)
        val createdAt = timeProvider.nowMillis()
        val evidence = Evidence(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            eventId = eventId,
            kind = EvidenceKind.PHOTO,
            uri = file.toURI().toString(),
            sha256 = sha256,
            sizeBytes = file.length(),
            metaJson = null,
            createdAt = createdAt,
        )

        persistEvidence(evidence)
        return evidence
    }

    override suspend fun saveArScreenshot(
        workItemId: String,
        eventId: String,
        uri: Uri,
        meta: ArScreenshotMeta,
    ): Evidence {
        val file = uri.toFile()
        val sha256 = computeSha256(file)
        val createdAt = timeProvider.nowMillis()
        val evidence = Evidence(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            eventId = eventId,
            kind = EvidenceKind.AR_SCREENSHOT,
            uri = uri.toString(),
            sha256 = sha256,
            sizeBytes = file.length(),
            metaJson = Json.encodeToString(meta),
            createdAt = createdAt,
        )

        persistEvidence(evidence)
        return evidence
    }

    override suspend fun saveAll(evidenceList: List<Evidence>) {
        if (evidenceList.isEmpty()) return
        evidenceDao.insertAll(evidenceList.map { it.toEntity() })
    }

    override suspend fun getEvidenceForEvent(eventId: String): List<Evidence> {
        return evidenceDao.listByEvent(eventId).map { it.toDomain() }
    }

    override suspend fun getEvidenceForWorkItem(workItemId: String): List<Evidence> {
        return evidenceDao.listByWorkItem(workItemId).map { it.toDomain() }
    }

    override suspend fun countsByKindForWorkItem(workItemId: String): Map<EvidenceKind, Int> {
        return evidenceDao.listByWorkItem(workItemId)
            .groupingBy { EvidenceKind.valueOf(it.kind) }
            .eachCount()
    }

    private suspend fun persistEvidence(evidence: Evidence) {
        saveEvidence(evidence)
        appendEvidenceCapturedEvent(evidence)
    }

    private suspend fun appendEvidenceCapturedEvent(evidence: Evidence) {
        val user = authRepository.currentUser() ?: error("User must be logged in")
        val payload = EvidenceCapturedPayload(
            evidenceId = evidence.id,
            attachedEventId = evidence.eventId,
            sha256 = evidence.sha256,
            kind = evidence.kind,
            fileName = evidence.uri.substringAfterLast('/'),
            uri = evidence.uri,
            sizeBytes = evidence.sizeBytes,
        )

        val event = Event(
            id = UUID.randomUUID().toString(),
            workItemId = evidence.workItemId,
            type = EventType.EVIDENCE_CAPTURED,
            timestamp = evidence.createdAt,
            actorId = user.id,
            actorRole = user.role,
            deviceId = deviceInfoProvider.deviceId,
            payloadJson = Json.encodeToString(payload),
        )

        eventRepository.appendEvent(event)
    }
}

@kotlinx.serialization.Serializable
private data class EvidenceCapturedPayload(
    val evidenceId: String,
    val attachedEventId: String,
    val sha256: String,
    val kind: EvidenceKind,
    val fileName: String,
    val uri: String,
    val sizeBytes: Long,
)
