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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.work.viewmodel.QcStartUiState

@Composable
fun QcStartScreen(
    workItemId: String?,
    uiState: QcStartUiState,
    codeArg: String?,
    onNavigateToAr: (String) -> Unit,
    onOpenChecklist: (String, String?) -> Unit,
    onCapturePhoto: (String) -> Unit,
    onBackToQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "QC Start") },
                navigationIcon = {
                    IconButton(onClick = onBackToQueue) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Инициализация проверки...")
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                    Button(onClick = onBackToQueue) {
                        Text(text = "Назад в очередь")
                    }
                }
            }

            else -> {
                val resolvedId = uiState.workItemId ?: workItemId
                val resolvedCode = uiState.code ?: codeArg
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "QC Inspection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "ID: ${resolvedId ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            resolvedCode?.takeIf { it.isNotEmpty() }?.let { code ->
                                Text(text = "Code: $code", style = MaterialTheme.typography.bodyMedium)
                            }
                            uiState.zone?.let { zone ->
                                Text(text = "Zone: $zone", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Photos: ${uiState.evidenceCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (uiState.policyReasons.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Evidence requirements",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                uiState.policyReasons.forEach { reason ->
                                    Text(
                                        text = "• $reason",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        enabled = resolvedId != null,
                        onClick = { resolvedId?.let(onNavigateToAr) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Перейти в AR")
                    }

                    Button(
                        enabled = resolvedId != null && !uiState.actionInProgress,
                        onClick = { resolvedId?.let(onCapturePhoto) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Сделать фото")
                    }

                    Button(
                        enabled = resolvedId != null,
                        onClick = { resolvedId?.let { id -> onOpenChecklist(id, resolvedCode) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Заполнить чеклист")
                    }

                    if (!uiState.canCompleteQc) {
                        Text(
                            text = "требуются AR-скрин и фото",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    uiState.actionErrorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Button(
                        onClick = onBackToQueue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Назад в очередь")
                    }
                }
            }
        }
    }
}
