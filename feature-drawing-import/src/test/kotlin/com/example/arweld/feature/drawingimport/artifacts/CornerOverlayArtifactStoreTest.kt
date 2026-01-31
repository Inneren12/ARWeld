package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CornerOverlayArtifactStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `artifact store writes corners overlay relPath and sha256`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val store = FileArtifactStoreV1(baseDir)
        val bytes = ByteArray(32) { it.toByte() }

        val entry = store.writeBytes(
            kind = ArtifactKindV1.OVERLAY,
            relPath = ProjectLayoutV1.overlay("corners"),
            bytes = bytes,
            mime = "image/png",
        )

        val expectedRelPath = ProjectLayoutV1.overlay("corners")
        assertEquals(expectedRelPath, entry.relPath)
        assertEquals("image/png", entry.mime)
        assertTrue(entry.sha256.isNotBlank())
        val file = File(baseDir, entry.relPath)
        assertTrue(file.exists())
        assertTrue(file.length() > 0L)
    }
}
