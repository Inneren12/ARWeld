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
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import com.example.arweld.feature.drawingeditor.viewmodel.EditorState
import com.example.arweld.feature.drawingeditor.viewmodel.EditorTool

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualEditorScreen(
    uiState: EditorState,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(text = "Manual Editor") })
                ToolSelectorRow(
                    selectedTool = uiState.tool,
                    onToolSelected = onToolSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            BottomSheetPlaceholder(
                selectedTool = uiState.tool,
                summaryText = buildSummaryText(uiState.drawing),
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

                uiState.lastError != null -> {
                    Text(
                        text = uiState.lastError ?: "Unknown error",
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
                            text = buildSummaryText(uiState.drawing),
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
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorTool.values().forEach { tool ->
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
    selectedTool: EditorTool,
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

private fun toolLabel(tool: EditorTool): String = when (tool) {
    EditorTool.SELECT -> "Select"
    EditorTool.SCALE -> "Scale"
    EditorTool.NODE -> "Node"
    EditorTool.MEMBER -> "Member"
}

private fun buildSummaryText(drawing: Drawing2D): String {
    val missingRefs = drawing.missingNodeReferences()
    val scaleStatus = if (drawing.scale != null) "Scale calibrated" else "Scale not set"
    return "Nodes: ${drawing.nodes.size} • Members: ${drawing.members.size} • " +
        "Missing refs: ${missingRefs.size} • $scaleStatus"
}
