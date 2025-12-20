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

        // Derive state for each work item from events
        val states = allWorkItems.map { workItemEntity ->
            val events = eventDao.getByWorkItemId(workItemEntity.id).map { it.toDomain() }
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
