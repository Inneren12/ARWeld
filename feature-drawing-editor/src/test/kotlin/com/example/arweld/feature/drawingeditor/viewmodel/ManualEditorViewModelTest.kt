package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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
        val viewModel = ManualEditorViewModel(repository)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.drawing.nodes.size)
        assertEquals(1, viewModel.uiState.value.drawing.members.size)

        viewModel.onIntent(EditorIntent.ToolChanged(EditorTool.MEMBER))
        assertEquals(EditorTool.MEMBER, viewModel.uiState.value.tool)
    }
}
