package com.example.arweld.feature.drawingeditor.viewmodel

import kotlin.math.abs

private const val MIN_SCALE = 0.25f
private const val MAX_SCALE = 6f

private const val DEFAULT_ZOOM_EPSILON = 0.0001f

data class Point2(val x: Float, val y: Float)

fun clampScale(scale: Float, minScale: Float = MIN_SCALE, maxScale: Float = MAX_SCALE): Float {
    return scale.coerceIn(minScale, maxScale)
}

fun worldToScreen(transform: ViewTransform, world: Point2): Point2 {
    return Point2(
        x = world.x * transform.scale + transform.offsetX,
        y = world.y * transform.scale + transform.offsetY,
    )
}

fun screenToWorld(transform: ViewTransform, screen: Point2): Point2 {
    return Point2(
        x = (screen.x - transform.offsetX) / transform.scale,
        y = (screen.y - transform.offsetY) / transform.scale,
    )
}

fun panBy(transform: ViewTransform, deltaX: Float, deltaY: Float): ViewTransform {
    if (deltaX == 0f && deltaY == 0f) {
        return transform
    }
    return transform.copy(
        offsetX = transform.offsetX + deltaX,
        offsetY = transform.offsetY + deltaY,
    )
}

fun zoomBy(
    transform: ViewTransform,
    zoomFactor: Float,
    focalX: Float,
    focalY: Float,
    minScale: Float = MIN_SCALE,
    maxScale: Float = MAX_SCALE,
    epsilon: Float = DEFAULT_ZOOM_EPSILON,
): ViewTransform {
    if (zoomFactor <= 0f || zoomFactor.isNaN() || abs(zoomFactor - 1f) <= epsilon) {
        return transform
    }
    val targetScale = clampScale(transform.scale * zoomFactor, minScale, maxScale)
    if (abs(targetScale - transform.scale) <= epsilon) {
        return transform
    }
    val worldFocal = screenToWorld(transform, Point2(focalX, focalY))
    val newOffsetX = focalX - worldFocal.x * targetScale
    val newOffsetY = focalY - worldFocal.y * targetScale
    return transform.copy(
        scale = targetScale,
        offsetX = newOffsetX,
        offsetY = newOffsetY,
    )
}

fun applyViewTransformGesture(
    transform: ViewTransform,
    panX: Float,
    panY: Float,
    zoomFactor: Float,
    focalX: Float,
    focalY: Float,
): ViewTransform {
    val panned = panBy(transform, panX, panY)
    return zoomBy(
        transform = panned,
        zoomFactor = zoomFactor,
        focalX = focalX,
        focalY = focalY,
    )
}
