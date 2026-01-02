package com.example.arweld.core.domain.evidence

import android.net.Uri
import java.io.File
import kotlinx.serialization.Serializable

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
    suspend fun savePhoto(workItemId: String, eventId: String, file: File): Evidence

    /**
     * Persist an AR screenshot evidence file and return the stored metadata.
     */
    suspend fun saveArScreenshot(
        workItemId: String,
        eventId: String,
        uri: Uri,
        meta: ArScreenshotMeta,
    ): Evidence

    /**
     * Persist multiple evidence records in a batch.
     */
    suspend fun saveAll(evidenceList: List<Evidence>)

    /**
     * Fetch all evidence attached to the given event.
     */
    suspend fun getEvidenceForEvent(eventId: String): List<Evidence>

    /**
     * Fetch all evidence tied to a work item regardless of source event.
     */
    suspend fun getEvidenceForWorkItem(workItemId: String): List<Evidence>
}

@Serializable
data class ArScreenshotMeta(
    val markerIds: List<Int>,
    val trackingState: String,
    val alignmentQualityScore: Float,
    val distanceToMarker: Float?,
)
