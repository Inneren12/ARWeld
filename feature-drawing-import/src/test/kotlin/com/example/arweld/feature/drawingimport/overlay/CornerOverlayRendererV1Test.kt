package com.example.arweld.feature.drawingimport.overlay

import android.graphics.Bitmap
import android.graphics.Color
import com.example.arweld.feature.drawingimport.preprocess.CornerPointV1
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CornerOverlayRendererV1Test {

    @Test
    fun `render keeps bitmap size and produces png bytes`() {
        val base = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        base.eraseColor(Color.DKGRAY)
        val ordered = OrderedCornersV1(
            topLeft = CornerPointV1(10.0, 10.0),
            topRight = CornerPointV1(180.0, 12.0),
            bottomRight = CornerPointV1(175.0, 88.0),
            bottomLeft = CornerPointV1(12.0, 90.0),
        )
        val refined = OrderedCornersV1(
            topLeft = CornerPointV1(12.0, 14.0),
            topRight = CornerPointV1(178.0, 15.0),
            bottomRight = CornerPointV1(172.0, 86.0),
            bottomLeft = CornerPointV1(15.0, 87.0),
        )

        val rendered = CornerOverlayRendererV1().render(base, ordered, refined)

        assertEquals(200, rendered.width)
        assertEquals(100, rendered.height)

        val outputStream = ByteArrayOutputStream()
        rendered.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        assertTrue(bytes.isNotEmpty())
    }
}
