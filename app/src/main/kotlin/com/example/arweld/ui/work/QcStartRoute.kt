package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.QcStartScreen

@Composable
fun QcStartRoute(
    navController: NavHostController,
    workItemId: String?,
) {
    QcStartScreen(
        workItemId = workItemId,
        onBack = { navController.popBackStack() }
    )
}
