package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.feature.drawingeditor.diagnostics.EditorDiagnosticsLogger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManualEditorScaleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `apply scale updates drawing and persists`() = runTest {
        val repository = FakeDrawing2DRepository()
        val viewModel = ManualEditorViewModel(repository, EditorDiagnosticsLogger(FakeDiagnosticsRecorder()))
        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.SCALE))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(0.0, 0.0)))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(0.0, 10.0)))
        viewModel.onIntent(EditorIntent.ScaleLengthChanged("100"))
        viewModel.onIntent(EditorIntent.ScaleApplyRequested)

        advanceUntilIdle()

        val savedDrawing = repository.savedDrawing
        assertNotNull(savedDrawing)
        assertNotNull(savedDrawing.scale)
        assertEquals(100.0, savedDrawing.scale?.realLengthMm)
        assertEquals(Point2D(0.0, 0.0), savedDrawing.scale?.pointA)
        assertEquals(Point2D(0.0, 10.0), savedDrawing.scale?.pointB)
    }

    @Test
    fun `scale apply fails on zero distance`() = runTest {
        val repository = FakeDrawing2DRepository()
        val viewModel = ManualEditorViewModel(repository, EditorDiagnosticsLogger(FakeDiagnosticsRecorder()))
        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.SCALE))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(5.0, 5.0)))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(5.0, 5.0)))
        viewModel.onIntent(EditorIntent.ScaleLengthChanged("120"))
        viewModel.onIntent(EditorIntent.ScaleApplyRequested)

        advanceUntilIdle()

        assertNull(repository.savedDrawing)
        assertEquals(
            "Points are too close to compute scale.",
            viewModel.uiState.value.scaleDraft.applyError,
        )
    }

    @Test
    fun `scale apply fails on non-positive length`() = runTest {
        val repository = FakeDrawing2DRepository()
        val viewModel = ManualEditorViewModel(repository, EditorDiagnosticsLogger(FakeDiagnosticsRecorder()))
        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.SCALE))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(0.0, 0.0)))
        viewModel.onIntent(EditorIntent.ScalePointSelected(Point2D(0.0, 5.0)))
        viewModel.onIntent(EditorIntent.ScaleLengthChanged("0"))
        viewModel.onIntent(EditorIntent.ScaleApplyRequested)

        advanceUntilIdle()

        assertNull(repository.savedDrawing)
        assertEquals(
            "Length must be > 0 mm.",
            viewModel.uiState.value.scaleDraft.applyError,
        )
    }

    private class FakeDrawing2DRepository : Drawing2DRepository {
        private var currentDrawing: Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())
        var savedDrawing: Drawing2D? = null
            private set

        override suspend fun getCurrentDrawing(): Drawing2D = currentDrawing

        override suspend fun saveCurrentDrawing(drawing: Drawing2D) {
            currentDrawing = drawing
            savedDrawing = drawing
        }
    }

    private class FakeDiagnosticsRecorder : DiagnosticsRecorder {
        override fun recordEvent(name: String, attributes: Map<String, String>) = Unit

        override fun updateArTelemetry(snapshot: ArTelemetrySnapshot) = Unit

        override fun updateDeviceHealth(snapshot: DeviceHealthSnapshot) = Unit

        override fun snapshot(maxEvents: Int): DiagnosticsSnapshot = DiagnosticsSnapshot(
            arTelemetry = null,
            deviceHealth = null,
            recentEvents = emptyList(),
        )
    }
}
