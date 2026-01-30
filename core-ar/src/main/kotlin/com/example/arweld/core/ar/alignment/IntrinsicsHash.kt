package com.example.arweld.core.ar.alignment

import android.util.Base64
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.floor

/**
 * Generates a deterministic hash identifier for camera intrinsics parameters.
 *
 * This hash enables correlation between alignment snapshots and specific camera/calibration
 * states in the ARWeld QC audit trail. The hash is stable across runs and devices for
 * identical intrinsic values.
 *
 * ## Algorithm (v1)
 *
 * ### Canonicalization Rules
 * 1. **Fixed-point conversion**: Doubles are converted to fixed-point integers using millipixel
 *    precision (1e-3 px). Each double value is multiplied by 1000 and rounded to the nearest
 *    long integer. This avoids float jitter from slight precision differences.
 *
 * 2. **Byte encoding**: Values are encoded as big-endian bytes with a version prefix:
 *    - Version tag: 2 bytes ASCII "v1"
 *    - width: 4 bytes (Int, big-endian)
 *    - height: 4 bytes (Int, big-endian)
 *    - fx: 8 bytes (Long, big-endian, millipixels)
 *    - fy: 8 bytes (Long, big-endian, millipixels)
 *    - cx: 8 bytes (Long, big-endian, millipixels)
 *    - cy: 8 bytes (Long, big-endian, millipixels)
 *    Total: 42 bytes
 *
 * 3. **Hashing**: SHA-256 digest of the canonical byte representation.
 *
 * 4. **Output encoding**: Base64url without padding (RFC 4648 §5), producing a 43-character string.
 *
 * ### Precision
 * - Millipixel precision (0.001 px) is sufficient for sub-pixel alignment quality metrics.
 * - Values up to ±9.2e15 millipixels can be represented (covers any practical resolution).
 *
 * @see CameraIntrinsics
 * @see intrinsicsHashV1
 */
object IntrinsicsHash {
    private const val VERSION_TAG = "v1"
    private const val MILLIPIXEL_SCALE = 1000L

    /**
     * Computes a deterministic hash for camera intrinsics parameters.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param fx Focal length in x (pixels)
     * @param fy Focal length in y (pixels)
     * @param cx Principal point x (pixels)
     * @param cy Principal point y (pixels)
     * @return Base64url-encoded SHA-256 hash (43 characters, no padding)
     */
    fun computeV1(
        width: Int,
        height: Int,
        fx: Double,
        fy: Double,
        cx: Double,
        cy: Double,
    ): String {
        val canonicalBytes = canonicalize(width, height, fx, fy, cx, cy)
        val digest = MessageDigest.getInstance("SHA-256").digest(canonicalBytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Computes a deterministic hash for camera intrinsics.
     *
     * @param intrinsics Camera intrinsics containing all parameters
     * @return Base64url-encoded SHA-256 hash (43 characters, no padding)
     */
    fun computeV1(intrinsics: CameraIntrinsics): String = computeV1(
        width = intrinsics.width,
        height = intrinsics.height,
        fx = intrinsics.fx,
        fy = intrinsics.fy,
        cx = intrinsics.cx,
        cy = intrinsics.cy,
    )

    /**
     * Converts intrinsics parameters to canonical byte representation.
     *
     * The byte layout is:
     * - [0-1]: Version tag "v1" (ASCII)
     * - [2-5]: width (Int, big-endian)
     * - [6-9]: height (Int, big-endian)
     * - [10-17]: fx in millipixels (Long, big-endian)
     * - [18-25]: fy in millipixels (Long, big-endian)
     * - [26-33]: cx in millipixels (Long, big-endian)
     * - [34-41]: cy in millipixels (Long, big-endian)
     */
    internal fun canonicalize(
        width: Int,
        height: Int,
        fx: Double,
        fy: Double,
        cx: Double,
        cy: Double,
    ): ByteArray {
        // Version prefix (2) + width (4) + height (4) + fx (8) + fy (8) + cx (8) + cy (8) = 42 bytes
        val buffer = ByteBuffer.allocate(42)

        // Version tag
        buffer.put(VERSION_TAG.toByteArray(Charsets.US_ASCII))

        // Image dimensions
        buffer.putInt(width)
        buffer.putInt(height)

        // Focal lengths and principal point in millipixels
        buffer.putLong(toMillipixels(fx))
        buffer.putLong(toMillipixels(fy))
        buffer.putLong(toMillipixels(cx))
        buffer.putLong(toMillipixels(cy))

        return buffer.array()
    }

    /**
     * Converts a pixel value to millipixels (fixed-point with 3 decimal places).
     *
     * Uses `floor(x + 0.5)` to match JVM Math.round semantics and ensure
     * deterministic behavior across floating-point representations.
     */
    private fun toMillipixels(pixels: Double): Long {
        val scaled = pixels * MILLIPIXEL_SCALE
        return floor(scaled + 0.5).toLong()
    }
}

/**
 * Computes a deterministic v1 hash for camera intrinsics parameters.
 *
 * This is a convenience top-level function that delegates to [IntrinsicsHash.computeV1].
 *
 * @param width Image width in pixels
 * @param height Image height in pixels
 * @param fx Focal length in x (pixels)
 * @param fy Focal length in y (pixels)
 * @param cx Principal point x (pixels)
 * @param cy Principal point y (pixels)
 * @return Base64url-encoded SHA-256 hash (43 characters, no padding)
 *
 * @see IntrinsicsHash
 */
fun intrinsicsHashV1(
    width: Int,
    height: Int,
    fx: Double,
    fy: Double,
    cx: Double,
    cy: Double,
): String = IntrinsicsHash.computeV1(width, height, fx, fy, cx, cy)

/**
 * Computes a deterministic v1 hash for camera intrinsics.
 *
 * This is a convenience top-level function that delegates to [IntrinsicsHash.computeV1].
 *
 * @param intrinsics Camera intrinsics containing all parameters
 * @return Base64url-encoded SHA-256 hash (43 characters, no padding)
 *
 * @see IntrinsicsHash
 */
fun intrinsicsHashV1(intrinsics: CameraIntrinsics): String = IntrinsicsHash.computeV1(intrinsics)
