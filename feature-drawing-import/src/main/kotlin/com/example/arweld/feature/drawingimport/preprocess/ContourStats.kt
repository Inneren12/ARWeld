package com.example.arweld.feature.drawingimport.preprocess

object ContourStats {
    fun topByArea(contours: List<ContourV1>, limit: Int): List<ContourV1> {
        if (limit <= 0) return emptyList()
        return contours.sortedByDescending { it.area }.take(limit)
    }
}
