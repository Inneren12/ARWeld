package com.example.arweld.core.data.file

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

private const val DEFAULT_BUFFER_SIZE_BYTES = 32 * 1024

/**
 * Compute a SHA-256 hash for the given [file] without loading the entire file into memory.
 */
fun computeSha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE_BYTES)

    FileInputStream(file).use { inputStream ->
        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) break
            digest.update(buffer, 0, read)
        }
    }

    return digest.digest().joinToString(separator = "") { byte ->
        String.format("%02x", byte)
    }
}
