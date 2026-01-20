package com.example.arweld.ui.supervisor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    val reportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        viewModel.exportReportJson(uri)
    }
    val reportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        viewModel.exportReportCsv(uri)
    }
    val manifestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        viewModel.exportManifest(uri)
    }

    ExportScreen(
        state = uiState,
        onSelectPeriod = { viewModel.selectPeriod(it) },
        onToggleCsv = { viewModel.toggleCsv(it) },
        onToggleZip = { viewModel.toggleZip(it) },
        onToggleManifest = { viewModel.toggleManifest(it) },
        onExport = { viewModel.export(outputRoot) },
        onExportDiagnostics = { viewModel.exportDiagnostics(outputRoot) },
        onExportEvidenceZip = { viewModel.exportEvidenceZip(outputRoot) },
        onExportReportJson = { reportJsonLauncher.launch(viewModel.suggestedReportFileName()) },
        onExportReportCsv = { reportCsvLauncher.launch(viewModel.suggestedSummaryCsvFileName()) },
        onExportManifest = { manifestLauncher.launch(viewModel.suggestedManifestFileName()) },
    )
}
