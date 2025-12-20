package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.BottleneckItem
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Finds work items waiting for QC longer than a threshold.
 * Items are derived from events and sorted by wait time (longest first).
 */
class GetQcBottleneckUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val userDao: UserDao
) {
    /**
     * @param thresholdMs Minimum wait time in milliseconds to be considered a bottleneck.
     *                     Items waiting less than this are filtered out.
     */
    suspend operator fun invoke(thresholdMs: Long = 0L): List<BottleneckItem> {
        val now = System.currentTimeMillis()
        val allWorkItems = workItemDao.observeAll().first()

        // Batch load all events for all work items (eliminates N+1 query)
        val workItemIds = allWorkItems.map { it.id }
        val allEventsEntities = eventDao.getByWorkItemIds(workItemIds)
        val allEvents = allEventsEntities.map { it.toDomain() }

        // Group events by workItemId
        val eventsByWorkItem = allEvents.groupBy { it.workItemId }

        // Derive state for each work item and filter for READY_FOR_QC
        val bottlenecks = allWorkItems.mapNotNull { workItemEntity ->
            val events = eventsByWorkItem[workItemEntity.id] ?: emptyList()
            // Events are already sorted by timestamp ASC from the DAO query
            val state = reduce(events)

            if (state.status == WorkStatus.READY_FOR_QC && state.readyForQcSince != null) {
                val waitTimeMs = now - state.readyForQcSince!!

                // Filter by threshold
                if (waitTimeMs >= thresholdMs) {
                    // Get assignee name if available
                    val assigneeName = state.currentAssigneeId?.let { userId ->
                        userDao.getUserById(userId)?.displayName
                    }

                    BottleneckItem(
                        workItemId = workItemEntity.id,
                        code = workItemEntity.code ?: "",
                        description = workItemEntity.description ?: "",
                        status = state.status,
                        assigneeId = state.currentAssigneeId,
                        assigneeName = assigneeName,
                        waitTimeMs = waitTimeMs,
                        readyForQcSince = state.readyForQcSince!!
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }

        // Sort by wait time descending (longest wait first)
        return bottlenecks.sortedByDescending { it.waitTimeMs }
    }
}
