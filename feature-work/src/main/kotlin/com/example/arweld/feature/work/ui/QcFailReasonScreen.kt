package com.example.arweld.feature.work.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.work.viewmodel.QcFailReasonUiState

@Composable
fun QcFailReasonScreen(
    uiState: QcFailReasonUiState,
    onReasonsChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Отклонить QC") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            uiState.checklist == null || uiState.workItemId == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Нет данных чеклиста",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(onClick = onBack) { Text(text = "Назад") }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Работа: ${uiState.workItemId}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            uiState.code?.takeIf { it.isNotEmpty() }?.let { code ->
                                Text(text = "Код: $code", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Пункты чеклиста: ${uiState.checklist.items.size}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.reasonsInput,
                        onValueChange = onReasonsChange,
                        label = { Text(text = "Причины отклонения (через запятую или строки)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )

                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = onCommentChange,
                        label = { Text(text = "Комментарий (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "Приоритет: ${uiState.priority}")
                        Slider(
                            value = uiState.priority.toFloat(),
                            onValueChange = { onPriorityChange(it.toInt()) },
                            valueRange = 1f..3f,
                            steps = 1,
                            colors = SliderDefaults.colors(),
                        )
                    }

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    if (!uiState.policySatisfied) {
                        Text(
                            text = "Need: 1 photo + 1 AR screenshot",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Button(
                        onClick = onSubmit,
                        enabled = !uiState.isSubmitting && uiState.policySatisfied,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        }
                        Text(text = "Отправить на доработку")
                    }
                }
            }
        }
    }
}
