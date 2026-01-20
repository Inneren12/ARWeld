package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.supervisor.ui.OfflineQueueScreen
import com.example.arweld.feature.supervisor.viewmodel.OfflineQueueViewModel

@Composable
fun OfflineQueueRoute(
    viewModel: OfflineQueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    OfflineQueueScreen(
        state = uiState,
    )
}
