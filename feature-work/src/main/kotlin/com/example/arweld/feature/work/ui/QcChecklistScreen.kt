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
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistItem
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.feature.work.viewmodel.QcChecklistUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QcChecklistScreen(
    uiState: QcChecklistUiState,
    workItemId: String?,
    code: String?,
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
                    onClick = { onFail(uiState.checklist) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.policySatisfied,
                ) {
                    Text(text = "Отклонить")
                }

                Button(
                    onClick = { onPass(uiState.checklist) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.policySatisfied,
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
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "QC Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Work item: ${workItemId ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        code?.takeIf { it.isNotEmpty() }?.let { itemCode ->
                            Text(
                                text = "Code: $itemCode",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                EvidencePolicyCard(
                    evidenceCounts = uiState.evidenceCounts,
                    missingEvidence = uiState.missingEvidence,
                    policySatisfied = uiState.policySatisfied,
                    errorMessage = uiState.errorMessage,
                )
            }

            items(uiState.checklist.items) { item ->
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

@Composable
private fun EvidencePolicyCard(
    evidenceCounts: Map<EvidenceKind, Int>,
    missingEvidence: Set<EvidenceKind>,
    policySatisfied: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Evidence status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            EvidenceCountRow(
                label = "Photo",
                count = evidenceCounts[EvidenceKind.PHOTO] ?: 0,
            )
            EvidenceCountRow(
                label = "AR screenshot",
                count = evidenceCounts[EvidenceKind.AR_SCREENSHOT] ?: 0,
            )
            if (!policySatisfied) {
                Text(
                    text = missingEvidenceMessage(missingEvidence),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun EvidenceCountRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = "x$count", style = MaterialTheme.typography.bodySmall)
    }
}

private fun missingEvidenceMessage(missingEvidence: Set<EvidenceKind>): String {
    if (missingEvidence.isEmpty()) return "Required evidence captured."
    val parts = mutableListOf<String>()
    if (EvidenceKind.PHOTO in missingEvidence) parts.add("1 photo")
    if (EvidenceKind.AR_SCREENSHOT in missingEvidence) parts.add("1 AR screenshot")
    return "Need: ${parts.joinToString(separator = " + ")}"
}
