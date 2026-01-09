package com.example.arweld.ui.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.di.ScannerEntryPoint
import com.example.arweld.feature.scanner.ui.ScanCodeScreen
import com.example.arweld.navigation.workItemSummaryRoute
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ScanCodeRoute(
    navController: NavHostController,
    viewModel: ScanCodeViewModel = hiltViewModel(),
) {
    val resolutionState = viewModel.resolutionState.collectAsState().value
    val context = LocalContext.current
    val scannerEngine = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ScannerEntryPoint::class.java,
        ).scannerEngine()
    }

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
        resolutionState = resolutionState,
        onResolutionReset = viewModel::resetResolution,
        scannerEngine = scannerEngine,
    )
}
