# 2D3D Editor — Selection & Hit Testing (S3-11)

This document defines the deterministic hit-testing rules used by the Manual 2D editor
for selecting nodes and members.

## Goals
- Tap selects the **nearest node** within tolerance (nodes have priority).
- If no node is within tolerance, select the **nearest member**.
- Taps on the background clear selection.
- Tolerance is stable across zoom levels by defining it in **screen space**.

## Tolerance (screen space)
- Hit tolerance is defined in dp constants (`EditorUiConstants.NODE_HIT_RADIUS_DP`, optionally `MEMBER_HIT_TOLERANCE_DP`).
- Default node hit radius: **16dp**.
- Converted to pixels at runtime using `LocalDensity`.
- Convert to world distance using the view transform:

```
toleranceWorld = tolerancePx / viewTransform.scale
```

This keeps selection behavior consistent regardless of zoom.

**Rendering alignment:** Member stroke widths are also defined in screen-space dp (converted to px and divided by `viewTransform.scale`), so the visual line thickness tracks the same zoom-stable policy as hit tolerances.

## Node hit test
`hitTestNode(worldTap, nodes, tolerancePx, viewTransform)`:
- Computes the world-space distance from the tap to each node.
- Selects the closest node within `toleranceWorld`.
- Deterministic tie-breaker: **lowest node id**.

## Member hit test
`hitTestMember(worldTap, members, nodes, tolerancePx, viewTransform)`:
- Resolves member endpoints from node references.
- Computes the point-to-segment distance in **world space**.
- Selects the closest member within `toleranceWorld`.
- Deterministic tie-breaker: **lowest member id**.

### Point-to-segment distance (robust)
For each member segment `[A,B]` and tap point `P`:

```
t = clamp( dot(P - A, B - A) / |B - A|^2, 0..1 )
closest = A + t * (B - A)
distance = |P - closest|
```

Degenerate segments (A == B) fall back to point distance `|P - A|`.

## Selection policy
Selection is derived by:
1. Attempting node hit-test.
2. Falling back to member hit-test if no node is hit.
3. Clearing selection if nothing is within tolerance.

This ensures nodes are always prioritized over members when both are in range. Tie-breaks are deterministic by **lowest id**.
