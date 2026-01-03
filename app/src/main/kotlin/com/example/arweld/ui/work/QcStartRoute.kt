package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.model.AR_SCREENSHOT_REQUEST_KEY
import com.example.arweld.feature.work.model.AR_SCREENSHOT_RESULT_KEY
import com.example.arweld.feature.work.model.ArScreenshotResult
import com.example.arweld.feature.work.ui.QcStartScreen
import com.example.arweld.feature.work.viewmodel.QcStartViewModel
import com.example.arweld.navigation.ROUTE_QC_QUEUE
import com.example.arweld.navigation.arViewRoute
import com.example.arweld.navigation.qcChecklistRoute

@Composable
fun QcStartRoute(
    navController: NavHostController,
    workItemId: String?,
    code: String?,
    viewModel: QcStartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val screenshotResultFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<ArScreenshotResult?>(AR_SCREENSHOT_RESULT_KEY, null)
    }
    val screenshotResult: ArScreenshotResult? by (screenshotResultFlow?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) })

    LaunchedEffect(workItemId) {
        if (workItemId == null) {
            viewModel.onMissingWorkItem()
        } else {
            viewModel.start(workItemId)
        }
    }

    LaunchedEffect(screenshotResult) {
        val result = screenshotResult ?: return@LaunchedEffect
        val resolvedId = uiState.workItemId ?: workItemId
        if (resolvedId != null) {
            viewModel.onArScreenshotCaptured(resolvedId, result)
        }
        savedStateHandle?.set(AR_SCREENSHOT_RESULT_KEY, null)
    }

    QcStartScreen(
        workItemId = workItemId,
        uiState = uiState,
        codeArg = code,
        onNavigateToAr = { id -> navController.navigate(arViewRoute(id)) },
        onOpenChecklist = { id, passedCode -> navController.navigate(qcChecklistRoute(id, passedCode)) },
        onCapturePhoto = { id -> viewModel.capturePhoto(id) },
        onCaptureArScreenshot = { id ->
            savedStateHandle?.set(AR_SCREENSHOT_REQUEST_KEY, true)
            savedStateHandle?.set(AR_SCREENSHOT_RESULT_KEY, null)
            navController.navigate(arViewRoute(id))
        },
        onBackToQueue = {
            val popped = navController.popBackStack(ROUTE_QC_QUEUE, false)
            if (!popped) {
                navController.navigate(ROUTE_QC_QUEUE)
            }
        }
    )
}
