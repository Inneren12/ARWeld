package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Defines the page/canvas dimensions for a Drawing2D document.
 *
 * In v1, dimensions are always in pixels.
 *
 * @property widthPx The width of the page in pixels
 * @property heightPx The height of the page in pixels
 */
@Serializable
data class PageV1(
    val widthPx: Int,
    val heightPx: Int
)
