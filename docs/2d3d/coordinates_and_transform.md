# 2D3D Editor — Coordinate Spaces & View Transform

This document defines the coordinate spaces used by the Manual 2D editor and the
pure, deterministic transform math that maps between them.

## Coordinate Spaces

### World (drawing) space
- Stored in `Drawing2D` (`Node2D.x/y`, `ScaleInfo` points).
- Units are **drawing units** (millimeters once scale calibration is applied).
- Origin is arbitrary and authored by the user (no implicit grid snap).

### Screen space
- Pixels in the Compose canvas.
- Origin at the top-left of the canvas container.
- Units are **device pixels**.

## View Transform

The editor maintains a view transform with two components:

```kotlin
ViewTransform(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
)
```

- `scale` maps world units → screen pixels.
- `offsetX/offsetY` translate the world origin into screen space.

### World → Screen

```
screenX = worldX * scale + offsetX
screenY = worldY * scale + offsetY
```

### Screen → World

```
worldX = (screenX - offsetX) / scale
worldY = (screenY - offsetY) / scale
```

### Clamping

To prevent runaway zoom, `scale` is clamped deterministically:

```
scale = clamp(scale, MIN_SCALE = 0.25, MAX_SCALE = 6.0)
```

## Gesture Updates

### Pan
- One-finger drag produces a delta in **screen pixels**.
- Pan updates only `offsetX/offsetY`:

```
offsetX = offsetX + panX
offsetY = offsetY + panY
```

### Zoom (around focal point)
- Pinch gestures provide a `zoomFactor` and a screen-space focal point.
- Compute the world point under the focal using the **previous** transform, then
  update the scale and offset so the focal point stays stationary:

```
worldFocal = screenToWorld(focalX, focalY)
scale' = clamp(scale * zoomFactor)

offsetX' = focalX - worldFocal.x * scale'
offsetY' = focalY - worldFocal.y * scale'
```

This keeps the screen-space focal point visually fixed while zooming.

## Determinism Notes

- All math is pure and side-effect free.
- Zoom clamping always uses the same min/max range.
- Gesture updates are applied in a fixed order: **pan → zoom**.
