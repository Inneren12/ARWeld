package com.example.arweld.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.ui.auth.LoginRoute
import com.example.arweld.ui.auth.SplashScreen
import com.example.arweld.ui.home.HomeRoute
import com.example.arweld.ui.ar.ARViewRoute
import com.example.arweld.ui.drawingimport.DrawingImportRoute
import com.example.arweld.ui.drawingeditor.ManualEditorRoute
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
import com.example.arweld.ui.supervisor.SupervisorWorkListRoute
import com.example.arweld.ui.supervisor.WorkItemDetailRoute
import com.example.arweld.ui.supervisor.ExportCenterRoute
import com.example.arweld.ui.supervisor.ReportsRoute
import com.example.arweld.ui.supervisor.OfflineQueueRoute
import javax.inject.Inject

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    authRepository: AuthRepository = hiltViewModel<AppNavigationViewModel>().authRepository
) {
    val resolvedNavController = navController ?: rememberNavController()
    val navigationLogger: NavigationLoggerViewModel = hiltViewModel()
    val currentUser by authRepository.currentUserFlow.collectAsState()

    val startDestination = if (currentUser != null) ROUTE_MAIN_GRAPH else ROUTE_AUTH_GRAPH

    DisposableEffect(resolvedNavController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { navigationLogger.logNavigation(it) }
        }
        resolvedNavController.addOnDestinationChangedListener(listener)
        onDispose { resolvedNavController.removeOnDestinationChangedListener(listener) }
    }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            val currentRoute = resolvedNavController.currentBackStackEntry?.destination?.route
            if (currentRoute != ROUTE_LOGIN && currentRoute != ROUTE_SPLASH && currentRoute != ROUTE_AUTH_GRAPH) {
                resolvedNavController.navigate(ROUTE_AUTH_GRAPH) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = resolvedNavController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        navigation(route = ROUTE_AUTH_GRAPH, startDestination = ROUTE_SPLASH) {
            composable(ROUTE_SPLASH) {
                SplashScreen(navController = resolvedNavController)
            }
            composable(ROUTE_LOGIN) {
                LoginRoute(
                    onLoginSuccess = {
                        resolvedNavController.navigate(ROUTE_MAIN_GRAPH) {
                            popUpTo(ROUTE_AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }
        navigation(route = ROUTE_MAIN_GRAPH, startDestination = ROUTE_HOME) {
            composable(ROUTE_HOME) {
                HomeRoute(navController = resolvedNavController)
            }
            composable(ROUTE_ASSEMBLER_QUEUE) {
                AssemblerQueueRoute(navController = resolvedNavController)
            }
            composable(ROUTE_QC_QUEUE) {
                QcQueueRoute(navController = resolvedNavController)
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
                    navController = resolvedNavController,
                    workItemId = workItemId,
                    code = code,
                )
            }
            composable(ROUTE_SCAN_CODE) {
                ScanCodeRoute(navController = resolvedNavController)
            }
            composable(ROUTE_DRAWING_IMPORT) {
                DrawingImportRoute()
            }
            composable(ROUTE_MANUAL_EDITOR) {
                ManualEditorRoute()
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
                    navController = resolvedNavController,
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
                    navController = resolvedNavController,
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
                    navController = resolvedNavController,
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
                    navController = resolvedNavController,
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
                    navController = resolvedNavController,
                    workItemId = workItemId,
                    code = code,
                    checklistJson = checklistJson,
                )
            }
            composable(ROUTE_SUPERVISOR_DASHBOARD) {
                SupervisorDashboardRoute(
                    onWorkItemClick = { workItemId ->
                        resolvedNavController.navigate(workItemDetailRoute(workItemId))
                    },
                    onKpiClick = { status ->
                        resolvedNavController.navigate(supervisorWorkListRoute(status))
                    },
                    onWorkListClick = {
                        resolvedNavController.navigate(supervisorWorkListRoute())
                    }
                )
            }
            composable(
                route = "$ROUTE_SUPERVISOR_WORK_LIST?status={status}",
                arguments = listOf(
                    navArgument("status") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val statusArg = backStackEntry.arguments?.getString("status")
                val initialStatus = statusArg?.let { value ->
                    runCatching { WorkStatus.valueOf(value) }.getOrNull()
                }
                SupervisorWorkListRoute(
                    onNavigateBack = { resolvedNavController.popBackStack() },
                    onWorkItemClick = { workItemId ->
                        resolvedNavController.navigate(workItemDetailRoute(workItemId))
                    },
                    initialStatus = initialStatus,
                )
            }
            composable(ROUTE_EXPORT_CENTER) {
                ExportCenterRoute()
            }
            composable(ROUTE_REPORTS) {
                ReportsRoute()
            }
            composable(ROUTE_OFFLINE_QUEUE) {
                OfflineQueueRoute()
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
                        resolvedNavController.popBackStack()
                    }
                )
            }
        }
    }
}
