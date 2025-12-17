package com.example.arweld.core.domain.evidence

import java.io.File

/**
 * Domain-facing repository for capturing and retrieving evidence metadata.
 * File capture is handled elsewhere; this repository stores metadata and computes hashes for captured files.
 */
interface EvidenceRepository {
    /**
     * Persist a single evidence record.
     */
    suspend fun saveEvidence(evidence: Evidence)

    /**
     * Persist a photo evidence file and return the stored metadata.
     */
    suspend fun savePhoto(eventId: String, file: File): Evidence

    /**
     * Persist multiple evidence records in a batch.
     */
    suspend fun saveAll(evidenceList: List<Evidence>)

    /**
     * Fetch all evidence attached to the given event.
     */
    suspend fun getEvidenceForEvent(eventId: String): List<Evidence>
}
