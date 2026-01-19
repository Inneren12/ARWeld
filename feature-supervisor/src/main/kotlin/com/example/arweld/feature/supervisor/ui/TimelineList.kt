package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.model.TimelineEntry
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineList(
    timeline: List<TimelineEntry>,
    modifier: Modifier = Modifier,
    emptyLabel: String = "No timeline events"
) {
    val orderedTimeline = remember(timeline) {
        timeline.sortedWith(compareBy({ it.timestamp }, { it.eventId }))
    }

    if (orderedTimeline.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = emptyLabel,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        orderedTimeline.forEach { entry ->
            TimelineEventRow(entry = entry)
        }
    }
}

@Composable
private fun TimelineEventRow(
    entry: TimelineEntry,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val payloadSummary = remember(entry.payloadSummary) {
        summarizePayload(entry.payloadSummary)
    }
    var isExpanded by remember(entry.eventId) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimelineTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = entry.eventType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = entry.eventDescription,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "by ${entry.actorName} (${entry.actorRole.name})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            payloadSummary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(entry.eventId))
                    }
                ) {
                    Text("Copy event id")
                }

                if (!entry.payloadSummary.isNullOrBlank()) {
                    TextButton(onClick = { isExpanded = !isExpanded }) {
                        Text(if (isExpanded) "Hide payload" else "Expand payload")
                    }
                }
            }

            if (isExpanded && !entry.payloadSummary.isNullOrBlank()) {
                Text(
                    text = formatExpandedPayload(entry.payloadSummary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimelineTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun summarizePayload(payloadJson: String?): String? {
    if (payloadJson.isNullOrBlank() || payloadJson == "null") {
        return null
    }

    return runCatching {
        val value = JSONTokener(payloadJson).nextValue()
        when (value) {
            is JSONObject -> summarizeJsonObject(value)
            is JSONArray -> "Payload: ${value.length()} items"
            else -> "Payload: ${value.toString().take(140)}"
        }
    }.getOrElse {
        "Payload: (unparseable)"
    }
}

private fun summarizeJsonObject(jsonObject: JSONObject): String {
    val keys = jsonObject.keys().asSequence().toList().sorted()
    if (keys.isEmpty()) {
        return "Payload: {}"
    }

    val summaryPairs = keys.take(3).map { key ->
        val rawValue = jsonObject.opt(key)
        val displayValue = when (rawValue) {
            is JSONObject -> "{...}"
            is JSONArray -> "[${rawValue.length()}]"
            else -> rawValue?.toString()?.take(40) ?: "null"
        }
        "$key=$displayValue"
    }

    val suffix = if (keys.size > 3) "…" else ""
    return "Payload: ${summaryPairs.joinToString(", ")}$suffix"
}

private fun formatExpandedPayload(payloadJson: String): String {
    return runCatching {
        val value = JSONTokener(payloadJson).nextValue()
        when (value) {
            is JSONObject -> value.toString(2)
            is JSONArray -> value.toString(2)
            else -> value.toString()
        }
    }.getOrElse {
        "Raw payload (unparsed): $payloadJson"
    }
}
