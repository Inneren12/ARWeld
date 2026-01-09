package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import com.example.arweld.feature.supervisor.viewmodel.ExportUiState

@Composable
fun ExportScreen(
    state: ExportUiState,
    onSelectPeriod: (ExportPeriod) -> Unit,
    onToggleCsv: (Boolean) -> Unit,
    onToggleZip: (Boolean) -> Unit,
    onToggleManifest: (Boolean) -> Unit,
    onExport: () -> Unit,
    onExportDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Export Center", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Select Period", style = MaterialTheme.typography.titleMedium)
                state.availablePeriods.forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.selectedPeriod == period,
                                onClick = { onSelectPeriod(period) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = period.label, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Options", style = MaterialTheme.typography.titleMedium)
                ExportOptionRow(label = "Include CSV", checked = state.options.includeCsv, onToggle = onToggleCsv)
                ExportOptionRow(label = "Include ZIP", checked = state.options.includeZip, onToggle = onToggleZip)
                ExportOptionRow(label = "Include Manifest", checked = state.options.includeManifest, onToggle = onToggleManifest)
            }
        }

        if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        if (state.lastExportPath != null) {
            Text(text = "Exported to: ${state.lastExportPath}", style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = onExport,
            enabled = !state.isExporting && state.selectedPeriod != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isExporting) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = if (state.isExporting) "Exporting..." else "Export")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Diagnostics", style = MaterialTheme.typography.titleMedium)
                if (state.diagnosticsError != null) {
                    Text(text = state.diagnosticsError, color = MaterialTheme.colorScheme.error)
                }
                if (state.lastDiagnosticsPath != null) {
                    Text(
                        text = "Diagnostics zip: ${state.lastDiagnosticsPath}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = onExportDiagnostics,
                    enabled = !state.isDiagnosticsExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isDiagnosticsExporting) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = if (state.isDiagnosticsExporting) "Exporting diagnostics..." else "Export diagnostics zip")
                }
            }
        }
    }
}

@Composable
private fun ExportOptionRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
