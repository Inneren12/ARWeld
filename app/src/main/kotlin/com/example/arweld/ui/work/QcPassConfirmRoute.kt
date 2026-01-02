package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.feature.work.ui.QcPassConfirmScreen
import com.example.arweld.feature.work.viewmodel.QcPassConfirmViewModel
import com.example.arweld.navigation.ROUTE_QC_QUEUE
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun QcPassConfirmRoute(
    navController: NavHostController,
    workItemId: String?,
    code: String?,
    checklistJson: String?,
    viewModel: QcPassConfirmViewModel = hiltViewModel(),
) {
    val checklist = remember(checklistJson) {
        checklistJson?.let { runCatching { Json.decodeFromString<QcChecklistResult>(it) }.getOrNull() }
    }

    LaunchedEffect(workItemId, code, checklist) {
        viewModel.initialize(workItemId, code, checklist)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.completed) {
        if (uiState.completed) {
            navigateBackToQcQueue(navController)
        }
    }

    QcPassConfirmScreen(
        uiState = uiState,
        onCommentChange = viewModel::updateComment,
        onSubmit = viewModel::submit,
        onBack = { navController.popBackStack() },
    )
}

internal fun navigateBackToQcQueue(navController: NavHostController) {
    val popped = navController.popBackStack(ROUTE_QC_QUEUE, false)
    if (!popped) {
        navController.navigate(ROUTE_QC_QUEUE) {
            popUpTo(ROUTE_QC_QUEUE)
            launchSingleTop = true
        }
    }
}
