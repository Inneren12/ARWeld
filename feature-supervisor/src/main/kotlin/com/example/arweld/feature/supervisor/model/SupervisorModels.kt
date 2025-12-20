package com.example.arweld.feature.supervisor.model

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.QcStatus
import kotlin.time.Duration

/**
 * KPI metrics derived from the event log for the supervisor dashboard.
 */
data class ShopKpis(
    val totalWorkItems: Int,
    val inProgress: Int,
    val readyForQc: Int,
    val qcInProgress: Int,
    val approved: Int,
    val rework: Int,
    val avgQcWaitTimeMs: Long,
    val qcPassRate: Float
)

/**
 * Represents a work item in the QC bottleneck list.
 * Shows items waiting for QC inspection, sorted by wait time.
 */
data class BottleneckItem(
    val workItemId: String,
    val code: String,
    val description: String,
    val status: WorkStatus,
    val assigneeId: String?,
    val assigneeName: String?,
    val waitTimeMs: Long,
    val readyForQcSince: Long
)

/**
 * Represents current activity of a user in the shop floor.
 * Used for "Who does what" dashboard section.
 */
data class UserActivity(
    val userId: String,
    val userName: String,
    val role: Role,
    val currentWorkItemId: String?,
    val currentWorkItemCode: String?,
    val lastActionType: String,
    val lastActionTimeMs: Long
)

/**
 * Detailed information about a work item for the supervisor detail view.
 */
data class WorkItemDetail(
    val workItem: WorkItem,
    val status: WorkStatus,
    val qcStatus: QcStatus?,
    val currentAssigneeId: String?,
    val currentAssigneeName: String?,
    val createdAt: Long,
    val lastUpdated: Long
)

/**
 * Timeline entry for the work item detail view.
 */
data class TimelineEntry(
    val eventId: String,
    val timestamp: Long,
    val actorId: String,
    val actorName: String,
    val actorRole: Role,
    val eventType: String,
    val eventDescription: String,
    val payloadSummary: String?
)
