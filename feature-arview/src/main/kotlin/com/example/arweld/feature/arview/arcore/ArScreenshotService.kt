package com.example.arweld.feature.arview.arcore

import android.net.Uri
import com.example.arweld.core.domain.evidence.ArScreenshotMeta

interface ArScreenshotService {
    suspend fun captureArScreenshotToFile(workItemId: String): Uri

    fun currentScreenshotMeta(): ArScreenshotMeta
}
