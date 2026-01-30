package com.example.arweld.core.drawing2d.crypto

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

object Sha256V1 {
    private val hexDigits = "0123456789abcdef".toCharArray()

    fun bytesToHexLower(bytes: ByteArray): String {
        val chars = CharArray(bytes.size * 2)
        var index = 0
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            chars[index++] = hexDigits[value ushr 4]
            chars[index++] = hexDigits[value and 0x0F]
        }
        return String(chars)
    }

    fun hashBytes(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return bytesToHexLower(digest.digest(data))
    }

    fun hashStream(input: InputStream, bufferSize: Int = 64 * 1024): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(bufferSize)
        var read = input.read(buffer)
        while (read >= 0) {
            if (read > 0) {
                digest.update(buffer, 0, read)
            }
            read = input.read(buffer)
        }
        return bytesToHexLower(digest.digest())
    }

    fun hashFile(path: Path): String =
        Files.newInputStream(path).use { input ->
            hashStream(input)
        }
}
