package com.example.arweld.core.domain.work

import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.state.WorkStatus

/**
 * Facade for retrieving WorkItems and their derived state from the event log.
 *
 * Queue lookups return derived [WorkItemState] instances; the associated `workItemId`
 * can be read from `lastEvent?.workItemId` when needed.
 */
interface WorkRepository {
    suspend fun getByCode(code: String): WorkItem?

    suspend fun getById(id: String): WorkItem?

    suspend fun getWorkItemState(workItemId: String): WorkItemState

    suspend fun listByStatus(status: WorkStatus): List<WorkItemState>

    suspend fun listMyQueue(userId: String): List<WorkItemState>

    suspend fun listQcQueue(): List<WorkItemState>

    // Legacy aliases used by existing callers (kept for backward compatibility during S1)
    suspend fun getWorkItemByCode(code: String): WorkItem? = getByCode(code)

    suspend fun getWorkItemById(id: String): WorkItem? = getById(id)

    suspend fun getMyQueue(userId: String): List<WorkItemState> = listMyQueue(userId)

    suspend fun getQcQueue(): List<WorkItemState> = listQcQueue()
}
