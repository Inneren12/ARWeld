package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.SupervisorWorkItem
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Loads supervisor work list entries by joining work items with derived state.
 */
class GetSupervisorWorkListUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val userDao: UserDao,
) {
    suspend operator fun invoke(): List<SupervisorWorkItem> {
        val workItems = workItemDao.observeAll().first()
        val workItemIds = workItems.map { it.id }

        val eventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk ->
                eventDao.getByWorkItemIds(chunk)
            }
        }

        val eventsByWorkItem = eventEntities.map { it.toDomain() }
            .groupBy { it.workItemId }

        val usersById = userDao.getAll().associateBy { it.id }

        return workItems.map { workItem ->
            val events = eventsByWorkItem[workItem.id].orEmpty()
            val state = reduce(events)
            val lastChangedAt = state.lastEvent?.timestamp
                ?: workItem.createdAt
                ?: 0L
            val assigneeName = state.currentAssigneeId?.let { userId ->
                usersById[userId]?.userNameCompat(fallback = userId)
            }

            SupervisorWorkItem(
                workItemId = workItem.id,
                code = workItem.code ?: "",
                description = workItem.description ?: "",
                zoneId = workItem.zoneId,
                status = state.status,
                lastChangedAt = lastChangedAt,
                assigneeId = state.currentAssigneeId,
                assigneeName = assigneeName,
            )
        }
    }
}
