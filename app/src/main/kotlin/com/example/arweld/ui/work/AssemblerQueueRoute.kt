package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.AssemblerQueueScreen
import com.example.arweld.feature.work.viewmodel.AssemblerQueueViewModel
import com.example.arweld.navigation.workItemSummaryRoute

@Composable
fun AssemblerQueueRoute(
    navController: NavHostController,
    viewModel: AssemblerQueueViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    AssemblerQueueScreen(
        state = state,
        onWorkItemClick = { workItemId ->
            navController.navigate(workItemSummaryRoute(workItemId))
        },
        onClaimWorkItem = { workItemId -> viewModel.claimWork(workItemId) },
        onRefresh = { viewModel.refresh() },
        onBack = { navController.popBackStack() }
    )
}
