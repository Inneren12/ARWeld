package com.example.arweld.core.data.file

import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest

class ChecksumCalculatorTest {

    @Test
    fun `computeSha256 returns expected hash for multi megabyte file`() {
        val content = ByteArray(1_500_000) { index -> (index % 256).toByte() }
        val tempFile = createTempFile(prefix = "checksum_test", suffix = ".bin").apply {
            deleteOnExit()
            outputStream().use { stream ->
                stream.write(content)
            }
        }

        val expectedHash = sha256Hex(content)

        val actualHash = computeSha256(tempFile)

        assertEquals(expectedHash, actualHash)
        // Cleanup temp file eagerly to avoid cluttering /tmp
        tempFile.delete()
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte ->
            String.format("%02x", byte)
        }
    }
}
