package com.example.arweld.feature.work.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WorkItemSummaryScreen(
    workItemId: String?,
    modifier: Modifier = Modifier,
) {
    Text(text = "WorkItemSummary stub: id=$workItemId", modifier = modifier)
}
