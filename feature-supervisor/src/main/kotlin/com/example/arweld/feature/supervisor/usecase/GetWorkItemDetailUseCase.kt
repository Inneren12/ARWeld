package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.TimelineEntry
import com.example.arweld.feature.supervisor.model.WorkItemDetail
import com.example.arweld.core.domain.model.WorkItem
import javax.inject.Inject

/**
 * Retrieves detailed information about a work item including its full timeline.
 * All state is derived from the event log.
 */
class GetWorkItemDetailUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val userDao: UserDao
) {
    suspend operator fun invoke(workItemId: String): WorkItemDetail? {
        // Get work item entity
        val workItemEntity = workItemDao.getById(workItemId) ?: return null

        // Get all events for this work item
        val eventEntities = eventDao.getByWorkItemId(workItemId)
        val events = eventEntities.map { it.toDomain() }

        // Derive current state
        val state = reduce(events)

        // Get assignee name if available
        val assigneeName = state.currentAssigneeId?.let { userId ->
            userDao.getById(userId)?.userNameCompat(fallback = userId)
        }

        // Find last event timestamp
        val lastUpdated = events.maxOfOrNull { it.timestamp } ?: workItemEntity.createdAt ?: 0L

        // Convert to domain WorkItem
        val workItem = WorkItem(
            id = workItemEntity.id,
            code = workItemEntity.code ?: "",
            type = WorkItemType.valueOf(workItemEntity.type),
            description = workItemEntity.description ?: "",
            zone = workItemEntity.zoneId,
            nodeId = workItemEntity.nodeId,
            createdAt = workItemEntity.createdAt ?: 0L
        )

        return WorkItemDetail(
            workItem = workItem,
            status = state.status,
            qcStatus = state.qcStatus,
            currentAssigneeId = state.currentAssigneeId,
            currentAssigneeName = assigneeName,
            createdAt = workItemEntity.createdAt ?: 0L,
            lastUpdated = lastUpdated
        )
    }

    /**
     * Get timeline entries for a work item.
     */
    suspend fun getTimeline(workItemId: String): List<TimelineEntry> {
        val eventEntities = eventDao.getByWorkItemId(workItemId)
        val events = eventEntities.map { it.toDomain() }

        // For each event, get actor name and create timeline entry
        return events.map { event ->
            val actorName = userDao.getById(event.actorId)?.userNameCompat(fallback = "Unknown") ?: "Unknown"

            TimelineEntry(
                eventId = event.id,
                timestamp = event.timestamp,
                actorId = event.actorId,
                actorName = actorName,
                actorRole = event.actorRole,
                eventType = event.type.name,
                eventDescription = formatEventDescription(event.type, actorName, event.actorRole.name),
                payloadSummary = event.payloadJson
            )
        }.sortedWith(compareBy({ it.timestamp }, { it.eventId }))  // Stable sort: timestamp + id
    }

    private fun formatEventDescription(type: EventType, actorName: String, role: String): String {
        return when (type) {
            EventType.WORK_CLAIMED -> "$actorName claimed work"
            EventType.WORK_STARTED -> "$actorName started work"
            EventType.WORK_READY_FOR_QC -> "$actorName marked ready for QC"
            EventType.QC_STARTED -> "$actorName started QC inspection"
            EventType.QC_PASSED -> "$actorName passed QC"
            EventType.QC_FAILED_REWORK -> "$actorName failed QC - rework required"
            EventType.REWORK_STARTED -> "$actorName started rework"
            EventType.EVIDENCE_CAPTURED -> "$actorName captured evidence"
            EventType.AR_ALIGNMENT_SET -> "$actorName set AR alignment"
            EventType.ISSUE_CREATED -> "$actorName created issue"
        }
    }
}
