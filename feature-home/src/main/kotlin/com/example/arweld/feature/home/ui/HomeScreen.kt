package com.example.arweld.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.home.viewmodel.HomeUiState
import com.example.arweld.feature.home.viewmodel.HomeViewModel

/**
 * Home screen with role-based navigation.
 * Shows different options based on the current user's role.
 * Demonstrates Hilt dependency injection with ViewModel.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToWorkSummary: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToAssemblerQueue: () -> Unit = {},
    onNavigateToQcQueue: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToSupervisorDashboard: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    userName = state.currentUser?.displayName ?: "Guest",
                    userRole = state.currentUser?.role?.name ?: "N/A",
                    workItemCount = state.workItemCount,
                    onNavigateToWorkSummary = onNavigateToWorkSummary,
                    onNavigateToTimeline = onNavigateToTimeline,
                    onNavigateToAssemblerQueue = onNavigateToAssemblerQueue,
                    onNavigateToQcQueue = onNavigateToQcQueue,
                    onNavigateToScan = onNavigateToScan,
                    onNavigateToSupervisorDashboard = onNavigateToSupervisorDashboard
                )
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    userName: String,
    userRole: String,
    workItemCount: Int,
    onNavigateToWorkSummary: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToAssemblerQueue: () -> Unit,
    onNavigateToQcQueue: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToSupervisorDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ARWeld MVP",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Role: $userRole",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hilt DI Configured",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Work Items in Database: $workItemCount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Navigation Demo",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToWorkSummary,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Work Item Summary")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToTimeline,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Timeline")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToAssemblerQueue,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Assembler Queue")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToQcQueue,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("QC Queue")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToScan,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Scan Code")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToSupervisorDashboard,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Supervisor Dashboard")
        }
    }
}
