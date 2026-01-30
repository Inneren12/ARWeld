package com.example.arweld.core.ar.api

import com.example.arweld.core.domain.spatial.Vector3

/**
 * Immutable snapshot of AR alignment state for QC audit logging.
 *
 * This payload captures all mandatory fields required for audit-grade alignment records.
 * All fields are non-null and must be present at construction time to ensure data integrity
 * for traceability and QC reporting.
 *
 * ## Schema Versioning
 * The [schemaVersion] field enables forward-compatible evolution of this payload.
 * Consumers should check `schemaVersion` before deserializing to handle migrations.
 * Current version: **1**
 *
 * ## Fields
 *
 * ### intrinsicsHash
 * A hash string uniquely identifying the camera intrinsics used during alignment.
 * This allows correlation between alignment snapshots and specific device/calibration states.
 *
 * **Derivation (high-level):**
 * The hash is computed from the camera's intrinsic parameters (focal length, principal point,
 * image dimensions). The exact hashing algorithm (e.g., SHA-256 of a canonical JSON representation)
 * will be defined in a later PR. For now, this field must be non-blank to ensure traceability.
 *
 * ### reprojection
 * Quantitative alignment quality metrics based on reprojection error.
 * See [AlignmentQuality] for detailed documentation on error interpretation.
 *
 * ### gravity
 * The gravity vector in the **device's sensor coordinate frame** at the time of alignment.
 * This uses the standard Android sensor coordinate system:
 * - **X**: Points to the right when the device is held in portrait orientation
 * - **Y**: Points up (top of device)
 * - **Z**: Points out of the screen toward the user
 *
 * When the device is stationary and level (screen facing up), gravity ≈ (0, 0, -9.81).
 * This vector is used to validate device orientation and detect tilt during alignment.
 * Units are m/s² (meters per second squared).
 *
 * @property schemaVersion Version of the snapshot schema. Always 1 for this implementation.
 * @property intrinsicsHash Hash identifying the camera intrinsics configuration. Must be non-blank.
 * @property reprojection Reprojection error statistics for alignment quality assessment.
 * @property gravity Device gravity vector in sensor coordinates (m/s²).
 *
 * @throws IllegalArgumentException if intrinsicsHash is blank.
 */
data class AlignmentSnapshot(
    val intrinsicsHash: String,
    val reprojection: AlignmentQuality,
    val gravity: Vector3,
) {
    /**
     * Schema version for this payload format.
     * Version 1: Initial schema with intrinsicsHash, reprojection, and gravity fields.
     */
    val schemaVersion: Int = SCHEMA_VERSION

    init {
        require(intrinsicsHash.isNotBlank()) { "intrinsicsHash must not be blank" }
    }

    companion object {
        /**
         * Current schema version for AlignmentSnapshot.
         */
        const val SCHEMA_VERSION: Int = 1
    }
}
