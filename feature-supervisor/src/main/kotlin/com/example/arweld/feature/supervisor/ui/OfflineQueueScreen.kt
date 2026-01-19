package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.core.domain.sync.SyncQueueItem
import com.example.arweld.feature.supervisor.viewmodel.OfflineQueueUiState

@Composable
fun OfflineQueueScreen(
    state: OfflineQueueUiState,
    onRefresh: () -> Unit,
    onProcess: () -> Unit,
    onEnqueueSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Offline Queue", style = MaterialTheme.typography.headlineSmall)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                Text("Refresh")
            }
            Button(onClick = onProcess, modifier = Modifier.weight(1f)) {
                Text("Process Pending")
            }
        }

        Button(onClick = onEnqueueSample, modifier = Modifier.fillMaxWidth()) {
            Text("Add Sample Payload")
        }

        if (state.errorMessage != null) {
            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Text(text = "Pending", style = MaterialTheme.typography.titleMedium)
        QueueList(items = state.pendingItems, emptyLabel = "No pending items")

        Text(text = "Errors", style = MaterialTheme.typography.titleMedium)
        QueueList(items = state.errorItems, emptyLabel = "No error items")
    }
}

@Composable
private fun QueueList(items: List<SyncQueueItem>, emptyLabel: String) {
    if (items.isEmpty()) {
        Text(text = emptyLabel, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "ID: ${item.id}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Type: ${item.type} • Event: ${item.eventType}")
                    Text(text = "Status: ${item.status}")
                    Text(text = "WorkItem: ${item.workItemId ?: "N/A"}")
                    Text(text = "Created: ${item.createdAt}")
                    Text(text = item.payloadJson, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
