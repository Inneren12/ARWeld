package com.example.arweld.core.data.repository

import android.net.Uri
import androidx.core.net.toFile
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.evidence.toDomain
import com.example.arweld.core.data.evidence.toEntity
import com.example.arweld.core.data.file.computeSha256
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
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
    private val timeProvider: TimeProvider,
) : EvidenceRepository {

    override suspend fun saveEvidence(evidence: Evidence) {
        evidenceDao.insert(evidence.toEntity())
    }

    override suspend fun savePhoto(eventId: String, file: File): Evidence {
        val sha256 = computeSha256(file)
        val evidence = Evidence(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            kind = EvidenceKind.PHOTO,
            uri = file.toURI().toString(),
            sha256 = sha256,
            metaJson = null,
            createdAt = timeProvider.nowMillis(),
        )

        saveEvidence(evidence)
        return evidence
    }

    override suspend fun saveArScreenshot(
        eventId: String,
        uri: Uri,
        meta: ArScreenshotMeta,
    ): Evidence {
        val file = uri.toFile()
        val sha256 = computeSha256(file)
        val evidence = Evidence(
            id = UUID.randomUUID().toString(),
            eventId = eventId,
            kind = EvidenceKind.AR_SCREENSHOT,
            uri = uri.toString(),
            sha256 = sha256,
            metaJson = Json.encodeToString(meta),
            createdAt = timeProvider.nowMillis(),
        )

        saveEvidence(evidence)
        return evidence
    }

    override suspend fun saveAll(evidenceList: List<Evidence>) {
        if (evidenceList.isEmpty()) return
        evidenceDao.insertAll(evidenceList.map { it.toEntity() })
    }

    override suspend fun getEvidenceForEvent(eventId: String): List<Evidence> {
        return evidenceDao.listByEvent(eventId).map { it.toDomain() }
    }
}
