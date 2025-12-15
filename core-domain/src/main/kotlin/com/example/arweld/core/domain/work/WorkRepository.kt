package com.example.arweld.core.domain.work

import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.state.WorkItemState

/**
 * Facade for retrieving WorkItems and their derived state from the event log.
 *
 * Queue lookups return derived [WorkItemState] instances; the associated `workItemId`
 * can be read from `lastEvent?.workItemId` when needed.
 */
interface WorkRepository {
    suspend fun getWorkItemByCode(code: String): WorkItem?

    suspend fun getWorkItemById(id: String): WorkItem?

    suspend fun getWorkItemState(workItemId: String): WorkItemState

    suspend fun getMyQueue(userId: String): List<WorkItemState>

    suspend fun getQcQueue(): List<WorkItemState>
}
