package com.example.arweld.feature.work.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.work.viewmodel.WorkItemSummaryUiState

@Composable
fun WorkItemSummaryScreen(
    state: WorkItemSummaryUiState,
    onClaimWork: () -> Unit,
    onStartWork: () -> Unit,
    onMarkReadyForQc: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Work item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Loading work item...")
                }
            }

            state.error != null -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Tap to retry",
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable { onRefresh() },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            else -> {
                WorkItemSummaryContent(
                    workItemId = state.workItemId.orEmpty(),
                    workItemState = state.workItemState,
                    actionInProgress = state.actionInProgress,
                    onClaimWork = onClaimWork,
                    onStartWork = onStartWork,
                    onMarkReadyForQc = onMarkReadyForQc,
                    onRefresh = onRefresh,
                    modifier = modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun WorkItemSummaryContent(
    workItemId: String,
    workItemState: WorkItemState?,
    actionInProgress: Boolean,
    onClaimWork: () -> Unit,
    onStartWork: () -> Unit,
    onMarkReadyForQc: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "ID: $workItemId", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Status: ${workItemState?.status ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge
        )
        workItemState?.qcStatus?.let { qc ->
            Text(text = "QC: $qc", style = MaterialTheme.typography.bodyMedium)
        }

        val status = workItemState?.status
        if (status == WorkStatus.NEW || status == WorkStatus.REWORK_REQUIRED) {
            ActionButton(text = "Claim work", enabled = !actionInProgress, onClick = onClaimWork)
        }

        if (status == WorkStatus.IN_PROGRESS) {
            ActionButton(text = "Start work", enabled = !actionInProgress, onClick = onStartWork)
            ActionButton(
                text = "Mark ready for QC",
                enabled = !actionInProgress,
                onClick = onMarkReadyForQc
            )
        }

        if (status == WorkStatus.READY_FOR_QC) {
            Text(
                text = "Waiting for quality control",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))
        ActionButton(text = "Refresh", enabled = !actionInProgress, onClick = onRefresh)
    }
}

@Composable
private fun ActionButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}
