package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.QcChecklistScreen
import com.example.arweld.feature.work.viewmodel.QcChecklistViewModel
import com.example.arweld.navigation.qcStartRoute

@Composable
fun QcChecklistRoute(
    navController: NavHostController,
    workItemId: String?,
    viewModel: QcChecklistViewModel = hiltViewModel(),
) {
    QcChecklistScreen(
        viewModel = viewModel,
        onContinue = {
            val returned = navController.popBackStack()
            if (!returned && workItemId != null) {
                navController.navigate(qcStartRoute(workItemId))
            }
        }
    )
}
