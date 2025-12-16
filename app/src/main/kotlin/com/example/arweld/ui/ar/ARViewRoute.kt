package com.example.arweld.ui.ar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.arweld.feature.arview.ui.arview.ARViewScreen

@Composable
fun ARViewRoute(
    navController: NavHostController,
    workItemId: String?,
) {
    ARViewScreen(
        workItemId = workItemId,
        onBack = { navController.popBackStack() },
    ) {
        // Placeholder for overlay content (tracking/debug info)
    }
}
