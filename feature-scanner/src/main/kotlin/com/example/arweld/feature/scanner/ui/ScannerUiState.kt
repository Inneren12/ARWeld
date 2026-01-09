package com.example.arweld.feature.scanner.ui

sealed interface ScanCodeResolutionState {
    data object Idle : ScanCodeResolutionState
    data object Resolving : ScanCodeResolutionState
    data class NotFound(val code: String) : ScanCodeResolutionState
    data class Error(val message: String) : ScanCodeResolutionState
}

enum class CameraPermissionState {
    Granted,
    Requesting,
    Denied,
}
