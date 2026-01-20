package com.example.arweld.core.data.file

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject

private const val DEFAULT_BUFFER_SIZE_BYTES = 32 * 1024

class Sha256Hasher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun stream(file: File): String = streamWithSize(file).sha256Hex

    fun stream(uri: Uri): String = streamWithSize(uri).sha256Hex

    fun streamWithSize(file: File): HashResult {
        FileInputStream(file).use { inputStream ->
            return hashStream(inputStream)
        }
    }

    fun streamWithSize(uri: Uri): HashResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for $uri")
        inputStream.use { stream ->
            return hashStream(stream)
        }
    }

    private fun hashStream(inputStream: InputStream): HashResult {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE_BYTES)
        var totalBytes = 0L

        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) break
            digest.update(buffer, 0, read)
            totalBytes += read
        }

        val hex = digest.digest().joinToString(separator = "") { byte ->
            String.format("%02x", byte)
        }

        return HashResult(sha256Hex = hex, sizeBytes = totalBytes)
    }
}

data class HashResult(
    val sha256Hex: String,
    val sizeBytes: Long,
)
