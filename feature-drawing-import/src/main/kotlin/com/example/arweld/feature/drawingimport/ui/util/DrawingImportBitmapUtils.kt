package com.example.arweld.feature.drawingimport.ui.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFrame

fun frameToBitmap(frame: PageDetectFrame): Bitmap {
    val expectedSize = frame.width * frame.height
    require(frame.gray.size == expectedSize) {
        "Frame gray data mismatch: expected $expectedSize bytes, got ${frame.gray.size}."
    }
    val pixels = IntArray(expectedSize)
    for (i in 0 until expectedSize) {
        val gray = frame.gray[i].toInt() and 0xFF
        pixels[i] = 0xFF000000.toInt() or (gray shl 16) or (gray shl 8) or gray
    }
    return Bitmap.createBitmap(
        pixels,
        frame.width,
        frame.height,
        Bitmap.Config.ARGB_8888,
    )
}

fun encodePng(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
