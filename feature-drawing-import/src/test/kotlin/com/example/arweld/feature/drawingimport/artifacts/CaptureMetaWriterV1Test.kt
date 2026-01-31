package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.core.drawing2d.artifacts.v1.ExposureMetricsV1
import com.example.arweld.core.drawing2d.artifacts.v1.ImageInfoV1
import com.example.arweld.core.drawing2d.artifacts.v1.MetricsBlockV1
import com.example.arweld.core.drawing2d.artifacts.v1.QualityGateBlockV1
import com.example.arweld.core.drawing2d.artifacts.v1.SkewMetricsV1
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
            artifactsRoot = projectDir.parentFile,
            projectDir = projectDir,
            artifacts = emptyList(),
        )
        val captureMeta = CaptureMetaV1(
            projectId = "project-123",
            raw = ImageInfoV1(widthPx = 4032, heightPx = 3024, rotationAppliedDeg = 0),
            upright = ImageInfoV1(widthPx = 3024, heightPx = 4032, rotationAppliedDeg = 90),
            downscaleFactor = 2.0,
            cornersDownscaledPx = listOf(
                PointV1(0.0, 0.0),
                PointV1(100.0, 0.0),
                PointV1(100.0, 200.0),
                PointV1(0.0, 200.0),
            ),
            cornersUprightPx = listOf(
                PointV1(0.0, 0.0),
                PointV1(200.0, 0.0),
                PointV1(200.0, 400.0),
                PointV1(0.0, 400.0),
            ),
            homographyH = listOf(
                1.0, 0.0, 2.0,
                0.0, 1.0, 3.0,
                0.0, 0.0, 1.0,
            ),
            rectified = ImageInfoV1(widthPx = 1024, heightPx = 768, rotationAppliedDeg = 0),
            metrics = MetricsBlockV1(
                blurVar = 42.0,
                exposure = ExposureMetricsV1(meanY = 128.0, clipLowPct = 1.0, clipHighPct = 0.5),
                skew = SkewMetricsV1(
                    angleMaxAbsDeg = 3.0,
                    angleMeanAbsDeg = 1.5,
                    keystoneWidthRatio = 1.1,
                    keystoneHeightRatio = 1.05,
                    pageFillRatio = 0.8,
                ),
            ),
            quality = QualityGateBlockV1(decision = "PASS", reasons = emptyList()),
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
