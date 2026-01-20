package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.supervisor.ui.ReportsScreen
import com.example.arweld.feature.supervisor.viewmodel.ReportsViewModel

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ReportsScreen(
        state = uiState,
        onSelectPeriodType = { viewModel.selectPeriodType(it) },
        onSelectShift = { viewModel.selectShift(it) },
        onSelectDate = { viewModel.selectDate(it) },
        onSelectZone = { viewModel.selectZone(it) },
    )
}
