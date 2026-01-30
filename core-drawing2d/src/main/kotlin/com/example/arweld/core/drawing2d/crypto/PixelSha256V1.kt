package com.example.arweld.core.drawing2d.crypto

import java.lang.Math.multiplyExact

object PixelSha256V1 {
    fun hash(width: Int, height: Int, format: PixelFormatV1, rgbaBytes: ByteArray): String {
        val bytesPerPixel = when (format) {
            PixelFormatV1.RGBA_8888 -> 4
        }
        val pixelCount = multiplyExact(width, height)
        val expectedSize = multiplyExact(pixelCount, bytesPerPixel)
        require(rgbaBytes.size == expectedSize) {
            "Expected $expectedSize bytes for ${format.name} (${width}x${height}), got ${rgbaBytes.size}."
        }
        val header = "W=$width;H=$height;F=${format.name};".toByteArray(Charsets.UTF_8)
        val payload = ByteArray(header.size + rgbaBytes.size)
        System.arraycopy(header, 0, payload, 0, header.size)
        System.arraycopy(rgbaBytes, 0, payload, header.size, rgbaBytes.size)
        return Sha256V1.hashBytes(payload)
    }
}
