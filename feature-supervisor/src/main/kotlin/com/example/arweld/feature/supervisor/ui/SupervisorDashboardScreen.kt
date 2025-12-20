package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.model.BottleneckItem
import com.example.arweld.feature.supervisor.model.ShopKpis
import com.example.arweld.feature.supervisor.model.UserActivity
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboardScreen(
    kpis: ShopKpis,
    bottleneckItems: List<BottleneckItem>,
    userActivities: List<UserActivity>,
    bottleneckThresholdMs: Long,
    onBottleneckThresholdChange: (Long) -> Unit,
    onWorkItemClick: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supervisor Dashboard") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show error banner if present
        if (error != null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            return@Scaffold
        }

        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LinearProgressIndicator()
                    Text(
                        text = "Loading dashboard...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Section
            item {
                Text(
                    text = "Shop KPIs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                KpiCards(kpis = kpis)
            }

            // QC Bottleneck Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QC Bottleneck",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Threshold selector
                    BottleneckThresholdSelector(
                        currentThresholdMs = bottleneckThresholdMs,
                        onThresholdChange = onBottleneckThresholdChange
                    )
                }
            }

            if (bottleneckItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No bottlenecks detected",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(bottleneckItems) { item ->
                    BottleneckItemCard(
                        item = item,
                        onClick = { onWorkItemClick(item.workItemId) }
                    )
                }
            }

            // Who Does What Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Who Does What",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (userActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No recent activity",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(userActivities) { activity ->
                    UserActivityCard(
                        activity = activity,
                        onClick = {
                            activity.currentWorkItemId?.let { workItemId ->
                                onWorkItemClick(workItemId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCards(kpis: ShopKpis) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Total",
                value = kpis.totalWorkItems.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "In Progress",
                value = kpis.inProgress.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Ready for QC",
                value = kpis.readyForQc.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "QC In Progress",
                value = kpis.qcInProgress.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Third row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Approved",
                value = kpis.approved.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Rework",
                value = kpis.rework.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Fourth row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Avg QC Wait",
                value = formatDuration(kpis.avgQcWaitTimeMs),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "QC Pass Rate",
                value = "${(kpis.qcPassRate * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BottleneckThresholdSelector(
    currentThresholdMs: Long,
    onThresholdChange: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val thresholds = listOf(
        0L to "All",
        30 * 60 * 1000L to "30 min",
        60 * 60 * 1000L to "1 hour",
        2 * 60 * 60 * 1000L to "2 hours",
        4 * 60 * 60 * 1000L to "4 hours"
    )

    val currentLabel = thresholds.find { it.first == currentThresholdMs }?.second ?: "All"

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Threshold: $currentLabel")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            thresholds.forEach { (thresholdMs, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onThresholdChange(thresholdMs)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BottleneckItemCard(
    item: BottleneckItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Assignee: ${item.assigneeName ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Wait: ${formatDuration(item.waitTimeMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UserActivityCard(
    activity: UserActivity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (activity.currentWorkItemId != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(${activity.role.name})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activity.currentWorkItemCode != null) {
                Text(
                    text = "Working on: ${activity.currentWorkItemCode}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Last action: ${activity.lastActionType} (${formatDuration(System.currentTimeMillis() - activity.lastActionTimeMs)} ago)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
