package com.example.arweld.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.arweld.ui.auth.LoginRoute
import com.example.arweld.ui.home.HomeRoute
import com.example.arweld.ui.work.AssemblerQueueRoute
import com.example.arweld.ui.work.WorkItemSummaryRoute
import com.example.arweld.feature.work.ui.TimelineScreen
import com.example.arweld.ui.scanner.ScanCodeRoute

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = modifier
    ) {
        composable(ROUTE_LOGIN) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_HOME) {
            HomeRoute(navController = navController)
        }
        composable(ROUTE_ASSEMBLER_QUEUE) {
            AssemblerQueueRoute(navController = navController)
        }
        composable(ROUTE_SCAN_CODE) {
            ScanCodeRoute(navController = navController)
        }
        composable(
            route = "$ROUTE_WORK_ITEM_SUMMARY?workItemId={workItemId}",
            arguments = listOf(
                navArgument("workItemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val workItemId = backStackEntry.arguments?.getString("workItemId")
            WorkItemSummaryRoute(
                navController = navController,
                workItemId = workItemId,
            )
        }
        composable(ROUTE_TIMELINE) {
            TimelineScreen()
        }
    }
}
