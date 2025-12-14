package com.example.arweld.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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

    val roleButtons = listOf(
        RoleButton("Sign in as Assembler", Role.ASSEMBLER),
        RoleButton("Sign in as QC", Role.QC),
        RoleButton("Sign in as Supervisor", Role.SUPERVISOR),
        RoleButton("Sign in as Director", Role.DIRECTOR)
    )

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
            Text(
                text = "Select your role to continue:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            roleButtons.forEachIndexed { index, roleButton ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.onRoleSelected(roleButton.role)
                            navController.navigate(Routes.ROUTE_HOME) {
                                popUpTo(Routes.ROUTE_LOGIN) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .sizeIn(minHeight = 56.dp)
                        .padding(top = if (index == 0) 32.dp else 16.dp)
                ) {
                    Text(roleButton.label)
                }
            }
        }
    }
}

private data class RoleButton(
    val label: String,
    val role: Role
)
