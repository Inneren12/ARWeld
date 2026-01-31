package com.example.arweld.feature.drawingimport.artifacts

import android.graphics.Bitmap
import com.example.arweld.core.drawing2d.artifacts.io.v1.ArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.ManifestWriterV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.crypto.PixelFormatV1
import com.example.arweld.core.drawing2d.crypto.PixelSha256V1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession
import com.example.arweld.feature.drawingimport.ui.RectifiedImageInfo
import java.io.ByteArrayOutputStream

class RectifiedArtifactWriterV1(
    private val manifestWriter: ManifestWriterV1 = ManifestWriterV1(),
) {
    fun write(
        rectifiedBitmap: Bitmap,
        projectStore: ArtifactStoreV1,
        session: DrawingImportSession,
        rewriteManifest: Boolean = true,
    ): DrawingImportSession {
        val normalized = ensureArgb8888(rectifiedBitmap)
        val width = normalized.width
        val height = normalized.height
        val rgbaBytes = extractRgbaBytes(normalized)
        val pixelSha256 = PixelSha256V1.hash(width, height, PixelFormatV1.RGBA_8888, rgbaBytes)
        val pngBytes = encodePng(normalized)
        val entry = projectStore.writeBytes(
            kind = ArtifactKindV1.RECTIFIED_IMAGE,
            relPath = ProjectLayoutV1.RECTIFIED_IMAGE_PNG,
            bytes = pngBytes,
            mime = "image/png",
        ).withPixelSha(pixelSha256)
        val artifactsWithoutManifest = session.artifacts
            .filter { it.kind != ArtifactKindV1.MANIFEST_JSON }
            .filterNot { it.kind == ArtifactKindV1.RECTIFIED_IMAGE && it.relPath == entry.relPath }
        val updatedArtifacts = artifactsWithoutManifest + entry
        val finalArtifacts = if (rewriteManifest) {
            val manifestEntry = manifestWriter.write(
                session.projectDir,
                DrawingImportArtifacts.buildManifest(
                    projectId = session.projectId,
                    artifacts = updatedArtifacts,
                ),
            )
            updatedArtifacts + manifestEntry
        } else {
            updatedArtifacts
        }

        if (normalized !== rectifiedBitmap) {
            normalized.recycle()
        }

        return session.copy(
            artifacts = finalArtifacts,
            rectifiedImageInfo = RectifiedImageInfo(width = width, height = height),
        )
    }
}

internal fun extractRgbaBytes(bitmap: Bitmap): ByteArray {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val rgba = ByteArray(width * height * 4)
    pixels.forEachIndexed { index, color ->
        val offset = index * 4
        rgba[offset] = ((color shr 16) and 0xFF).toByte()
        rgba[offset + 1] = ((color shr 8) and 0xFF).toByte()
        rgba[offset + 2] = (color and 0xFF).toByte()
        rgba[offset + 3] = ((color ushr 24) and 0xFF).toByte()
    }
    return rgba
}

private fun ensureArgb8888(bitmap: Bitmap): Bitmap {
    return if (bitmap.config == Bitmap.Config.ARGB_8888) {
        bitmap
    } else {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
}

private fun encodePng(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

private fun ArtifactEntryV1.withPixelSha(pixelSha256: String): ArtifactEntryV1 {
    return copy(pixelSha256 = pixelSha256)
}
