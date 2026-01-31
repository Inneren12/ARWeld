package com.example.arweld.feature.drawingeditor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualEditorViewModel @Inject constructor(
    private val drawing2DRepository: Drawing2DRepository,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(ManualEditorUiState())
    val uiState: StateFlow<ManualEditorUiState> = mutableUiState.asStateFlow()

    init {
        loadDrawing()
    }

    fun onToolSelected(tool: ManualEditorTool) {
        mutableUiState.update { it.copy(selectedTool = tool) }
    }

    private fun loadDrawing() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { drawing2DRepository.getCurrentDrawing() }
                .onSuccess { drawing ->
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            drawing = drawing,
                            summary = drawing.toSummary(),
                        )
                    }
                }
                .onFailure { error ->
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            drawing = emptyDrawing(),
                            summary = emptyDrawing().toSummary(),
                            errorMessage = error.message ?: "Failed to load drawing",
                        )
                    }
                }
        }
    }

    private fun Drawing2D.toSummary(): Drawing2DSummary {
        val missingRefs = missingNodeReferences()
        return Drawing2DSummary(
            nodeCount = nodes.size,
            memberCount = members.size,
            missingNodeRefs = missingRefs.size,
            hasScale = scale != null,
        )
    }

    private fun emptyDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())
}
