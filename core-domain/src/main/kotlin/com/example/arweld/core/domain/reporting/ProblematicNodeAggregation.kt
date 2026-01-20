package com.example.arweld.core.domain.reporting

/**
 * Aggregates QC failures by structural node for deterministic reporting.
 *
 * Rules:
 * - Each work item contributes at most one failure count based on its latest QC result.
 * - Missing node IDs are bucketed under [UNKNOWN_NODE].
 * - Results are sorted by failure count DESC, then node ID ASC.
 */
object ProblematicNodeAggregation {
    const val UNKNOWN_NODE = "UNKNOWN_NODE"

    fun aggregateFromReport(report: ReportV1): List<NodeFailStats> {
        if (report.qcResults.isEmpty()) return emptyList()

        val nodeByWorkItem = report.workItems.associate { workItem ->
            workItem.id to (workItem.nodeId ?: UNKNOWN_NODE)
        }

        val failedWorkItemIds = report.qcResults
            .groupBy { it.workItemId }
            .mapNotNull { (_, results) ->
                results.maxWithOrNull(compareBy<QcResult> { it.timestamp }.thenBy { it.eventId })
            }
            .filter { it.outcome == QcOutcome.FAILED_REWORK }
            .map { it.workItemId }

        if (failedWorkItemIds.isEmpty()) return emptyList()

        return failedWorkItemIds
            .groupBy { workItemId -> nodeByWorkItem[workItemId] ?: UNKNOWN_NODE }
            .entries
            .map { (nodeId, workItemIds) ->
                NodeFailStats(
                    nodeId = nodeId,
                    failCount = workItemIds.size,
                    workItemIds = workItemIds.sorted(),
                )
            }
            .sortedWith(compareByDescending<NodeFailStats> { it.failCount }.thenBy { it.nodeId })
    }
}
