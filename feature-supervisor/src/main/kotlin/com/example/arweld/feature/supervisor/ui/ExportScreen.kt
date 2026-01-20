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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.viewmodel.BannerType
import com.example.arweld.feature.supervisor.viewmodel.ExportPeriodType
import com.example.arweld.feature.supervisor.viewmodel.ExportUiState
import com.example.arweld.feature.supervisor.viewmodel.ShiftSelection
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            state = state,
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
private fun PeriodSelectionCard(
    state: ExportUiState,
    onSelectPeriodType: (ExportPeriodType) -> Unit,
    onSelectShift: (ShiftSelection) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Select Period", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodTypeButton(
                    label = "Shift",
                    selected = state.periodType == ExportPeriodType.SHIFT,
                    onClick = { onSelectPeriodType(ExportPeriodType.SHIFT) },
                )
                PeriodTypeButton(
                    label = "Day",
                    selected = state.periodType == ExportPeriodType.DAY,
                    onClick = { onSelectPeriodType(ExportPeriodType.DAY) },
                )
            }
            Divider()
            if (state.periodType == ExportPeriodType.SHIFT) {
                Text(text = "Shift selection", style = MaterialTheme.typography.bodyMedium)
                ShiftSelectionRow(
                    label = state.currentShiftLabel,
                    selected = state.selectedShift == ShiftSelection.CURRENT,
                    onClick = { onSelectShift(ShiftSelection.CURRENT) },
                )
                ShiftSelectionRow(
                    label = state.previousShiftLabel,
                    selected = state.selectedShift == ShiftSelection.PREVIOUS,
                    onClick = { onSelectShift(ShiftSelection.PREVIOUS) },
                )
            } else {
                Text(text = "Select day", style = MaterialTheme.typography.bodyMedium)
                DayPickerRow(date = state.selectedDate, onSelectDate = onSelectDate)
            }
            if (state.selectedPeriod != null) {
                Text(text = "Active period: ${state.selectedPeriod.label}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PeriodTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(text = label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text = label)
        }
    }
}

@Composable
private fun ShiftSelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Button(onClick = onClick, enabled = !selected) {
                Text(text = if (selected) "Selected" else "Select")
            }
        }
    }
}

@Composable
private fun DayPickerRow(
    date: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    val label = DAY_PICKER_FORMAT.format(date)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = { onSelectDate(date.minusDays(1)) }) {
            Text(text = "Previous")
        }
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = { onSelectDate(date.plusDays(1)) }) {
            Text(text = "Next")
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

private val DAY_PICKER_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
