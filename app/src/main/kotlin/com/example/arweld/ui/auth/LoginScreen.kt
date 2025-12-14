package com.example.arweld.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.arweld.core.domain.model.Role
import com.example.arweld.navigation.Routes
import kotlinx.coroutines.launch

/**
 * Login screen - allows user to select their role.
 * For MVP, this is a mock authentication screen with no real auth.
 * Users select their role (Assembler, QC, Supervisor, Director) to proceed.
 */
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    fun onLoginSelected(role: Role) {
        coroutineScope.launch {
            viewModel.onRoleSelected(role)
            navController.navigate(Routes.ROUTE_HOME) {
                popUpTo(Routes.ROUTE_LOGIN) { inclusive = true }
            }
        }
    }

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
                onClick = { onLoginSelected(Role.ASSEMBLER) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as Assembler")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign in as QC
            Button(
                onClick = { onLoginSelected(Role.QC) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as QC")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign in as Supervisor
            Button(
                onClick = { onLoginSelected(Role.SUPERVISOR) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as Supervisor")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLoginSelected(Role.DIRECTOR) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Sign in as Director")
            }
        }
    }
}
