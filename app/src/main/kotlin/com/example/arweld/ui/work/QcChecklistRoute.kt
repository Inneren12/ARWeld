package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.QcChecklistScreen
import com.example.arweld.feature.work.viewmodel.QcChecklistViewModel
import com.example.arweld.navigation.qcFailReasonRoute
import com.example.arweld.navigation.qcPassConfirmRoute
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun QcChecklistRoute(
    navController: NavHostController,
    workItemId: String?,
    code: String?,
    viewModel: QcChecklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workItemId) {
        viewModel.initialize(workItemId)
    }

    QcChecklistScreen(
        uiState = uiState,
        workItemId = workItemId,
        code = code,
        onUpdateItem = viewModel::updateItemState,
        onNavigateBack = { navController.popBackStack() },
        onPass = { result ->
            val id = workItemId ?: return@QcChecklistScreen
            val checklistParam = Json.encodeToString(result)
            navController.navigate(qcPassConfirmRoute(id, checklistParam, code))
        },
        onFail = { result ->
            val id = workItemId ?: return@QcChecklistScreen
            val checklistParam = Json.encodeToString(result)
            navController.navigate(qcFailReasonRoute(id, checklistParam, code))
        }
    )
}
