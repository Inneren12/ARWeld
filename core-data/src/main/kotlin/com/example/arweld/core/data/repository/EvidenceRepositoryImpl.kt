package com.example.arweld.core.data.repository

import com.example.arweld.core.domain.model.Evidence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EvidenceRepository.
 * This is a stub implementation for S1-02.
 * Full implementation with Room will be added in later sprints.
 */
@Singleton
class EvidenceRepositoryImpl @Inject constructor() : EvidenceRepository {

    override suspend fun insert(evidence: Evidence) {
        // Stub implementation
    }

    override suspend fun getByWorkItem(workItemId: String): List<Evidence> {
        // Stub implementation
        return emptyList()
    }

    override fun observeByWorkItem(workItemId: String): Flow<List<Evidence>> {
        // Stub implementation
        return flowOf(emptyList())
    }
}
