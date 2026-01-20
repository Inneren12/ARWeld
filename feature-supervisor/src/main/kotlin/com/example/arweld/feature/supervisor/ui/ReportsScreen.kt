package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import com.example.arweld.feature.supervisor.viewmodel.ExportPeriodType
import com.example.arweld.feature.supervisor.viewmodel.ReportsUiState
import com.example.arweld.feature.supervisor.viewmodel.ShiftSelection
import java.time.LocalDate

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onSelectPeriodType: (ExportPeriodType) -> Unit,
    onSelectShift: (ShiftSelection) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onSelectZone: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Reports", style = MaterialTheme.typography.headlineSmall)

        PeriodSelectionCard(
            periodType = state.periodType,
            selectedShift = state.selectedShift,
            selectedDate = state.selectedDate,
            currentShiftLabel = state.currentShiftLabel,
            previousShiftLabel = state.previousShiftLabel,
            selectedPeriodLabel = state.selectedPeriod?.label,
            onSelectPeriodType = onSelectPeriodType,
            onSelectShift = onSelectShift,
            onSelectDate = onSelectDate,
        )

        ZoneFilterCard(
            selectedZone = state.selectedZoneId,
            availableZones = state.availableZones,
            onSelectZone = onSelectZone,
        )

        if (state.isLoading) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Text(text = "Loading reports…")
                }
            }
        }

        if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        if (!state.isLoading && state.shiftCounts.isEmpty() && state.topFailReasons.isEmpty() && state.problematicNodes.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No report data for the selected filters.",
                    modifier = Modifier.padding(16.dp),
                )
            }
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

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ZoneFilterCard(
    selectedZone: String?,
    availableZones: List<String>,
    onSelectZone: (String?) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Zone filter", style = MaterialTheme.typography.titleMedium)
            FilterMenu(
                label = "Zone",
                current = selectedZone ?: "All zones",
                options = listOf("All zones") + availableZones,
                onSelected = { selected ->
                    onSelectZone(selected.takeIf { it != "All zones" })
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Active zone: ${selectedZone ?: "All zones"}",
                style = MaterialTheme.typography.bodySmall,
            )
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { entry ->
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { entry ->
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { entry ->
            Text(text = "${entry.nodeId}: ${entry.failures} failures of ${entry.totalItems} items")
        }
    }
}
