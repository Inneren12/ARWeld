package com.example.arweld.core.data.repository

import com.example.arweld.core.domain.model.Evidence
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Evidence data access.
 * Manages evidence files (photos, AR screenshots) and their metadata.
 */
interface EvidenceRepository {
    suspend fun insert(evidence: Evidence)
    suspend fun getByWorkItem(workItemId: String): List<Evidence>
    fun observeByWorkItem(workItemId: String): Flow<List<Evidence>>
}
