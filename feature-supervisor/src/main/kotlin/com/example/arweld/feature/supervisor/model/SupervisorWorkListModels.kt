package com.example.arweld.feature.supervisor.model

import com.example.arweld.core.domain.state.WorkStatus

/**
 * Summary entry for supervisor work list.
 */
data class SupervisorWorkItem(
    val workItemId: String,
    val code: String,
    val description: String,
    val zoneId: String?,
    val status: WorkStatus,
    val lastChangedAt: Long,
    val assigneeId: String?,
    val assigneeName: String?,
)

data class WorkListAssignee(
    val id: String,
    val name: String,
)
