package com.example.arweld.core.data.drawing2d

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import com.example.arweld.core.drawing2d.editor.v1.canonicalize
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File

class Drawing2DRepositoryImplTest {
    private val repository = Drawing2DRepositoryImpl()

    @Test
    fun `save then load returns canonical drawing`() = runBlocking {
        val projectDir = createTempDir(prefix = "drawing2d")
        val workspace = Drawing2DWorkspace(projectDir)
        val drawing = sampleDrawing()

        repository.save(workspace, drawing)
        val loaded = repository.load(workspace)

        assertThat(loaded).isEqualTo(drawing.canonicalize())
    }

    @Test
    fun `save twice produces identical output`() = runBlocking {
        val projectDir = createTempDir(prefix = "drawing2d")
        val workspace = Drawing2DWorkspace(projectDir)
        val resolver = Project2D3DWorkspaceResolver(workspace.layout)

        val firstDrawing = sampleDrawing()
        repository.save(workspace, firstDrawing)
        val firstJson = resolver.drawing2dJsonFile(projectDir).readText()

        val secondDrawing = Drawing2D(
            nodes = firstDrawing.nodes.reversed(),
            members = firstDrawing.members.reversed()
        )
        repository.save(workspace, secondDrawing)
        val secondJson = resolver.drawing2dJsonFile(projectDir).readText()

        assertThat(firstJson).isEqualTo(secondJson)
    }

    @Test
    fun `load returns empty drawing when missing`() = runBlocking {
        val projectDir = createTempDir(prefix = "drawing2d")
        val workspace = Drawing2DWorkspace(projectDir)

        val loaded = repository.load(workspace)

        assertThat(loaded).isEqualTo(Drawing2D(nodes = emptyList(), members = emptyList()))
        assertThat(File(projectDir, "workspace").exists()).isFalse()
    }

    private fun sampleDrawing(): Drawing2D {
        val nodes = listOf(
            Node2D(id = "N000002", x = 20.0, y = 30.0),
            Node2D(id = "N000001", x = 10.0, y = 10.0)
        )
        val members = listOf(
            Member2D(id = "M000002", aNodeId = "N000002", bNodeId = "N000001"),
            Member2D(id = "M000001", aNodeId = "N000001", bNodeId = "N000002")
        )
        return Drawing2D(
            nodes = nodes,
            members = members,
            scale = ScaleInfo(
                pointA = Point2D(10.0, 10.0),
                pointB = Point2D(20.0, 10.0),
                realLengthMm = 500.0,
            )
        )
    }
}
