package com.example.arweld.core.data.work

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.core.domain.work.WorkRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkRepositoryImpl @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
) : WorkRepository {

    override suspend fun getWorkItemByCode(code: String): WorkItem? {
        return workItemDao.getByCode(code)?.toDomain()
    }

    override suspend fun getWorkItemState(workItemId: String): WorkItemState {
        val events = eventDao.getByWorkItemId(workItemId).map { it.toDomain() }
        return reduce(events)
    }

    override suspend fun getMyQueue(userId: String): List<WorkItemState> {
        val events = eventDao.getLastEventsByUser(userId)
        val workItemIds = events.map { it.workItemId }.toSet()

        // v1 definition: items the user has touched recently that are not yet approved
        return workItemIds
            .map { workItemId -> getWorkItemState(workItemId) }
            .filter { state -> state.status != WorkStatus.APPROVED }
    }

    override suspend fun getQcQueue(): List<WorkItemState> {
        val workItems = workItemDao.observeAll().first()
        // Simple v1 approach: derive state for every WorkItem and filter. Can be optimized with
        // targeted queries in a later sprint if performance needs arise.
        return workItems
            .map { entity -> getWorkItemState(entity.id) }
            .filter { state ->
                state.status == WorkStatus.READY_FOR_QC || state.status == WorkStatus.QC_IN_PROGRESS
            }
    }

    private fun WorkItemEntity.toDomain() = WorkItem(
        id = id,
        code = code ?: "",
        type = WorkItemType.valueOf(type),
        description = description ?: "",
        zone = zoneId,
        nodeId = nodeId,
        createdAt = createdAt ?: 0L,
    )

    private fun EventEntity.toDomain() = Event(
        id = id,
        workItemId = workItemId,
        type = EventType.valueOf(type),
        timestamp = timestamp,
        actorId = actorId,
        actorRole = Role.valueOf(actorRole),
        deviceId = deviceId,
        payloadJson = payloadJson,
    )
}
