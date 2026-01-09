package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import com.example.arweld.feature.supervisor.viewmodel.ReportsUiState

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Reports", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onRefresh) {
            Text("Refresh")
        }

        if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        Section(title = "Shift Counts") {
            ShiftList(items = state.shiftCounts)
        }

        Section(title = "Top Fail Reasons") {
            FailReasonList(items = state.topFailReasons)
        }

        Section(title = "Problematic Nodes") {
            NodeList(items = state.problematicNodes)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ShiftList(items: List<ShiftReportSummary>) {
    if (items.isEmpty()) {
        Text(text = "No shift data yet.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { entry ->
            Text(text = "${entry.label}: ${entry.total} total (${entry.passed} passed, ${entry.failed} failed)")
        }
    }
}

@Composable
private fun FailReasonList(items: List<FailReasonSummary>) {
    if (items.isEmpty()) {
        Text(text = "No failure reasons captured.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { entry ->
            Text(text = "${entry.reason}: ${entry.count}")
        }
    }
}

@Composable
private fun NodeList(items: List<NodeIssueSummary>) {
    if (items.isEmpty()) {
        Text(text = "No problematic nodes yet.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { entry ->
            Text(text = "${entry.nodeId}: ${entry.failures} failures of ${entry.totalItems} items")
        }
    }
}
