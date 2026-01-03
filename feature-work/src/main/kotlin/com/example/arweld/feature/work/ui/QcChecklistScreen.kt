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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistItem
import com.example.arweld.core.domain.work.model.QcChecklistResult

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QcChecklistScreen(
    checklist: QcChecklistResult,
    onUpdateItem: (String, QcCheckState) -> Unit,
    onNavigateBack: () -> Unit,
    onPass: (QcChecklistResult) -> Unit,
    onFail: (QcChecklistResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "QC Checklist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onFail(checklist) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Отклонить")
                }

                Button(
                    onClick = { onPass(checklist) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Подтвердить")
                }
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
            items(checklist.items) { item ->
                QcChecklistRow(
                    item = item,
                    onStateChange = { state ->
                        onUpdateItem(item.id, state)
                    }
                )
            }
        }
    }
}

@Composable
private fun QcChecklistRow(
    item: QcChecklistItem,
    onStateChange: (QcCheckState) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title ?: item.id,
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
                    selected = item.state == QcCheckState.OK,
                    onClick = { onStateChange(QcCheckState.OK) }
                )
                ChecklistOption(
                    label = "NOT OK",
                    selected = item.state == QcCheckState.NOT_OK,
                    onClick = { onStateChange(QcCheckState.NOT_OK) }
                )
                ChecklistOption(
                    label = "N/A",
                    selected = item.state == QcCheckState.NA,
                    onClick = { onStateChange(QcCheckState.NA) }
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
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
