package com.example.arweld.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.core.domain.model.User

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onUserSelected = { userId -> viewModel.login(userId, onLoginSuccess) },
        onReload = { viewModel.reloadUsers() }
    )
}

@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onUserSelected: (String) -> Unit,
    onReload: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select a user", style = MaterialTheme.typography.headlineMedium)

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
                Button(onClick = onReload) {
                    Text(text = "Retry")
                }
            }

            uiState.users.isEmpty() -> {
                Text(text = "No users available")
            }

            else -> {
                uiState.users.forEach { user ->
                    UserButton(user = user, onClick = { onUserSelected(user.id) })
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Users are seeded into Room on first launch for deterministic logins.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UserButton(user: User, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = user.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Role: ${user.role.name}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
