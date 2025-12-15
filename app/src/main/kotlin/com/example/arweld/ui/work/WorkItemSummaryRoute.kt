package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.WorkItemSummaryScreen
import com.example.arweld.feature.work.viewmodel.WorkItemSummaryViewModel

@Composable
@Suppress("UnusedParameter")
fun WorkItemSummaryRoute(
    navController: NavHostController,
    workItemId: String?,
    viewModel: WorkItemSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(workItemId) {
        if (workItemId == null) {
            viewModel.onMissingWorkItem()
        } else {
            viewModel.load(workItemId)
        }
    }

    WorkItemSummaryScreen(
        state = state,
        onClaimWork = { viewModel.onClaimWork() },
        onStartWork = { viewModel.onStartWork() },
        onMarkReadyForQc = { viewModel.onMarkReadyForQc() },
        onRefresh = { viewModel.refresh() },
        onBack = { navController.popBackStack() }
    )
}
