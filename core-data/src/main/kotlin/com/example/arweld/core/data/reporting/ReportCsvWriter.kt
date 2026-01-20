package com.example.arweld.core.data.reporting

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class ReportCsvWriter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun writeCsv(uri: Uri, csv: String): Long {
        val bytes = csv.toByteArray(StandardCharsets.UTF_8)
        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Unable to open output stream for $uri")
        outputStream.use { stream ->
            stream.write(bytes)
            stream.flush()
        }
        return bytes.size.toLong()
    }
}
