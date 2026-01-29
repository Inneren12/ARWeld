package com.example.arweld.feature.arview.arcore

import android.content.Context
import com.example.arweld.core.domain.diagnostics.DeviceHealthProvider
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.feature.arview.alignment.AlignmentEventLogger

interface ArViewControllerFactory {
    fun create(
        context: Context,
        alignmentEventLogger: AlignmentEventLogger,
        workItemId: String?,
        diagnosticsRecorder: DiagnosticsRecorder?,
        deviceHealthProvider: DeviceHealthProvider?,
    ): ARViewController
}
