package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.ShopKpis
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Computes shop-wide KPIs from the event log.
 * All state is derived - no status stored directly.
 */
class CalculateKpisUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao
) {
    suspend operator fun invoke(): ShopKpis {
        // Get all work items
        val allWorkItems = workItemDao.observeAll().first()

        // Batch load all events for all work items (eliminates N+1 query)
        val workItemIds = allWorkItems.map { it.id }

        // Guard against empty list and chunk to prevent SQLite bind limit (999 max, use 900 for safety)
        val allEventsEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk ->
                eventDao.getByWorkItemIds(chunk)
            }
        }
        val allEvents = allEventsEntities.map { it.toDomain() }

        // Group events by workItemId
        val eventsByWorkItem = allEvents.groupBy { it.workItemId }

        // Derive state for each work item from events
        val states = allWorkItems.map { workItemEntity ->
            val events = eventsByWorkItem[workItemEntity.id] ?: emptyList()
            // Events are already sorted by timestamp ASC from the DAO query
            reduce(events)
        }

        // Count by status
        val statusCounts = states.groupingBy { it.status }.eachCount()
        val inProgress = statusCounts[WorkStatus.IN_PROGRESS] ?: 0
        val readyForQc = statusCounts[WorkStatus.READY_FOR_QC] ?: 0
        val qcInProgress = statusCounts[WorkStatus.QC_IN_PROGRESS] ?: 0
        val approved = statusCounts[WorkStatus.APPROVED] ?: 0
        val rework = statusCounts[WorkStatus.REWORK_REQUIRED] ?: 0

        // Calculate average QC wait time
        val now = System.currentTimeMillis()
        val qcWaitTimes = states
            .filter { it.status == WorkStatus.READY_FOR_QC && it.readyForQcSince != null }
            .map { now - it.readyForQcSince!! }
        val avgQcWaitTimeMs = if (qcWaitTimes.isNotEmpty()) {
            qcWaitTimes.average().toLong()
        } else {
            0L
        }

        // Calculate QC pass rate
        val qcCompleted = approved + rework
        val qcPassRate = if (qcCompleted > 0) {
            approved.toFloat() / qcCompleted.toFloat()
        } else {
            0f
        }

        return ShopKpis(
            totalWorkItems = allWorkItems.size,
            inProgress = inProgress,
            readyForQc = readyForQc,
            qcInProgress = qcInProgress,
            approved = approved,
            rework = rework,
            avgQcWaitTimeMs = avgQcWaitTimeMs,
            qcPassRate = qcPassRate
        )
    }
}
