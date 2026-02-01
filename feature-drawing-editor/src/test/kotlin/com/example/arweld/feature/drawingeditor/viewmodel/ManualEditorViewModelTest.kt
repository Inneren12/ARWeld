package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.domain.structural.ProfileCatalogQuery
import com.example.arweld.core.domain.structural.ProfileItem
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.structural.profiles.ProfileType
import com.example.arweld.feature.drawingeditor.diagnostics.EditorDiagnosticsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManualEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val recordedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    private val defaultProfileCatalogQuery = fakeProfileCatalogQuery()
    private val fakeRecorder = object : DiagnosticsRecorder {
        override fun recordEvent(name: String, attributes: Map<String, String>) {
            recordedEvents.add(name to attributes)
        }
        override fun updateArTelemetry(snapshot: ArTelemetrySnapshot) {}
        override fun updateDeviceHealth(snapshot: DeviceHealthSnapshot) {}
        override fun snapshot(maxEvents: Int): DiagnosticsSnapshot {
            return DiagnosticsSnapshot(emptyList(), null, null)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        recordedEvents.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tool selection updates state and drawing loads`() = runTest {
        val drawing = Drawing2D(
            nodes = listOf(
                Node2D(id = "N1", x = 0.0, y = 0.0),
                Node2D(id = "N2", x = 1.0, y = 1.0),
            ),
            members = listOf(
                Member2D(id = "M1", aNodeId = "N1", bNodeId = "N2")
            ),
        )
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = drawing

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) = Unit
        }
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        val viewModel = ManualEditorViewModel(repository, logger, defaultProfileCatalogQuery)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.drawing.nodes.size)
        assertEquals(1, viewModel.uiState.value.drawing.members.size)

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.MEMBER))
        assertEquals(EditorTool.MEMBER, viewModel.uiState.value.tool)
    }

    @Test
    fun `editor opened event is logged on init`() = runTest {
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) = Unit
        }
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        ManualEditorViewModel(repository, logger, defaultProfileCatalogQuery)

        advanceUntilIdle()

        val openedEvent = recordedEvents.find { it.first == "editor_opened" }
        assertEquals("editor_opened", openedEvent?.first)
        assertEquals("drawing_editor", openedEvent?.second?.get("feature"))
        assertEquals("lifecycle", openedEvent?.second?.get("phase"))
    }

    @Test
    fun `tool changed event is logged with tool names`() = runTest {
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) = Unit
        }
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        val viewModel = ManualEditorViewModel(repository, logger, defaultProfileCatalogQuery)

        advanceUntilIdle()
        recordedEvents.clear()

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.NODE))

        val toolChangedEvent = recordedEvents.find { it.first == "editor_tool_changed" }
        assertEquals("editor_tool_changed", toolChangedEvent?.first)
        assertEquals("NODE", toolChangedEvent?.second?.get("tool"))
        assertEquals("SELECT", toolChangedEvent?.second?.get("previousTool"))
    }

    @Test
    fun `node tap persists drawing when node is added`() = runTest {
        var saveCount = 0
        var savedDrawing: Drawing2D? = null
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) {
                saveCount += 1
                savedDrawing = drawing
            }
        }
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        val viewModel = ManualEditorViewModel(repository, logger, defaultProfileCatalogQuery)

        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.NODE))
        viewModel.onIntent(EditorIntent.NodeTap(Point2D(x = 4.0, y = 5.0), tolerancePx = 16f))

        advanceUntilIdle()

        assertEquals(1, saveCount)
        assertEquals(1, savedDrawing?.nodes?.size)
        assertEquals("N000001", savedDrawing?.nodes?.first()?.id)
    }

    @Test
    fun `node drag persists only on drag end`() = runTest {
        var saveCount = 0
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = Drawing2D(
                nodes = listOf(Node2D(id = "N1", x = 1.0, y = 2.0)),
                members = emptyList(),
            )

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) {
                saveCount += 1
            }
        }
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        val viewModel = ManualEditorViewModel(repository, logger, defaultProfileCatalogQuery)

        advanceUntilIdle()
        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.NODE))
        viewModel.onIntent(EditorIntent.NodeDragStart("N1", Point2D(x = 2.0, y = 3.0)))
        viewModel.onIntent(EditorIntent.NodeDragMove(Point2D(x = 4.0, y = 6.0)))

        advanceUntilIdle()
        assertEquals(0, saveCount)

        viewModel.onIntent(EditorIntent.NodeDragEnd(Point2D(x = 4.0, y = 6.0)))
        advanceUntilIdle()
        assertEquals(1, saveCount)
    }

    @Test
    fun `profile search results propagate to picker state`() = runTest {
        val drawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0), Node2D(id = "N2", x = 1.0, y = 1.0)),
            members = listOf(Member2D(id = "M1", aNodeId = "N1", bNodeId = "N2")),
        )
        val repository = object : Drawing2DRepository {
            override suspend fun getCurrentDrawing(): Drawing2D = drawing

            override suspend fun saveCurrentDrawing(drawing: Drawing2D) = Unit
        }
        val searchResults = listOf(
            ProfileItem(profileRef = "W310x39", displayName = "W310x39", type = ProfileType.W),
            ProfileItem(profileRef = "HSS 203x203x6.4", displayName = "HSS 203x203x6.4", type = ProfileType.HSS),
        )
        val logger = EditorDiagnosticsLogger(fakeRecorder)
        val viewModel = ManualEditorViewModel(
            repository,
            logger,
            fakeProfileCatalogQuery(searchResults = searchResults),
        )

        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ProfilePickerOpen("M1"))
        advanceUntilIdle()

        val pickerState = viewModel.uiState.value.profilePicker
        assertEquals(true, pickerState.isOpen)
        assertEquals(listOf("W310x39", "HSS 203x203x6.4"), pickerState.results.map { it.profileRef })
    }

    private fun fakeProfileCatalogQuery(
        searchResults: List<ProfileItem> = emptyList(),
        lookupResult: ProfileItem? = null,
    ): ProfileCatalogQuery {
        return object : ProfileCatalogQuery {
            override suspend fun listAll(): List<ProfileItem> = searchResults

            override suspend fun search(query: String, limit: Int): List<ProfileItem> = searchResults.take(limit)

            override suspend fun lookup(profileRef: String): ProfileItem? = lookupResult
        }
    }
}
