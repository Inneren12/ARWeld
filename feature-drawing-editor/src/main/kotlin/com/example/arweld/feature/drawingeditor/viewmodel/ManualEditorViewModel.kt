package com.example.arweld.feature.drawingeditor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
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
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(EditorState())
    val uiState: StateFlow<EditorState> = mutableUiState.asStateFlow()

    init {
        loadDrawing()
    }

    fun onIntent(intent: EditorIntent) {
        when (intent) {
            EditorIntent.SaveRequested -> saveDrawing()
            EditorIntent.LoadRequested -> loadDrawing()
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

    private fun reduce(intent: EditorIntent) {
        mutableUiState.update { current ->
            reduceEditorState(current, intent)
        }
    }
}
