package com.example.arweld.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.arweld.navigation.Routes

/**
 * Splash screen - entry point for the app.
 * Immediately redirects to Login screen.
 * In future versions, this could check for existing session or perform initialization.
 */
@Composable
fun SplashScreen(navController: NavHostController) {
    // Navigate to Login screen immediately on first composition
    LaunchedEffect(Unit) {
        navController.navigate(Routes.ROUTE_LOGIN) {
            popUpTo(Routes.ROUTE_SPLASH) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ar weld",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AR-Assisted QA System",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
