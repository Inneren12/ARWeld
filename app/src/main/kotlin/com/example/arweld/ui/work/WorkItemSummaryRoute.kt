package com.example.arweld.ui.work

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.arweld.feature.work.ui.WorkItemSummaryScreen

@Composable
@Suppress("UnusedParameter")
fun WorkItemSummaryRoute(
    navController: NavHostController,
    workItemId: String?,
) {
    WorkItemSummaryScreen(workItemId = workItemId)
}
