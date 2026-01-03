package com.example.arweld.ui.ar

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.feature.work.model.AR_SCREENSHOT_REQUEST_KEY
import com.example.arweld.feature.work.model.AR_SCREENSHOT_RESULT_KEY
import com.example.arweld.feature.work.model.ArScreenshotResult
import com.example.arweld.feature.arview.ui.arview.ARViewScreen

@Composable
fun ARViewRoute(
    navController: NavHostController,
    workItemId: String?,
) {
    val previousBackStackEntry = navController.previousBackStackEntry
    val savedStateHandle = previousBackStackEntry?.savedStateHandle
    val expectingResult = savedStateHandle?.get<Boolean>(AR_SCREENSHOT_REQUEST_KEY) == true

    val onScreenshotCaptured: ((Uri, ArScreenshotMeta) -> Unit)? = if (expectingResult) {
        { uri, meta ->
            savedStateHandle?.set(
                AR_SCREENSHOT_RESULT_KEY,
                ArScreenshotResult(
                    uriString = uri.toString(),
                    markerIds = meta.markerIds,
                    trackingState = meta.trackingState,
                    alignmentQualityScore = meta.alignmentQualityScore,
                    distanceToMarker = meta.distanceToMarker,
                    capturedAtMillis = meta.timestamp,
                ),
            )
            savedStateHandle?.set(AR_SCREENSHOT_REQUEST_KEY, false)
            navController.popBackStack()
        }
    } else {
        null
    }

    ARViewScreen(
        workItemId = workItemId,
        onBack = {
            savedStateHandle?.set(AR_SCREENSHOT_REQUEST_KEY, false)
            navController.popBackStack()
        },
        infoOverlay = { /* Placeholder for overlay content (tracking/debug info) */ },
        onScreenshotCaptured = onScreenshotCaptured,
    )
}
