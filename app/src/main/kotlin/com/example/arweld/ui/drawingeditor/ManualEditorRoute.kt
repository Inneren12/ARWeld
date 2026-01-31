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
        onScalePointSelected = { point -> viewModel.onIntent(EditorIntent.ScalePointSelected(point)) },
        onScaleLengthChanged = { text -> viewModel.onIntent(EditorIntent.ScaleLengthChanged(text)) },
        onScaleApply = { viewModel.onIntent(EditorIntent.ScaleApplyRequested) },
        onScaleReset = { viewModel.onIntent(EditorIntent.ScaleResetRequested) },
        onUndo = { viewModel.onIntent(EditorIntent.UndoRequested) },
        onRedo = { viewModel.onIntent(EditorIntent.RedoRequested) },
        onNodeDelete = { nodeId -> viewModel.onIntent(EditorIntent.NodeDeleteRequested(nodeId)) },
        onMemberDelete = { memberId -> viewModel.onIntent(EditorIntent.MemberDeleteRequested(memberId)) },
        onNodeTap = { point, tolerancePx ->
            viewModel.onIntent(EditorIntent.NodeTap(point, tolerancePx))
        },
        onMemberNodeTap = { nodeId ->
            viewModel.onIntent(EditorIntent.MemberNodeTapped(nodeId))
        },
        onNodeDragStart = { nodeId, pointerWorld ->
            viewModel.onIntent(EditorIntent.NodeDragStart(nodeId, pointerWorld))
        },
        onNodeDragMove = { pointerWorld ->
            viewModel.onIntent(EditorIntent.NodeDragMove(pointerWorld))
        },
        onNodeDragEnd = { pointerWorld ->
            viewModel.onIntent(EditorIntent.NodeDragEnd(pointerWorld))
        },
        onNodeDragCancel = {
            viewModel.onIntent(EditorIntent.NodeDragCancel)
        },
        onSelectEntity = { selection -> viewModel.onIntent(EditorIntent.SelectEntity(selection)) },
        onClearSelection = { viewModel.onIntent(EditorIntent.ClearSelection) },
        onNodeEditXChanged = { text -> viewModel.onIntent(EditorIntent.NodeEditXChanged(text)) },
        onNodeEditYChanged = { text -> viewModel.onIntent(EditorIntent.NodeEditYChanged(text)) },
        onNodeEditApply = { nodeId -> viewModel.onIntent(EditorIntent.NodeEditApplyRequested(nodeId)) },
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
