package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.QcStartScreen
import com.example.arweld.feature.work.viewmodel.QcStartViewModel
import com.example.arweld.navigation.ROUTE_QC_QUEUE
import com.example.arweld.navigation.arViewRoute

@Composable
fun QcStartRoute(
    navController: NavHostController,
    workItemId: String?,
    viewModel: QcStartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workItemId) {
        if (workItemId == null) {
            viewModel.onMissingWorkItem()
        } else {
            viewModel.start(workItemId)
        }
    }

    QcStartScreen(
        workItemId = workItemId,
        uiState = uiState,
        onNavigateToAr = { id -> navController.navigate(arViewRoute(id)) },
        onBackToQueue = {
            val popped = navController.popBackStack(ROUTE_QC_QUEUE, false)
            if (!popped) {
                navController.navigate(ROUTE_QC_QUEUE)
            }
        }
    )
}
