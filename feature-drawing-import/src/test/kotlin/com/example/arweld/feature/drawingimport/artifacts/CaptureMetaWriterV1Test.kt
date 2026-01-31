package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureCornersV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetricsV1
import com.example.arweld.core.drawing2d.artifacts.v1.CornerQuadV1
import com.example.arweld.core.drawing2d.artifacts.v1.RectifiedCaptureV1
import com.example.arweld.core.drawing2d.crypto.Sha256V1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.TemporaryFolder
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CaptureMetaWriterV1Test {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `writer persists capture meta with valid sha`() {
        val projectDir = tempFolder.newFolder("project")
        val store = FileArtifactStoreV1(projectDir)
        val session = DrawingImportSession(
            projectId = "project-123",
            projectDir = projectDir,
            artifacts = emptyList(),
        )
        val captureMeta = CaptureMetaV1(
            corners = CaptureCornersV1(
                ordered = CornerQuadV1(
                    topLeft = PointV1(0.0, 0.0),
                    topRight = PointV1(100.0, 0.0),
                    bottomRight = PointV1(100.0, 200.0),
                    bottomLeft = PointV1(0.0, 200.0),
                ),
            ),
            rectified = RectifiedCaptureV1(widthPx = 1024, heightPx = 768),
            metrics = CaptureMetricsV1(blurVariance = 42.0),
        )

        val updated = CaptureMetaWriterV1().write(
            captureMeta = captureMeta,
            projectStore = store,
            session = session,
            rewriteManifest = true,
        )

        val captureEntry = updated.artifacts.firstOrNull { it.relPath == ProjectLayoutV1.CAPTURE_META_JSON }
        assertNotNull(captureEntry)
        captureEntry?.let { entry ->
            assertEquals(ProjectLayoutV1.CAPTURE_META_JSON, entry.relPath)
            assertTrue(entry.sha256.matches(Regex("[a-f0-9]{64}")))
        }

        val captureFile = File(projectDir, ProjectLayoutV1.CAPTURE_META_JSON)
        assertTrue(captureFile.exists())
        val decoded = Drawing2DJson.decodeFromString<CaptureMetaV1>(captureFile.readText())
        assertEquals(captureMeta, decoded)

        val expectedSha = Sha256V1.hashBytes(captureFile.readBytes())
        assertEquals(expectedSha, captureEntry?.sha256)
    }
}
