package com.example.arweld.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arweld.ui.auth.LoginRoute
import com.example.arweld.ui.home.HomeRoute
import com.example.arweld.ui.placeholder.PlaceholderScreen

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
        composable(ROUTE_WORK_ITEM_SUMMARY) {
            PlaceholderScreen(title = "Work Item Summary")
        }
        composable(ROUTE_TIMELINE) {
            PlaceholderScreen(title = "Timeline")
        }
    }
}
