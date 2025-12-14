package com.example.arweld.core.data.repository

import com.example.arweld.core.domain.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Event data access.
 * Events are the source of truth for WorkItem state changes.
 */
interface EventRepository {
    suspend fun insert(event: Event)
    suspend fun getByWorkItem(workItemId: String): List<Event>
    fun observeByWorkItem(workItemId: String): Flow<List<Event>>
}
