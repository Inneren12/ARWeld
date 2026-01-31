package com.example.arweld.feature.drawingeditor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import com.example.arweld.feature.drawingeditor.diagnostics.EditorDiagnosticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ManualEditorViewModel @Inject constructor(
    private val drawing2DRepository: Drawing2DRepository,
    private val editorDiagnosticsLogger: EditorDiagnosticsLogger,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(EditorState())
    val uiState: StateFlow<EditorState> = mutableUiState.asStateFlow()

    init {
        editorDiagnosticsLogger.logEditorOpened()
        loadDrawing()
    }

    fun onIntent(intent: EditorIntent) {
        when (intent) {
            EditorIntent.SaveRequested -> saveDrawing()
            EditorIntent.LoadRequested -> loadDrawing()
            EditorIntent.ScaleApplyRequested -> applyScale()
            EditorIntent.ScaleResetRequested -> resetScale()
            EditorIntent.UndoRequested -> applyUndo()
            EditorIntent.RedoRequested -> applyRedo()
            is EditorIntent.NodeTap -> handleNodeTap(intent)
            is EditorIntent.NodeDragStart -> reduce(intent)
            is EditorIntent.NodeDragMove -> reduce(intent)
            is EditorIntent.NodeDragEnd -> handleNodeDragEnd(intent)
            EditorIntent.NodeDragCancel -> reduce(intent)
            is EditorIntent.NodeDeleteRequested -> handleNodeDelete(intent)
            is EditorIntent.ToolChanged -> {
                val previousTool = mutableUiState.value.tool
                reduce(intent)
                editorDiagnosticsLogger.logToolChanged(
                    tool = intent.tool.name,
                    previousTool = previousTool.name,
                )
            }
            else -> reduce(intent)
        }
    }

    private fun loadDrawing() {
        viewModelScope.launch {
            reduce(EditorIntent.LoadRequested)
            runCatching { drawing2DRepository.getCurrentDrawing() }
                .onSuccess { drawing ->
                    reduce(EditorIntent.Loaded(drawing))
                }
                .onFailure { error ->
                    reduce(EditorIntent.Error(error.message ?: "Failed to load drawing"))
                }
        }
    }

    private fun saveDrawing() {
        viewModelScope.launch {
            reduce(EditorIntent.SaveRequested)
            val drawing = mutableUiState.value.drawing
            runCatching { drawing2DRepository.saveCurrentDrawing(drawing) }
                .onSuccess { reduce(EditorIntent.Saved) }
                .onFailure { error ->
                    reduce(EditorIntent.Error(error.message ?: "Failed to save drawing"))
                }
        }
    }

    private fun applyScale() {
        viewModelScope.launch {
            reduce(EditorIntent.ScaleApplyRequested)
            val state = mutableUiState.value
            val draft = state.scaleDraft
            val pointA = draft.pointA
            val pointB = draft.pointB
            if (pointA == null || pointB == null) {
                reduce(EditorIntent.ScaleApplyFailed("Select two points before applying scale."))
                return@launch
            }
            val parsedLength = parseStrictNumber(draft.inputText)
            if (parsedLength == null) {
                reduce(EditorIntent.ScaleApplyFailed("Enter a valid length in mm."))
                return@launch
            }
            if (parsedLength <= 0.0) {
                reduce(EditorIntent.ScaleApplyFailed("Length must be > 0 mm."))
                return@launch
            }
            val distance = distanceBetween(pointA, pointB)
            if (distance <= SCALE_DISTANCE_EPSILON) {
                reduce(EditorIntent.ScaleApplyFailed("Points are too close to compute scale."))
                return@launch
            }
            val updatedDrawing = state.drawing.copy(
                scale = ScaleInfo(
                    pointA = pointA,
                    pointB = pointB,
                    realLengthMm = parsedLength,
                )
            )
            runCatching { drawing2DRepository.saveCurrentDrawing(updatedDrawing) }
                .onSuccess {
                    reduce(EditorIntent.ScaleApplied(updatedDrawing))
                    editorDiagnosticsLogger.logScaleSet(
                        realWorldLength = parsedLength,
                        unit = "mm",
                    )
                }
                .onFailure { error ->
                    reduce(EditorIntent.ScaleApplyFailed(error.message ?: "Failed to save scale."))
                }
        }
    }

    private fun resetScale() {
        viewModelScope.launch {
            reduce(EditorIntent.ScaleResetRequested)
            val state = mutableUiState.value
            if (state.drawing.scale == null) {
                reduce(EditorIntent.ScaleResetFailed("Scale is already cleared."))
                return@launch
            }
            val updatedDrawing = state.drawing.copy(scale = null)
            runCatching { drawing2DRepository.saveCurrentDrawing(updatedDrawing) }
                .onSuccess { reduce(EditorIntent.ScaleResetApplied(updatedDrawing)) }
                .onFailure { error ->
                    reduce(EditorIntent.ScaleResetFailed(error.message ?: "Failed to reset scale."))
                }
        }
    }

    private fun applyUndo() {
        viewModelScope.launch {
            val updated = reduceAndReturn(EditorIntent.UndoRequested)
            if (updated != null) {
                persistUpdatedDrawing(updated)
            }
        }
    }

    private fun applyRedo() {
        viewModelScope.launch {
            val updated = reduceAndReturn(EditorIntent.RedoRequested)
            if (updated != null) {
                persistUpdatedDrawing(updated)
            }
        }
    }

    private fun handleNodeTap(intent: EditorIntent.NodeTap) {
        viewModelScope.launch {
            val previous = mutableUiState.value
            val updated = reduceAndReturn(intent) ?: return@launch
            val nodeAdded = updated.drawing.nodes.size > previous.drawing.nodes.size
            if (!nodeAdded) {
                return@launch
            }
            runCatching { drawing2DRepository.saveCurrentDrawing(updated.drawing) }
                .onSuccess {
                    val nodeId = (updated.selection as? EditorSelection.Node)?.id
                    if (nodeId != null) {
                        editorDiagnosticsLogger.logNodeAdded(
                            nodeId = nodeId,
                            x = intent.worldPoint.x,
                            y = intent.worldPoint.y,
                        )
                    }
                }
                .onFailure { error ->
                    reduce(EditorIntent.Error(error.message ?: "Failed to save drawing."))
                }
        }
    }

    private fun handleNodeDragEnd(intent: EditorIntent.NodeDragEnd) {
        viewModelScope.launch {
            val previous = mutableUiState.value
            val dragState = previous.nodeDragState ?: return@launch
            val updated = reduceAndReturn(intent) ?: return@launch
            if (updated.drawing == previous.drawing) {
                return@launch
            }
            runCatching { drawing2DRepository.saveCurrentDrawing(updated.drawing) }
                .onSuccess {
                    val node = updated.drawing.nodes.firstOrNull { it.id == dragState.nodeId }
                    if (node != null) {
                        editorDiagnosticsLogger.logNodeMoved(
                            nodeId = dragState.nodeId,
                            fromX = dragState.startWorldPos.x,
                            fromY = dragState.startWorldPos.y,
                            toX = node.x,
                            toY = node.y,
                        )
                    }
                }
                .onFailure { error ->
                    reduce(EditorIntent.Error(error.message ?: "Failed to save drawing."))
                }
        }
    }

    private fun handleNodeDelete(intent: EditorIntent.NodeDeleteRequested) {
        viewModelScope.launch {
            val updated = reduceAndReturn(intent) ?: return@launch
            persistUpdatedDrawing(updated)
        }
    }

    private suspend fun persistUpdatedDrawing(updatedState: EditorState) {
        runCatching { drawing2DRepository.saveCurrentDrawing(updatedState.drawing) }
            .onFailure { error ->
                reduce(EditorIntent.Error(error.message ?: "Failed to save drawing."))
            }
    }

    private fun reduce(intent: EditorIntent) {
        mutableUiState.update { current ->
            reduceEditorState(current, intent)
        }
    }

    private fun reduceAndReturn(intent: EditorIntent): EditorState? {
        val current = mutableUiState.value
        val updated = reduceEditorState(current, intent)
        if (updated == current) {
            return null
        }
        mutableUiState.value = updated
        return updated
    }

    private companion object {
        const val SCALE_DISTANCE_EPSILON = 1e-6
    }
}
