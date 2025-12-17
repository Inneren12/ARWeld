package com.example.arweld.feature.arview.arcore

import android.net.Uri

interface ArScreenshotService {
    suspend fun captureArScreenshot(): Uri
}
