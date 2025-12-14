package com.example.arweld.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.arweld.feature.home.ui.HomeScreen
import com.example.arweld.feature.work.ui.TimelineScreen
import com.example.arweld.feature.work.ui.WorkItemSummaryScreen
import com.example.arweld.ui.auth.LoginScreen
import com.example.arweld.ui.auth.SplashScreen

/**
 * Main navigation setup for ARWeld app.
 * Defines two conceptual graphs:
 * - AuthGraph: Splash → Login
 * - MainGraph: Home → WorkItemSummary → Timeline
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        // Auth Graph
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        // Main Graph
        composable(Routes.HOME) {
            HomeRoute(navController = navController)
        }

        composable(Routes.WORK_ITEM_SUMMARY) {
            WorkItemSummaryScreen()
        }

        composable(Routes.TIMELINE) {
            TimelineScreen()
        }
    }
}

/**
 * Route wrapper for Home screen.
 * Wires navigation callbacks and provides ViewModel via Hilt.
 */
@Composable
private fun HomeRoute(navController: NavHostController) {
    HomeScreen(
        onNavigateToWorkSummary = {
            navController.navigate(Routes.WORK_ITEM_SUMMARY)
        },
        onNavigateToTimeline = {
            navController.navigate(Routes.TIMELINE)
        }
    )
}
