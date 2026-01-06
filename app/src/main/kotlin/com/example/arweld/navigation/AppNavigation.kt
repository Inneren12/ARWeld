package com.example.arweld.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.example.arweld.ui.auth.LoginRoute
import com.example.arweld.ui.auth.SplashScreen
import com.example.arweld.ui.home.HomeRoute
import com.example.arweld.ui.ar.ARViewRoute
import com.example.arweld.ui.work.AssemblerQueueRoute
import com.example.arweld.ui.work.QcQueueRoute
import com.example.arweld.ui.work.QcChecklistRoute
import com.example.arweld.ui.work.QcFailReasonRoute
import com.example.arweld.ui.work.QcPassConfirmRoute
import com.example.arweld.ui.work.QcStartRoute
import com.example.arweld.ui.work.WorkItemSummaryRoute
import com.example.arweld.feature.work.ui.TimelineScreen
import com.example.arweld.ui.scanner.ScanCodeRoute
import com.example.arweld.ui.supervisor.SupervisorDashboardRoute
import com.example.arweld.ui.supervisor.WorkItemDetailRoute

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navigationLogger: NavigationLoggerViewModel = hiltViewModel()

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { navigationLogger.logNavigation(it) }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_AUTH_GRAPH,
        modifier = modifier
    ) {
        navigation(route = ROUTE_AUTH_GRAPH, startDestination = ROUTE_SPLASH) {
            composable(ROUTE_SPLASH) {
                SplashScreen(navController = navController)
            }
            composable(ROUTE_LOGIN) {
                LoginRoute(
                    onLoginSuccess = {
                        navController.navigate(ROUTE_MAIN_GRAPH) {
                            popUpTo(ROUTE_AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }
        navigation(route = ROUTE_MAIN_GRAPH, startDestination = ROUTE_HOME) {
            composable(ROUTE_HOME) {
                HomeRoute(navController = navController)
            }
            composable(ROUTE_ASSEMBLER_QUEUE) {
                AssemblerQueueRoute(navController = navController)
            }
            composable(ROUTE_QC_QUEUE) {
                QcQueueRoute(navController = navController)
            }
            composable(
                route = "$ROUTE_QC_CHECKLIST?workItemId={workItemId}&code={code}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getString("workItemId")
                val code = backStackEntry.arguments?.getString("code")
                QcChecklistRoute(
                    navController = navController,
                    workItemId = workItemId,
                    code = code,
                )
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
            composable(
                route = "$ROUTE_AR_VIEW?workItemId={workItemId}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getString("workItemId")
                ARViewRoute(
                    navController = navController,
                    workItemId = workItemId,
                )
            }
            composable(
                route = "$ROUTE_QC_START?workItemId={workItemId}&code={code}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getString("workItemId")
                val code = backStackEntry.arguments?.getString("code")
                QcStartRoute(
                    navController = navController,
                    workItemId = workItemId,
                    code = code,
                )
            }
            composable(
                route = "$ROUTE_QC_PASS_CONFIRM?workItemId={workItemId}&code={code}&checklist={checklist}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("checklist") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getString("workItemId")
                val code = backStackEntry.arguments?.getString("code")
                val checklistJson = backStackEntry.arguments?.getString("checklist")
                QcPassConfirmRoute(
                    navController = navController,
                    workItemId = workItemId,
                    code = code,
                    checklistJson = checklistJson,
                )
            }
            composable(
                route = "$ROUTE_QC_FAIL_REASON?workItemId={workItemId}&code={code}&checklist={checklist}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("checklist") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workItemId = backStackEntry.arguments?.getString("workItemId")
                val code = backStackEntry.arguments?.getString("code")
                val checklistJson = backStackEntry.arguments?.getString("checklist")
                QcFailReasonRoute(
                    navController = navController,
                    workItemId = workItemId,
                    code = code,
                    checklistJson = checklistJson,
                )
            }
            composable(ROUTE_SUPERVISOR_DASHBOARD) {
                SupervisorDashboardRoute(
                    onWorkItemClick = { workItemId ->
                        navController.navigate(workItemDetailRoute(workItemId))
                    }
                )
            }
            composable(
                route = "$ROUTE_WORK_ITEM_DETAIL/{workItemId}",
                arguments = listOf(
                    navArgument("workItemId") {
                        type = NavType.StringType
                    }
                )
            ) {
                WorkItemDetailRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
