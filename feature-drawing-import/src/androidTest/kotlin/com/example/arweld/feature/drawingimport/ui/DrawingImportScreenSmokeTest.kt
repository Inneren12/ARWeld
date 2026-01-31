package com.example.arweld.feature.drawingimport.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot
import com.example.arweld.feature.drawingimport.camera.FakeCameraFacade
import java.io.File
import org.junit.Rule
import org.junit.Test

class DrawingImportScreenSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun drawingImportScreen_rendersRootContainer() {
        composeRule.setContent {
            DrawingImportScreen(
                diagnosticsRecorder = NoopDiagnosticsRecorder(),
                cameraFacade = FakeCameraFacade(),
                permissionStateOverride = CameraPermissionState.Ready,
            )
        }

        composeRule.onNodeWithTag(UiTags.drawingImportRoot).assertExists()
    }

    @Test
    fun drawingImportScreen_permissionNeeded_showsPermissionCta() {
        composeRule.setContent {
            DrawingImportScreen(
                diagnosticsRecorder = NoopDiagnosticsRecorder(),
                cameraFacade = FakeCameraFacade(),
                permissionStateOverride = CameraPermissionState.NeedsPermission,
            )
        }

        composeRule.onNodeWithTag(UiTags.cameraPermissionCta).assertExists()
    }

    @Test
    fun drawingImportScreen_readyState_showsCaptureButton() {
        composeRule.setContent {
            DrawingImportScreen(
                diagnosticsRecorder = NoopDiagnosticsRecorder(),
                cameraFacade = FakeCameraFacade(),
                permissionStateOverride = CameraPermissionState.Ready,
                initialScreenState = DrawingImportUiState.Ready,
            )
        }

        composeRule.onNodeWithTag(UiTags.captureButton).assertExists()
    }

    @Test
    fun drawingImportScreen_savedState_showsResultAndProcessButton() {
        val context = composeRule.activity
        val projectDir = File(context.cacheDir, "drawing-import-smoke").apply { mkdirs() }
        val session = DrawingImportSession(
            projectId = "test-project",
            artifactsRoot = context.cacheDir,
            projectDir = projectDir,
            artifacts = emptyList(),
        )

        composeRule.setContent {
            DrawingImportScreen(
                diagnosticsRecorder = NoopDiagnosticsRecorder(),
                cameraFacade = FakeCameraFacade(),
                permissionStateOverride = CameraPermissionState.Ready,
                initialScreenState = DrawingImportUiState.Saved(session),
            )
        }

        composeRule.onNodeWithTag(UiTags.resultRoot).assertExists()
        composeRule.onNodeWithTag(UiTags.processButton).assertExists()
    }
}

private class NoopDiagnosticsRecorder : DiagnosticsRecorder {
    override fun recordEvent(name: String, attributes: Map<String, String>) = Unit

    override fun updateArTelemetry(snapshot: ArTelemetrySnapshot) = Unit

    override fun updateDeviceHealth(snapshot: DeviceHealthSnapshot) = Unit

    override fun snapshot(maxEvents: Int): DiagnosticsSnapshot = DiagnosticsSnapshot(
        arTelemetry = null,
        deviceHealth = null,
        recentEvents = emptyList(),
    )
}
