package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.supervisor.ui.ExportScreen
import com.example.arweld.feature.supervisor.viewmodel.ExportViewModel
import java.io.File

@Composable
fun ExportCenterRoute(
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val outputRoot = File(context.getExternalFilesDir(null), "exports")

    ExportScreen(
        state = uiState,
        onSelectPeriod = { viewModel.selectPeriod(it) },
        onToggleCsv = { viewModel.toggleCsv(it) },
        onToggleZip = { viewModel.toggleZip(it) },
        onToggleManifest = { viewModel.toggleManifest(it) },
        onExport = { viewModel.export(outputRoot) },
    )
}
