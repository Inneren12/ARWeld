package com.example.arweld.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.domain.model.Role
import com.example.arweld.domain.model.User

@Composable
fun HomeScreen(
    user: User,
    onOpenWorkSummary: () -> Unit,
    onOpenTimeline: () -> Unit,
) {
    val tiles = tilesForRole(user.role, onOpenWorkSummary, onOpenTimeline)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Greeting(user)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(tiles) { tile ->
                HomeTile(tile)
            }
        }
    }
}

private data class HomeTileModel(
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

@Composable
private fun Greeting(user: User) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Welcome, ${user.displayName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Role: ${user.role.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeTile(model: HomeTileModel) {
    Card(
        onClick = model.onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun tilesForRole(
    role: Role,
    onOpenWorkSummary: () -> Unit,
    onOpenTimeline: () -> Unit,
): List<HomeTileModel> = when (role) {
    Role.ASSEMBLER -> listOf(
        HomeTileModel(
            title = "My Work Queue",
            description = "View and manage your assigned work items.",
            onClick = onOpenWorkSummary
        ),
        HomeTileModel(
            title = "Timeline",
            description = "See event history for your work items.",
            onClick = onOpenTimeline
        )
    )

    Role.QC -> listOf(
        HomeTileModel(
            title = "QC Queue",
            description = "Inspect items waiting for quality review.",
            onClick = onOpenWorkSummary
        ),
        HomeTileModel(
            title = "Timeline",
            description = "Review inspection history and evidence.",
            onClick = onOpenTimeline
        )
    )

    Role.SUPERVISOR, Role.DIRECTOR -> listOf(
        HomeTileModel(
            title = "Shop overview",
            description = "Monitor shop status and assignments.",
            onClick = onOpenWorkSummary
        ),
        HomeTileModel(
            title = "Timeline",
            description = "Browse timelines across the shop floor.",
            onClick = onOpenTimeline
        )
    )
}
