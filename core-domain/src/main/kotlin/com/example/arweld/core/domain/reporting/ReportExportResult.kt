package com.example.arweld.core.domain.reporting

sealed class ExportResult {
    data class Success(val bytesWritten: Long) : ExportResult()

    data class Failure(
        val message: String,
        val throwable: Throwable? = null,
    ) : ExportResult()
}
