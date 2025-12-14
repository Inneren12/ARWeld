package com.example.arweld.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.arweld.navigation.Routes

/**
 * Login screen - allows user to select their role.
 * For MVP, this is a mock authentication screen with no real auth.
 * Users simply select their role (Assembler, QC, Supervisor) to proceed.
 */
@Composable
fun LoginScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Select your role to continue:",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Sign in as Assembler
            Button(
                onClick = {
                    navController.navigate(Routes.ROUTE_HOME) {
                        popUpTo(Routes.ROUTE_LOGIN) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as Assembler")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign in as QC
            Button(
                onClick = {
                    navController.navigate(Routes.ROUTE_HOME) {
                        popUpTo(Routes.ROUTE_LOGIN) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as QC")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign in as Supervisor
            Button(
                onClick = {
                    navController.navigate(Routes.ROUTE_HOME) {
                        popUpTo(Routes.ROUTE_LOGIN) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as Supervisor")
            }
        }
    }
}
