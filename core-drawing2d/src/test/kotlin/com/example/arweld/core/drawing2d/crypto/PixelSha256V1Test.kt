package com.example.arweld.core.drawing2d.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class PixelSha256V1Test {

    @Test
    fun `hash matches deterministic 2x1 sample`() {
        val rgba = byteArrayOf(
            0x00, 0x01, 0x02, 0x03,
            0x10, 0x20, 0x30, 0x40,
        )

        val hash = PixelSha256V1.hash(2, 1, PixelFormatV1.RGBA_8888, rgba)

        assertThat(hash).isEqualTo(
            "ef3cfe9cb80ce5ddeaac56d3f461ebc63dab5d9d9b20086043052a54e884b53d"
        )
        assertThat(hash.length).isEqualTo(64)
        assertThat(hash).matches("^[0-9a-f]{64}$")
    }

    @Test
    fun `size mismatch throws`() {
        val rgba = ByteArray(7)

        val error = assertThrows(IllegalArgumentException::class.java) {
            PixelSha256V1.hash(2, 1, PixelFormatV1.RGBA_8888, rgba)
        }

        assertThat(error).hasMessageThat().contains("Expected 8 bytes")
    }

    @Test
    fun `same input returns same hash`() {
        val rgba = byteArrayOf(
            0x7f, 0x00, 0x10, 0x20,
            0x30, 0x40, 0x50, 0x60,
        )

        val first = PixelSha256V1.hash(2, 1, PixelFormatV1.RGBA_8888, rgba)
        val second = PixelSha256V1.hash(2, 1, PixelFormatV1.RGBA_8888, rgba)

        assertThat(first).isEqualTo(second)
    }
}
