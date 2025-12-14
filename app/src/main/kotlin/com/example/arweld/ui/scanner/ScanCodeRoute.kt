package com.example.arweld.ui.scanner

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.arweld.feature.scanner.ui.ScanCodeScreen
import com.example.arweld.navigation.ROUTE_WORK_ITEM_SUMMARY

@Composable
fun ScanCodeRoute(navController: NavHostController) {
    ScanCodeScreen(
        onCodeResolved = {
            navController.navigate(ROUTE_WORK_ITEM_SUMMARY)
        },
        onBack = { navController.popBackStack() },
    )
}
