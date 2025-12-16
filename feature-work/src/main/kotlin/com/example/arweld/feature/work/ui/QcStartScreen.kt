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
    onNavigateToAr: (String) -> Unit,
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
                                style = MaterialTheme.typography.bodyLarge
                            )
                            uiState.code?.takeIf { it.isNotEmpty() }?.let { code ->
                                Text(text = "Code: $code", style = MaterialTheme.typography.bodyMedium)
                            }
                            uiState.zone?.let { zone ->
                                Text(text = "Zone: $zone", style = MaterialTheme.typography.bodyMedium)
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
