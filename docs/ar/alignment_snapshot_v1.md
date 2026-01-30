# AlignmentSnapshot Schema v1

This document describes the `AlignmentSnapshot` schema used for QC audit-grade AR alignment logging in the ARWeld system.

## Overview

The `AlignmentSnapshot` is an immutable data structure that captures the state of AR alignment at a specific point in time. It is designed to provide complete traceability for QC audits by ensuring all mandatory alignment data is present and validated.

## Schema Version

**Current Version:** 1

The `schemaVersion` field is embedded in every `AlignmentSnapshot` instance to enable forward-compatible evolution. Consumers should check this field before processing to handle potential schema migrations.

## Location

- **Package:** `com.example.arweld.core.ar.api`
- **Files:**
  - `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/AlignmentSnapshot.kt`
  - `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/AlignmentQuality.kt`

## Fields

### AlignmentSnapshot

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | `Int` | Yes (auto) | Always `1` for this schema version. Read-only, set automatically. |
| `intrinsicsHash` | `String` | Yes | Hash identifying the camera intrinsics configuration. Must be non-blank. |
| `reprojection` | `AlignmentQuality` | Yes | Reprojection error statistics for alignment quality assessment. |
| `gravity` | `Vector3` | Yes | Device gravity vector in sensor coordinates (m/s²). |

### AlignmentQuality

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `meanPx` | `Double` | Yes | >= 0 | Mean reprojection error in pixels. |
| `maxPx` | `Double` | Yes | >= 0 | Maximum reprojection error in pixels. |
| `samples` | `Int` | Yes | >= 0 | Number of sample points used for error calculation. |

## Field Details

### intrinsicsHash

A hash string that uniquely identifies the camera intrinsics used during alignment. This enables correlation between alignment snapshots and specific device/calibration states.

**Derivation (high-level):**
The hash is computed from the camera's intrinsic parameters:
- Focal length (fx, fy)
- Principal point (cx, cy)
- Image dimensions (width, height)

The exact hashing algorithm (e.g., SHA-256 of a canonical JSON representation) will be defined in a subsequent PR. For now, any non-blank string is accepted.

**Example values:**
- `"sha256:a1b2c3d4e5f6..."`
- `"cam_1920x1080_fx1500_fy1500_cx960_cy540"`

### reprojection

Quantitative measure of alignment quality based on reprojection error. Reprojection error is the L2 (Euclidean) distance in the image plane between:
- Projected 3D model points
- Observed 2D feature points (e.g., marker corners)

**Units:** Pixels (at camera native resolution)

**Quality interpretation:**
| Mean Error | Quality | Suitability |
|------------|---------|-------------|
| < 2 px | Excellent | QC audit ready |
| 2-5 px | Good | Acceptable for most use cases |
| > 5 px | Poor | May require recalibration |

### gravity

The gravity vector in the **device's sensor coordinate frame** at the time of alignment.

**Coordinate system (Android sensor coordinates):**
- **X:** Points right when device is in portrait orientation
- **Y:** Points up (toward top of device)
- **Z:** Points out of the screen toward the user

**Expected values:**
- Device flat (screen up): `gravity ≈ (0, 0, -9.81)`
- Device upright (portrait): `gravity ≈ (0, -9.81, 0)`
- Device tilted: Components vary based on orientation

**Units:** m/s² (meters per second squared)

**Purpose:**
- Validate device orientation during alignment
- Detect unexpected tilt that may affect alignment accuracy
- Provide reference for gravity-aware pose correction

## Validation Rules

### Compile-time guarantees
- All fields are non-null Kotlin types (no `?` suffix)
- `AlignmentSnapshot` cannot be constructed without providing all three fields
- Type safety enforced by Kotlin compiler

### Runtime validation
- `intrinsicsHash` must not be blank (throws `IllegalArgumentException`)
- `meanPx` must be >= 0 (throws `IllegalArgumentException`)
- `maxPx` must be >= 0 (throws `IllegalArgumentException`)
- `samples` must be >= 0 (throws `IllegalArgumentException`)

## Usage Example

```kotlin
import com.example.arweld.core.ar.api.AlignmentQuality
import com.example.arweld.core.ar.api.AlignmentSnapshot
import com.example.arweld.core.domain.spatial.Vector3

// Create alignment quality metrics
val quality = AlignmentQuality(
    meanPx = 1.2,
    maxPx = 2.8,
    samples = 16
)

// Create alignment snapshot with all required fields
val snapshot = AlignmentSnapshot(
    intrinsicsHash = "sha256:abc123def456",
    reprojection = quality,
    gravity = Vector3(0.1, -0.05, -9.79)
)

// Access schema version
println("Schema version: ${snapshot.schemaVersion}") // Output: 1
println("Schema constant: ${AlignmentSnapshot.SCHEMA_VERSION}") // Output: 1
```

## Dependencies

The `AlignmentSnapshot` class uses:
- `Vector3` from `core-structural` module (`com.example.arweld.core.domain.spatial.Vector3`)

This follows the architecture rule that `core-ar` may depend on `core-structural` for shared spatial math types.

## Future Evolution

Potential additions in future schema versions:
- Timestamp of alignment capture
- Device model/calibration profile identifier
- Confidence interval for error metrics
- Multi-marker aggregation data
- Pose stability metrics

Any such additions would increment `schemaVersion` and maintain backward compatibility where possible.

## Related Documents

- [MODULES.md](../MODULES.md) - Module architecture and dependencies
- [FILE_OVERVIEW.md](../FILE_OVERVIEW.md) - Codebase navigation guide
- [core-ar_boundary.md](../architecture/core-ar_boundary.md) - core-ar module boundary rules
