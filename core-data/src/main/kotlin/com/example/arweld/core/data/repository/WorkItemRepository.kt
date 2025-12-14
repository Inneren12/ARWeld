package com.example.arweld.core.data.repository

import com.example.arweld.core.domain.model.WorkItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for WorkItem data access.
 * Implementations will handle Room database operations.
 */
interface WorkItemRepository {
    suspend fun insert(workItem: WorkItem)
    suspend fun getById(id: String): WorkItem?
    suspend fun findByCode(code: String): WorkItem?
    fun observeAll(): Flow<List<WorkItem>>
}
