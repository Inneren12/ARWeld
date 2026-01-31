package com.example.arweld.feature.drawingimport.ui.debug

import com.example.arweld.feature.drawingimport.preprocess.ContourV1

data class PageDetectFrameInfo(
    val width: Int,
    val height: Int,
    val downscaleFactor: Double,
    val rotationAppliedDeg: Int,
)

data class ContourDebugInfo(
    val totalContours: Int,
    val topContours: List<ContourV1>,
)
