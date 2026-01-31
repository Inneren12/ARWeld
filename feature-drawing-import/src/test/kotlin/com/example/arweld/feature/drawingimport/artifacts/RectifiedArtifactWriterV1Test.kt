package com.example.arweld.feature.drawingimport.artifacts

import android.graphics.Bitmap
import android.graphics.Color
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.crypto.PixelFormatV1
import com.example.arweld.core.drawing2d.crypto.PixelSha256V1
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
class RectifiedArtifactWriterV1Test {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `pixel hash matches expected for 2x2 bitmap`() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        bitmap.setPixel(0, 0, Color.RED)
        bitmap.setPixel(1, 0, Color.GREEN)
        bitmap.setPixel(0, 1, Color.BLUE)
        bitmap.setPixel(1, 1, Color.WHITE)

        val rgbaBytes = extractRgbaBytes(bitmap)
        val hash = PixelSha256V1.hash(2, 2, PixelFormatV1.RGBA_8888, rgbaBytes)

        assertEquals(
            "29197084fbf7d30ceaff21eed8b4c7a6e63004b54dd7da3a964c568453e470a7",
            hash,
        )
    }

    @Test
    fun `write stores rectified png and manifest`() {
        val projectDir = tempFolder.newFolder("project")
        val store = FileArtifactStoreV1(projectDir)
        val session = DrawingImportSession(
            projectId = "test-project",
            projectDir = projectDir,
            artifacts = emptyList(),
        )
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bitmap.setPixel(0, 0, Color.WHITE)

        val updated = RectifiedArtifactWriterV1().write(bitmap, store, session)

        val rectifiedFile = File(projectDir, ProjectLayoutV1.RECTIFIED_IMAGE_PNG)
        assertTrue(rectifiedFile.exists())
        assertTrue(rectifiedFile.length() > 0)

        val rectifiedEntry = updated.artifacts.firstOrNull { it.kind == ArtifactKindV1.RECTIFIED_IMAGE }
        assertNotNull(rectifiedEntry)
        rectifiedEntry?.let { entry ->
            assertEquals(ProjectLayoutV1.RECTIFIED_IMAGE_PNG, entry.relPath)
            assertEquals("image/png", entry.mime)
            assertTrue(entry.sha256.isNotBlank())
            assertNotNull(entry.pixelSha256)
        }

        val manifestFile = File(projectDir, ProjectLayoutV1.MANIFEST_JSON)
        assertTrue(manifestFile.exists())
    }
}
