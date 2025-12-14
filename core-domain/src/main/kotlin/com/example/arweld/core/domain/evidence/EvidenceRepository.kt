package com.example.arweld.core.domain.evidence

/**
 * Domain-facing repository for capturing and retrieving evidence metadata.
 * File I/O is handled elsewhere; this repository only stores metadata records.
 */
interface EvidenceRepository {
    /**
     * Persist a single evidence record.
     */
    suspend fun saveEvidence(evidence: Evidence)

    /**
     * Persist multiple evidence records in a batch.
     */
    suspend fun saveAll(evidenceList: List<Evidence>)

    /**
     * Fetch all evidence attached to the given event.
     */
    suspend fun getEvidenceForEvent(eventId: String): List<Evidence>
}
