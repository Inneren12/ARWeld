package com.example.arweld.feature.supervisor.model

data class ShiftReportSummary(
    val label: String,
    val total: Int,
    val passed: Int,
    val failed: Int,
)

data class FailReasonSummary(
    val reason: String,
    val count: Int,
)

data class NodeIssueSummary(
    val nodeId: String,
    val failures: Int,
    val totalItems: Int,
)
