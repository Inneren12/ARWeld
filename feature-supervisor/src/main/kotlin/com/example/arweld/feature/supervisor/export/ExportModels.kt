package com.example.arweld.feature.supervisor.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportReport(
    @SerialName("export_metadata")
    val metadata: ExportMetadata,
    @SerialName("work_items")
    val workItems: List<ExportWorkItem>,
    val summary: ExportSummary,
    val reporting: ExportReporting,
)

@Serializable
data class ExportMetadata(
    @SerialName("export_id")
    val exportId: String,
    @SerialName("exported_at")
    val exportedAt: String,
    @SerialName("exported_by")
    val exportedBy: String,
    @SerialName("period_start")
    val periodStart: String,
    @SerialName("period_end")
    val periodEnd: String,
)

@Serializable
data class ExportWorkItem(
    val id: String,
    val code: String,
    val description: String,
    val status: String,
    val zone: String? = null,
    @SerialName("node_id")
    val nodeId: String? = null,
    val events: List<ExportEvent>,
    val evidence: List<ExportEvidence>,
)

@Serializable
data class ExportEvent(
    val id: String,
    val type: String,
    @SerialName("actor_id")
    val actorId: String,
    @SerialName("actor_name")
    val actorName: String?,
    @SerialName("actor_role")
    val actorRole: String,
    val timestamp: String,
    @SerialName("payload_json")
    val payloadJson: String? = null,
)

@Serializable
data class ExportEvidence(
    val id: String,
    @SerialName("event_id")
    val eventId: String,
    val kind: String,
    @SerialName("file_path")
    val filePath: String?,
    @SerialName("file_hash")
    val fileHash: String,
    @SerialName("captured_at")
    val capturedAt: String,
    @SerialName("size_bytes")
    val sizeBytes: Long,
    val uri: String,
)

@Serializable
data class ExportSummary(
    @SerialName("total_work_items")
    val totalWorkItems: Int,
    val passed: Int,
    val failed: Int,
    @SerialName("qc_pass_rate")
    val qcPassRate: Double,
)

@Serializable
data class ExportReporting(
    @SerialName("shift_counts")
    val shiftCounts: List<ShiftReportEntry>,
    @SerialName("top_fail_reasons")
    val topFailReasons: List<FailReasonEntry>,
    @SerialName("problematic_nodes")
    val problematicNodes: List<NodeIssueEntry>,
)

@Serializable
data class ShiftReportEntry(
    val label: String,
    val total: Int,
    val passed: Int,
    val failed: Int,
)

@Serializable
data class FailReasonEntry(
    val reason: String,
    val count: Int,
)

@Serializable
data class NodeIssueEntry(
    @SerialName("node_id")
    val nodeId: String,
    val failures: Int,
    @SerialName("total_items")
    val totalItems: Int,
)
