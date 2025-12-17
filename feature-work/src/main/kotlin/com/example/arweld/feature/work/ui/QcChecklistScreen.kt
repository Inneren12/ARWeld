package com.example.arweld.feature.work.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.work.viewmodel.ChecklistItemState
import com.example.arweld.feature.work.viewmodel.QcChecklistItem
import com.example.arweld.feature.work.viewmodel.QcChecklistViewModel

@Composable
fun QcChecklistScreen(
    viewModel: QcChecklistViewModel,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val checklistResult by viewModel.checklistResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "QC Checklist") },
                navigationIcon = {
                    IconButton(onClick = onContinue) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Продолжить → выбор PASS/FAIL")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(checklistResult.items) { item ->
                QcChecklistRow(
                    item = item,
                    onStateChange = { state ->
                        viewModel.updateItemState(item.id, state)
                    }
                )
            }
        }
    }
}

@Composable
private fun QcChecklistRow(
    item: QcChecklistItem,
    onStateChange: (ChecklistItemState) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (item.required) {
                Text(
                    text = "Обязательный пункт",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChecklistOption(
                    label = "OK",
                    selected = item.state == ChecklistItemState.OK,
                    onClick = { onStateChange(ChecklistItemState.OK) }
                )
                ChecklistOption(
                    label = "NOT OK",
                    selected = item.state == ChecklistItemState.NOT_OK,
                    onClick = { onStateChange(ChecklistItemState.NOT_OK) }
                )
                ChecklistOption(
                    label = "N/A",
                    selected = item.state == ChecklistItemState.NA,
                    onClick = { onStateChange(ChecklistItemState.NA) }
                )
            }
        }
    }
}

@Composable
private fun ChecklistOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
