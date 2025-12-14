package com.example.arweld.core.data.repository

import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.evidence.toDomain
import com.example.arweld.core.data.evidence.toEntity
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of EvidenceRepository for metadata storage only.
 * Actual file I/O is handled by other components.
 */
@Singleton
class EvidenceRepositoryImpl @Inject constructor(
    private val evidenceDao: EvidenceDao
) : EvidenceRepository {

    override suspend fun saveEvidence(evidence: Evidence) {
        evidenceDao.insert(evidence.toEntity())
    }

    override suspend fun saveAll(evidenceList: List<Evidence>) {
        if (evidenceList.isEmpty()) return
        evidenceDao.insertAll(evidenceList.map { it.toEntity() })
    }

    override suspend fun getEvidenceForEvent(eventId: String): List<Evidence> {
        return evidenceDao.getByEventId(eventId).map { it.toDomain() }
    }
}
