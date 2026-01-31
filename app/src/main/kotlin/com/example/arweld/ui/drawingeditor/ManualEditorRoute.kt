package com.example.arweld.ui.drawingeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.drawingeditor.ui.ManualEditorScreen
import com.example.arweld.feature.drawingeditor.viewmodel.EditorIntent
import com.example.arweld.feature.drawingeditor.viewmodel.ManualEditorViewModel

@Composable
fun ManualEditorRoute(
    viewModel: ManualEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ManualEditorScreen(
        uiState = uiState,
        onToolSelected = { tool -> viewModel.onIntent(EditorIntent.ToolChanged(tool)) },
        onTransformGesture = { panX, panY, zoomFactor, focalX, focalY ->
            viewModel.onIntent(
                EditorIntent.ViewTransformGesture(
                    panX = panX,
                    panY = panY,
                    zoomFactor = zoomFactor,
                    focalX = focalX,
                    focalY = focalY,
                )
            )
        },
    )
}
