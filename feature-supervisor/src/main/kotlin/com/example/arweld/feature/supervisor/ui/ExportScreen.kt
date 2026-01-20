package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.viewmodel.BannerType
import com.example.arweld.feature.supervisor.viewmodel.ExportPeriodType
import com.example.arweld.feature.supervisor.viewmodel.ExportUiState
import com.example.arweld.feature.supervisor.viewmodel.ShiftSelection
import java.time.LocalDate

@Composable
fun ExportScreen(
    state: ExportUiState,
    onSelectPeriodType: (ExportPeriodType) -> Unit,
    onSelectShift: (ShiftSelection) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToggleEvidenceZip: (Boolean) -> Unit,
    onExportDiagnostics: () -> Unit,
    onExportEvidenceZip: () -> Unit,
    onExportReportJson: () -> Unit,
    onExportReportCsv: () -> Unit,
    onExportManifest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Export Center", style = MaterialTheme.typography.headlineSmall)

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

        PreviewCard(state = state)

        ExportActionsCard(
            state = state,
            onToggleEvidenceZip = onToggleEvidenceZip,
            onExportEvidenceZip = onExportEvidenceZip,
            onExportReportJson = onExportReportJson,
            onExportReportCsv = onExportReportCsv,
            onExportManifest = onExportManifest,
        )

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
private fun PreviewCard(state: ExportUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Preview counts", style = MaterialTheme.typography.titleMedium)
            if (state.isPreviewLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Loading counts…")
                }
            }
            if (state.previewError != null) {
                Text(text = state.previewError, color = MaterialTheme.colorScheme.error)
            }
            state.previewCounts?.let { counts ->
                Text(text = "Work items: ${counts.workItems}")
                Text(text = "Events: ${counts.events}")
                Text(text = "Evidence: ${counts.evidence}")
            }
        }
    }
}

@Composable
private fun ExportActionsCard(
    state: ExportUiState,
    onToggleEvidenceZip: (Boolean) -> Unit,
    onExportEvidenceZip: () -> Unit,
    onExportReportJson: () -> Unit,
    onExportReportCsv: () -> Unit,
    onExportManifest: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Export actions", style = MaterialTheme.typography.titleMedium)
            if (state.bannerMessage != null) {
                val bannerColor = when (state.bannerType) {
                    BannerType.SUCCESS -> MaterialTheme.colorScheme.primary
                    BannerType.ERROR -> MaterialTheme.colorScheme.error
                }
                Text(text = state.bannerMessage, color = bannerColor)
            }
            if (state.reportJsonError != null) {
                Text(text = state.reportJsonError, color = MaterialTheme.colorScheme.error)
            }
            if (state.lastReportJsonMessage != null) {
                Text(text = state.lastReportJsonMessage, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onExportReportJson,
                enabled = !state.isReportJsonExporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isReportJsonExporting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = if (state.isReportJsonExporting) "Exporting JSON..." else "Export JSON")
            }
            if (state.reportCsvError != null) {
                Text(text = state.reportCsvError, color = MaterialTheme.colorScheme.error)
            }
            if (state.lastReportCsvMessage != null) {
                Text(text = state.lastReportCsvMessage, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onExportReportCsv,
                enabled = !state.isReportCsvExporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isReportCsvExporting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = if (state.isReportCsvExporting) "Exporting CSV..." else "Export CSV")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Include evidence ZIP", modifier = Modifier.weight(1f))
                Switch(checked = state.includeEvidenceZip, onCheckedChange = onToggleEvidenceZip)
            }
            if (state.evidenceZipError != null) {
                Text(text = state.evidenceZipError, color = MaterialTheme.colorScheme.error)
            }
            if (state.lastEvidenceZipPath != null) {
                Text(text = "Evidence zip: ${state.lastEvidenceZipPath}", style = MaterialTheme.typography.bodySmall)
            }
            if (state.evidenceZipMissingCount != null) {
                Text(text = "Missing evidence files: ${state.evidenceZipMissingCount}", style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onExportEvidenceZip,
                enabled = state.includeEvidenceZip && !state.isEvidenceZipExporting && state.selectedPeriod != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isEvidenceZipExporting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = if (state.isEvidenceZipExporting) "Exporting evidence..." else "Export Evidence ZIP")
            }
            if (state.manifestError != null) {
                Text(text = state.manifestError, color = MaterialTheme.colorScheme.error)
            }
            if (state.lastManifestMessage != null) {
                Text(text = state.lastManifestMessage, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onExportManifest,
                enabled = !state.isManifestExporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isManifestExporting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = if (state.isManifestExporting) "Exporting manifest..." else "Export manifest")
            }
        }
    }
}
