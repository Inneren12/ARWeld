package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.supervisor.viewmodel.ExportPeriodType
import com.example.arweld.feature.supervisor.viewmodel.ShiftSelection
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PeriodSelectionCard(
    periodType: ExportPeriodType,
    selectedShift: ShiftSelection,
    selectedDate: LocalDate,
    currentShiftLabel: String,
    previousShiftLabel: String,
    selectedPeriodLabel: String?,
    onSelectPeriodType: (ExportPeriodType) -> Unit,
    onSelectShift: (ShiftSelection) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Select Period",
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodTypeButton(
                    label = "Shift",
                    selected = periodType == ExportPeriodType.SHIFT,
                    onClick = { onSelectPeriodType(ExportPeriodType.SHIFT) },
                )
                PeriodTypeButton(
                    label = "Day",
                    selected = periodType == ExportPeriodType.DAY,
                    onClick = { onSelectPeriodType(ExportPeriodType.DAY) },
                )
            }
            Divider()
            if (periodType == ExportPeriodType.SHIFT) {
                Text(text = "Shift selection", style = MaterialTheme.typography.bodyMedium)
                ShiftSelectionRow(
                    label = currentShiftLabel,
                    selected = selectedShift == ShiftSelection.CURRENT,
                    onClick = { onSelectShift(ShiftSelection.CURRENT) },
                )
                ShiftSelectionRow(
                    label = previousShiftLabel,
                    selected = selectedShift == ShiftSelection.PREVIOUS,
                    onClick = { onSelectShift(ShiftSelection.PREVIOUS) },
                )
            } else {
                Text(text = "Select day", style = MaterialTheme.typography.bodyMedium)
                DayPickerRow(date = selectedDate, onSelectDate = onSelectDate)
            }
            if (selectedPeriodLabel != null) {
                Text(text = "Active period: $selectedPeriodLabel", style = MaterialTheme.typography.bodySmall)
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

private val DAY_PICKER_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
