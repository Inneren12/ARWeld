package com.example.arweld.feature.work.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.feature.work.viewmodel.AssemblerQueueUiState

@Composable
fun AssemblerQueueScreen(
    state: AssemblerQueueUiState,
    onWorkItemClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Assembler queue") },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Loading queue...")
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
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
                        modifier = Modifier.clickable { onRefresh() },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { SectionHeader(title = "In progress") }
                    items(state.inProgress) { workItem ->
                        WorkItemCard(state = workItem, onWorkItemClick = onWorkItemClick)
                    }
                    if (state.inProgress.isEmpty()) {
                        item { EmptySection(text = "No items in progress") }
                    }

                    item { SectionHeader(title = "Ready for QC") }
                    items(state.readyForQc) { workItem ->
                        WorkItemCard(state = workItem, onWorkItemClick = onWorkItemClick)
                    }
                    if (state.readyForQc.isEmpty()) {
                        item { EmptySection(text = "Nothing ready for QC") }
                    }

                    item { SectionHeader(title = "Rework required") }
                    items(state.reworkRequired) { workItem ->
                        WorkItemCard(state = workItem, onWorkItemClick = onWorkItemClick)
                    }
                    if (state.reworkRequired.isEmpty()) {
                        item { EmptySection(text = "No items in rework") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun WorkItemCard(state: WorkItemState, onWorkItemClick: (String) -> Unit) {
    val workItemId = state.lastEvent?.workItemId ?: "Unknown"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWorkItemClick(workItemId) }
            .padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: $workItemId", style = MaterialTheme.typography.titleMedium)
            Text(text = "Status: ${state.status}", style = MaterialTheme.typography.bodyMedium)
            state.qcStatus?.let { qc ->
                Text(text = "QC: $qc", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EmptySection(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}
