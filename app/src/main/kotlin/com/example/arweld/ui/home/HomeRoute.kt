package com.example.arweld.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.feature.home.ui.HomeScreen
import com.example.arweld.navigation.ROUTE_LOGIN
import com.example.arweld.navigation.ROUTE_TIMELINE
import com.example.arweld.navigation.ROUTE_WORK_ITEM_SUMMARY

@Composable
fun HomeRoute(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val userState = viewModel.user.collectAsState()
    val user = userState.value

    when (user) {
        null -> MissingUserFallback(onBackToLogin = {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
            }
        })
        else -> HomeScreen(
            user = user,
            onOpenWorkSummary = { navController.navigate(ROUTE_WORK_ITEM_SUMMARY) },
            onOpenTimeline = { navController.navigate(ROUTE_TIMELINE) }
        )
    }
}

@Composable
private fun MissingUserFallback(onBackToLogin: () -> Unit) {
    LaunchedEffect(Unit) { onBackToLogin() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Session expired. Returning to login...", modifier = Modifier)
    }
}
