package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.QcQueueScreen
import com.example.arweld.feature.work.viewmodel.QcQueueViewModel
import com.example.arweld.navigation.qcStartRoute

@Composable
fun QcQueueRoute(
    navController: NavHostController,
    viewModel: QcQueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    QcQueueScreen(
        uiState = uiState,
        onStartInspection = { workItemId, code ->
            navController.navigate(qcStartRoute(workItemId, code))
        },
        onRefresh = { viewModel.refresh() },
        onBack = { navController.popBackStack() }
    )
}
