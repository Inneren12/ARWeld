package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.supervisor.ui.WorkItemDetailScreen
import com.example.arweld.feature.supervisor.viewmodel.WorkItemDetailViewModel

@Composable
fun WorkItemDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: WorkItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    WorkItemDetailScreen(
        detail = uiState.detail,
        timeline = uiState.timeline,
        evidence = uiState.evidence,
        onNavigateBack = onNavigateBack
    )
}
