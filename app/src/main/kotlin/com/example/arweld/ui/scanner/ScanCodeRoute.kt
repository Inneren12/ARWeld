package com.example.arweld.ui.scanner

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.example.arweld.feature.scanner.ui.ScanCodeScreen
import com.example.arweld.navigation.workItemSummaryRoute

@Composable
fun ScanCodeRoute(
    navController: NavHostController,
    viewModel: ScanCodeViewModel = hiltViewModel(),
) {
    val errorMessage = viewModel.errorMessage.collectAsState().value

    ScanCodeScreen(
        onCodeResolved = { code ->
            viewModel.resolveCode(
                code = code,
                onFound = { workItemId ->
                    navController.navigate(workItemSummaryRoute(workItemId))
                }
            )
        },
        onBack = { navController.popBackStack() },
        errorMessage = errorMessage,
        onErrorConsumed = viewModel::clearError,
    )
}
