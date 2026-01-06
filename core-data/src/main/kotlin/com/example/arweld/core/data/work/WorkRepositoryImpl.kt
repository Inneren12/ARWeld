package com.example.arweld.core.data.work

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.event.Event
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

    override suspend fun getByCode(code: String): WorkItem? {
        return workItemDao.getByCode(code)?.toDomain()
    }

    override suspend fun getById(id: String): WorkItem? {
        return workItemDao.getById(id)?.toDomain()
    }

    override suspend fun getWorkItemState(workItemId: String): WorkItemState {
        val events = eventDao.getByWorkItemId(workItemId).map { it.toDomain() }
        return reduce(events)
    }

    override suspend fun listByStatus(status: WorkStatus): List<WorkItemState> {
        return allWorkItemStates().filter { it.status == status }
    }

    override suspend fun listMyQueue(userId: String): List<WorkItemState> {
        val events = eventDao.getLastEventsByUser(userId)
        val workItemIds = events.map { it.workItemId }.toSet()

        // v1 definition: items the user has touched recently that are not yet approved
        return workItemIds
            .map { workItemId -> getWorkItemState(workItemId) }
            .filter { state -> state.status != WorkStatus.APPROVED }
    }

    override suspend fun listQcQueue(): List<WorkItemState> {
        return allWorkItemStates().filter { state ->
            state.status == WorkStatus.READY_FOR_QC || state.status == WorkStatus.QC_IN_PROGRESS
        }
    }

    private suspend fun allWorkItemStates(): List<WorkItemState> {
        val workItems = workItemDao.observeAll().first()
        return workItems.map { entity -> getWorkItemState(entity.id) }
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
}
