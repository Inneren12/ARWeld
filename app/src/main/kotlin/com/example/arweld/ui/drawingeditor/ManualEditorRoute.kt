package com.example.arweld.ui.drawingeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.drawingeditor.ui.ManualEditorScreen
import com.example.arweld.feature.drawingeditor.viewmodel.ManualEditorViewModel

@Composable
fun ManualEditorRoute(
    viewModel: ManualEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ManualEditorScreen(
        uiState = uiState,
        onToolSelected = viewModel::onToolSelected,
    )
}
