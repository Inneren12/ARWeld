package com.example.arweld.core.domain.reporting

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.WorkItem
import kotlinx.serialization.Serializable

@Serializable
data class ReportV1(
    val reportVersion: Int = 1,
    val generatedAt: Long,
    val workItems: List<WorkItem> = emptyList(),
    val events: List<Event> = emptyList(),
    val qcResults: List<QcResult> = emptyList(),
    val topFailReasons: List<FailReasonCount> = emptyList(),
)

@Serializable
enum class QcOutcome {
    PASSED,
    FAILED_REWORK,
}

@Serializable
data class QcResult(
    val eventId: String,
    val workItemId: String,
    val outcome: QcOutcome,
    val timestamp: Long,
    val inspectorId: String,
    val inspectorRole: Role,
    val deviceId: String,
    val payloadJson: String? = null,
)
