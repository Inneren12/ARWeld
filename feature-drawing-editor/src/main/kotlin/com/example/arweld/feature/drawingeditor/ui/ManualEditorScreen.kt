package com.example.arweld.feature.drawingeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.drawingeditor.viewmodel.ManualEditorTool
import com.example.arweld.feature.drawingeditor.viewmodel.ManualEditorUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualEditorScreen(
    uiState: ManualEditorUiState,
    onToolSelected: (ManualEditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(text = "Manual Editor") })
                ToolSelectorRow(
                    selectedTool = uiState.selectedTool,
                    onToolSelected = onToolSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            BottomSheetPlaceholder(
                selectedTool = uiState.selectedTool,
                summaryText = buildSummaryText(uiState),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Loading Drawing2D…")
                    }
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Canvas placeholder",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildSummaryText(uiState),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolSelectorRow(
    selectedTool: ManualEditorTool,
    onToolSelected: (ManualEditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ManualEditorTool.values().forEach { tool ->
            val isSelected = tool == selectedTool
            OutlinedButton(
                onClick = { onToolSelected(tool) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
            ) {
                Text(text = toolLabel(tool))
            }
        }
    }
}

@Composable
private fun BottomSheetPlaceholder(
    selectedTool: ManualEditorTool,
    summaryText: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bottom sheet placeholder",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tool: ${toolLabel(selectedTool)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun toolLabel(tool: ManualEditorTool): String = when (tool) {
    ManualEditorTool.SELECT -> "Select"
    ManualEditorTool.SCALE -> "Scale"
    ManualEditorTool.NODE -> "Node"
    ManualEditorTool.MEMBER -> "Member"
}

private fun buildSummaryText(uiState: ManualEditorUiState): String {
    val summary = uiState.summary
    val scaleStatus = if (summary.hasScale) "Scale calibrated" else "Scale not set"
    return "Nodes: ${summary.nodeCount} • Members: ${summary.memberCount} • " +
        "Missing refs: ${summary.missingNodeRefs} • $scaleStatus"
}
