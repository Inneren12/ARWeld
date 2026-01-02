package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.feature.work.ui.QcFailReasonScreen
import com.example.arweld.feature.work.viewmodel.QcFailReasonViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun QcFailReasonRoute(
    navController: NavHostController,
    workItemId: String?,
    code: String?,
    checklistJson: String?,
    viewModel: QcFailReasonViewModel = hiltViewModel(),
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

    QcFailReasonScreen(
        uiState = uiState,
        onReasonsChange = viewModel::updateReasonsInput,
        onCommentChange = viewModel::updateComment,
        onPriorityChange = viewModel::updatePriority,
        onSubmit = viewModel::submit,
        onBack = { navController.popBackStack() },
    )
}
