package com.example.arweld.feature.drawingimport.diagnostics

import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder

const val DRAWING_IMPORT_FEATURE = "drawing_import"

enum class DrawingImportEvent(val eventName: String, val phase: String) {
    SCREEN_OPENED("drawing_import_screen_opened", "screen"),
    PERMISSION_RESULT("drawing_import_permission_result", "permission"),
    CAMERA_BIND_SUCCESS("drawing_import_camera_bind_success", "camera"),
    CAMERA_BIND_FAILED("drawing_import_camera_bind_failed", "camera"),
    CAPTURE_START("drawing_import_capture_start", "capture"),
    CAPTURE_SAVED("drawing_import_capture_saved", "capture"),
    SESSION_RESET("drawing_import_session_reset", "session"),
    ERROR("drawing_import_error", "error"),
}

enum class DrawingImportErrorCode {
    PERMISSION_DENIED,
    CAMERA_BIND_FAILED,
    CAPTURE_FAILED,
    SAVE_FAILED,
    PREVIEW_LOAD_FAILED,
    UNKNOWN,
}

object DrawingImportErrorMapper {
    fun fromThrowable(throwable: Throwable): DrawingImportErrorCode = when (throwable) {
        is androidx.camera.core.ImageCaptureException,
        is IllegalStateException,
        -> DrawingImportErrorCode.CAPTURE_FAILED
        is java.io.IOException,
        is SecurityException,
        -> DrawingImportErrorCode.SAVE_FAILED
        else -> DrawingImportErrorCode.UNKNOWN
    }

    fun messageFor(code: DrawingImportErrorCode): String = when (code) {
        DrawingImportErrorCode.PERMISSION_DENIED -> "Permission denied"
        DrawingImportErrorCode.CAMERA_BIND_FAILED -> "Camera bind failed"
        DrawingImportErrorCode.CAPTURE_FAILED -> "Capture failed"
        DrawingImportErrorCode.SAVE_FAILED -> "Save failed"
        DrawingImportErrorCode.PREVIEW_LOAD_FAILED -> "Preview load failed"
        DrawingImportErrorCode.UNKNOWN -> "Unknown error"
    }
}

class DrawingImportEventLogger(
    private val diagnosticsRecorder: DiagnosticsRecorder,
) {
    fun logEvent(
        event: DrawingImportEvent,
        state: String,
        projectId: String?,
        errorCode: DrawingImportErrorCode? = null,
        message: String? = null,
        extras: Map<String, String> = emptyMap(),
    ) {
        val attributes = buildMap {
            put("feature", DRAWING_IMPORT_FEATURE)
            put("phase", event.phase)
            put("state", state)
            projectId?.let { put("projectId", it) }
            errorCode?.let { put("errorCode", it.name) }
            message?.let { put("message", it) }
            putAll(extras)
        }
        diagnosticsRecorder.recordEvent(event.eventName, attributes)
    }
}
