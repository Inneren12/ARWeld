package com.example.arweld.core.data.reporting

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DocumentMetadataResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun displayName(uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.path?.let { File(it).name }
        }

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return null

        cursor.use {
            return if (it.moveToFirst()) {
                it.getString(0)
            } else {
                null
            }
        }
    }
}
