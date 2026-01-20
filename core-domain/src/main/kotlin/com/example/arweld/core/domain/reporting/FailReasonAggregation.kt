package com.example.arweld.core.domain.reporting

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object FailReasonAggregation {
    private const val UNKNOWN_REASON = "UNKNOWN"
    private val json = Json { ignoreUnknownKeys = true }

    fun aggregateFromEvents(events: List<Event>): List<FailReasonCount> {
        val failedWorkItemIds = events
            .filter { it.type == EventType.QC_PASSED || it.type == EventType.QC_FAILED_REWORK }
            .groupBy { it.workItemId }
            .mapNotNull { (_, qcEvents) ->
                qcEvents.maxWithOrNull(compareBy<Event> { it.timestamp }.thenBy { it.id })
            }
            .filter { it.type == EventType.QC_FAILED_REWORK }
            .map { it.workItemId }
            .toSet()

        if (failedWorkItemIds.isEmpty()) return emptyList()

        val reasons = events
            .filter { it.type == EventType.QC_FAILED_REWORK && it.workItemId in failedWorkItemIds }
            .flatMap { event ->
                val parsed = parseFailReasons(event.payloadJson)
                if (parsed.isEmpty()) {
                    listOf(UNKNOWN_REASON)
                } else {
                    parsed.map { reason ->
                        reason.trim().ifBlank { UNKNOWN_REASON }
                    }
                }
            }

        if (reasons.isEmpty()) return emptyList()

        return reasons
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { (reason, count) -> FailReasonCount(reason = reason, count = count) }
    }

    private fun parseFailReasons(payloadJson: String?): List<String> {
        if (payloadJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val element = json.decodeFromString<JsonElement>(payloadJson)
            element.jsonObject["reasons"]
                ?.jsonArray
                ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                .orEmpty()
        }.getOrDefault(emptyList())
    }
}
