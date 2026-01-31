package com.example.arweld.feature.drawingimport.ui.logging

import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorCode
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorMapper
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEvent
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEventLogger
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1

fun logDiagnosticsEvent(
    logger: DrawingImportEventLogger,
    event: DrawingImportEvent,
    state: String,
    projectId: String?,
    extras: Map<String, String> = emptyMap(),
    onLogged: () -> Unit,
) {
    logger.logEvent(
        event = event,
        state = state,
        projectId = projectId,
        extras = extras,
    )
    onLogged()
}

fun logPageDetectFailure(
    logger: DrawingImportEventLogger,
    failure: PageDetectFailureV1,
    projectId: String?,
    onLogged: () -> Unit,
) {
    val extras = buildMap {
        put("detectStage", failure.stage.name)
        put("detectCode", failure.code.name)
        failure.debugMessage?.let { put("debugMessage", it) }
    }
    logger.logEvent(
        event = DrawingImportEvent.ERROR,
        state = "page_detect_failure",
        projectId = projectId,
        errorCode = DrawingImportErrorCode.UNKNOWN,
        message = "${failure.stage.name}:${failure.code.name}",
        extras = extras,
    )
    onLogged()
}

fun logDiagnosticsError(
    logger: DrawingImportEventLogger,
    projectId: String?,
    code: DrawingImportErrorCode,
    onLogged: () -> Unit,
) {
    logger.logEvent(
        event = DrawingImportEvent.ERROR,
        state = "error",
        projectId = projectId,
        errorCode = code,
        message = DrawingImportErrorMapper.messageFor(code),
    )
    onLogged()
}
