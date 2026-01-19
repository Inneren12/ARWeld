package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.feature.supervisor.model.TimelineEntry
import com.example.arweld.feature.supervisor.model.WorkItemDetail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkItemDetailScreen(
    detail: WorkItemDetail?,
    timeline: List<TimelineEntry>,
    evidence: List<Evidence>,
    isLoading: Boolean = false,
    error: String? = null,
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onEvidenceClick: (Evidence) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for selected evidence to show in viewer
    var selectedEvidence by remember { mutableStateOf<Evidence?>(null) }

    // Show evidence viewer dialog when evidence is selected
    selectedEvidence?.let { evidence ->
        EvidenceViewerDialog(
            evidence = evidence,
            onDismiss = { selectedEvidence = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Item Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show error state
        if (error != null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error Loading Work Item",
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

        // Show loading state
        if (isLoading || detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading work item...",
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
            // Summary Section
            item {
                SummarySection(detail = detail)
            }

            // Timeline Section
            item {
                Text(
                    text = "Timeline",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                TimelineList(timeline = timeline)
            }

            // Evidence Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Evidence",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (evidence.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No evidence captured",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                item {
                    EvidenceGrid(
                        evidence = evidence,
                        onEvidenceClick = {
                            selectedEvidence = it
                            onEvidenceClick(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummarySection(detail: WorkItemDetail) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = detail.workItem.code,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (detail.workItem.description.isNotEmpty()) {
                Text(
                    text = detail.workItem.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Divider()

            DetailRow(label = "Type", value = detail.workItem.type.name)
            DetailRow(label = "Status", value = detail.status.name)

            detail.qcStatus?.let { qcStatus ->
                DetailRow(label = "QC Status", value = qcStatus.name)
            }

            detail.workItem.zone?.let { zone ->
                DetailRow(label = "Zone", value = zone)
            }

            detail.currentAssigneeName?.let { assignee ->
                DetailRow(label = "Assigned to", value = assignee)
            }

            DetailRow(
                label = "Created",
                value = formatTimestamp(detail.createdAt)
            )

            DetailRow(
                label = "Last Updated",
                value = formatTimestamp(detail.lastUpdated)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EvidenceGrid(
    evidence: List<Evidence>,
    onEvidenceClick: (Evidence) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(400.dp) // Limit height for evidence grid
    ) {
        items(evidence) { item ->
            EvidenceCard(
                evidence = item,
                onClick = { onEvidenceClick(item) }
            )
        }
    }
}

@Composable
private fun EvidenceCard(
    evidence: Evidence,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Evidence kind badge
            Surface(
                color = when (evidence.kind) {
                    EvidenceKind.PHOTO -> MaterialTheme.colorScheme.primary
                    EvidenceKind.AR_SCREENSHOT -> MaterialTheme.colorScheme.secondary
                    EvidenceKind.VIDEO -> MaterialTheme.colorScheme.tertiary
                    EvidenceKind.MEASUREMENT -> MaterialTheme.colorScheme.error
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (evidence.kind) {
                        EvidenceKind.PHOTO -> "Photo"
                        EvidenceKind.AR_SCREENSHOT -> "AR"
                        EvidenceKind.VIDEO -> "Video"
                        EvidenceKind.MEASUREMENT -> "Measurement"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Timestamp
            Text(
                text = formatTime(evidence.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Hash (truncated)
            Text(
                text = "Hash: ${evidence.sha256.take(8)}...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
