package com.example.arweld.core.ar.alignment

import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.ByteBuffer

class IntrinsicsHashTest {

    // Standard test intrinsics: 1920x1080, fx=fy=1500, principal point at center
    private val standardWidth = 1920
    private val standardHeight = 1080
    private val standardFx = 1500.0
    private val standardFy = 1500.0
    private val standardCx = 960.0
    private val standardCy = 540.0

    // Golden hash for the standard test intrinsics (computed and verified)
    // width=1920, height=1080, fx=1500.0, fy=1500.0, cx=960.0, cy=540.0
    // Canonical bytes (hex): 76310000078000000438000000000016e360000000000016e36000000000000ea6000000000000083d60
    private val goldenHash = "h8fmegG20PIefeyS5O1-YzsyFdziNU_9y2avmcrVRhk"

    // ---------------------------------------------------------------------------
    // Golden Test
    // ---------------------------------------------------------------------------

    @Test
    fun `golden test - known input produces exact expected hash`() {
        val hash = intrinsicsHashV1(
            width = standardWidth,
            height = standardHeight,
            fx = standardFx,
            fy = standardFy,
            cx = standardCx,
            cy = standardCy,
        )

        assertThat(hash).isEqualTo(goldenHash)
    }

    @Test
    fun `golden test - CameraIntrinsics overload produces same hash`() {
        val intrinsics = CameraIntrinsics(
            fx = standardFx,
            fy = standardFy,
            cx = standardCx,
            cy = standardCy,
            width = standardWidth,
            height = standardHeight,
        )

        val hash = intrinsicsHashV1(intrinsics)

        assertThat(hash).isEqualTo(goldenHash)
    }

    // ---------------------------------------------------------------------------
    // Stability Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `stability - same input repeated 100 times yields identical output`() {
        val hashes = (1..100).map {
            intrinsicsHashV1(
                width = standardWidth,
                height = standardHeight,
                fx = standardFx,
                fy = standardFy,
                cx = standardCx,
                cy = standardCy,
            )
        }

        assertThat(hashes.toSet()).hasSize(1)
        assertThat(hashes.first()).isEqualTo(goldenHash)
    }

    @Test
    fun `stability - IntrinsicsHash object and top-level function produce same result`() {
        val objectHash = IntrinsicsHash.computeV1(
            width = standardWidth,
            height = standardHeight,
            fx = standardFx,
            fy = standardFy,
            cx = standardCx,
            cy = standardCy,
        )

        val functionHash = intrinsicsHashV1(
            width = standardWidth,
            height = standardHeight,
            fx = standardFx,
            fy = standardFy,
            cx = standardCx,
            cy = standardCy,
        )

        assertThat(objectHash).isEqualTo(functionHash)
    }

    // ---------------------------------------------------------------------------
    // Sensitivity Tests - Each parameter change produces a different hash
    // ---------------------------------------------------------------------------

    @Test
    fun `sensitivity - changing width changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth + 1, standardHeight, standardFx, standardFy, standardCx, standardCy)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - changing height changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth, standardHeight + 1, standardFx, standardFy, standardCx, standardCy)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - changing fx changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth, standardHeight, standardFx + 0.001, standardFy, standardCx, standardCy)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - changing fy changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy + 0.001, standardCx, standardCy)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - changing cx changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx + 0.001, standardCy)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - changing cy changes hash`() {
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val modified = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy + 0.001)

        assertThat(modified).isNotEqualTo(original)
    }

    @Test
    fun `sensitivity - change below millipixel threshold does not change hash`() {
        // Changes of less than 0.0005 (half a millipixel) round to the same value
        val original = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)
        val slightlyDifferent = intrinsicsHashV1(
            standardWidth,
            standardHeight,
            standardFx + 0.0004, // Less than half a millipixel
            standardFy,
            standardCx,
            standardCy,
        )

        assertThat(slightlyDifferent).isEqualTo(original)
    }

    // ---------------------------------------------------------------------------
    // Canonicalization Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `canonicalize - produces correct byte layout`() {
        val bytes = IntrinsicsHash.canonicalize(
            width = 1920,
            height = 1080,
            fx = 1500.0,
            fy = 1500.0,
            cx = 960.0,
            cy = 540.0,
        )

        // Total size: 2 (version) + 4 (width) + 4 (height) + 8*4 (doubles) = 42 bytes
        assertThat(bytes).hasLength(42)

        val buffer = ByteBuffer.wrap(bytes)

        // Version tag "v1"
        val versionBytes = ByteArray(2)
        buffer.get(versionBytes)
        assertThat(String(versionBytes, Charsets.US_ASCII)).isEqualTo("v1")

        // Width (1920)
        assertThat(buffer.int).isEqualTo(1920)

        // Height (1080)
        assertThat(buffer.int).isEqualTo(1080)

        // fx in millipixels (1500 * 1000 = 1500000)
        assertThat(buffer.long).isEqualTo(1_500_000L)

        // fy in millipixels (1500 * 1000 = 1500000)
        assertThat(buffer.long).isEqualTo(1_500_000L)

        // cx in millipixels (960 * 1000 = 960000)
        assertThat(buffer.long).isEqualTo(960_000L)

        // cy in millipixels (540 * 1000 = 540000)
        assertThat(buffer.long).isEqualTo(540_000L)
    }

    @Test
    fun `canonicalize - millipixel rounding is symmetric`() {
        // 1.4995 rounds to 1500 (1.4995 * 1000 = 1499.5 -> rounds to 1500)
        // 1.5005 rounds to 1501 (1.5005 * 1000 = 1500.5 -> rounds to 1501)
        val bytes1 = IntrinsicsHash.canonicalize(1920, 1080, 1.4995, 1.0, 1.0, 1.0)
        val bytes2 = IntrinsicsHash.canonicalize(1920, 1080, 1.5005, 1.0, 1.0, 1.0)

        val buffer1 = ByteBuffer.wrap(bytes1)
        val buffer2 = ByteBuffer.wrap(bytes2)

        // Skip version (2) + width (4) + height (4) = 10 bytes
        buffer1.position(10)
        buffer2.position(10)

        val fx1 = buffer1.long
        val fx2 = buffer2.long

        assertThat(fx1).isEqualTo(1500L)  // 1.4995 rounds to 1.500
        assertThat(fx2).isEqualTo(1501L)  // 1.5005 rounds to 1.501
    }

    // ---------------------------------------------------------------------------
    // Output Format Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `hash output is base64url without padding`() {
        val hash = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)

        // Base64url uses - and _ instead of + and /
        assertThat(hash).doesNotContain("+")
        assertThat(hash).doesNotContain("/")

        // No padding characters
        assertThat(hash).doesNotContain("=")

        // SHA-256 produces 32 bytes = 256 bits
        // Base64 encodes 6 bits per character: 256/6 = 42.67 -> 43 characters (no padding)
        assertThat(hash).hasLength(43)
    }

    @Test
    fun `hash only contains valid base64url characters`() {
        val hash = intrinsicsHashV1(standardWidth, standardHeight, standardFx, standardFy, standardCx, standardCy)

        // Valid base64url characters: A-Z, a-z, 0-9, -, _
        val validChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_')

        for (char in hash) {
            assertThat(validChars).contains(char)
        }
    }

    // ---------------------------------------------------------------------------
    // Edge Cases
    // ---------------------------------------------------------------------------

    @Test
    fun `handles zero values`() {
        val hash = intrinsicsHashV1(
            width = 0,
            height = 0,
            fx = 0.0,
            fy = 0.0,
            cx = 0.0,
            cy = 0.0,
        )

        assertThat(hash).isNotEmpty()
        assertThat(hash).hasLength(43)
    }

    @Test
    fun `handles negative focal length`() {
        // Negative values are unusual but should still produce valid hashes
        val hash = intrinsicsHashV1(
            width = 1920,
            height = 1080,
            fx = -1500.0,
            fy = 1500.0,
            cx = 960.0,
            cy = 540.0,
        )

        assertThat(hash).isNotEmpty()
        assertThat(hash).hasLength(43)

        // Should be different from positive fx
        val positiveHash = intrinsicsHashV1(1920, 1080, 1500.0, 1500.0, 960.0, 540.0)
        assertThat(hash).isNotEqualTo(positiveHash)
    }

    @Test
    fun `handles large dimensions`() {
        // 8K resolution
        val hash = intrinsicsHashV1(
            width = 7680,
            height = 4320,
            fx = 4000.0,
            fy = 4000.0,
            cx = 3840.0,
            cy = 2160.0,
        )

        assertThat(hash).isNotEmpty()
        assertThat(hash).hasLength(43)
    }

    @Test
    fun `handles fractional focal lengths with many decimal places`() {
        // Test with high precision input
        val hash = intrinsicsHashV1(
            width = 1920,
            height = 1080,
            fx = 1499.123456789,
            fy = 1500.987654321,
            cx = 959.555555555,
            cy = 540.111111111,
        )

        assertThat(hash).isNotEmpty()
        assertThat(hash).hasLength(43)
    }

    // ---------------------------------------------------------------------------
    // CameraIntrinsics Overload Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `CameraIntrinsics overload extracts all parameters correctly`() {
        val intrinsics = CameraIntrinsics(
            fx = 1234.567,
            fy = 1345.678,
            cx = 640.123,
            cy = 480.456,
            width = 1280,
            height = 960,
        )

        val fromObject = intrinsicsHashV1(intrinsics)
        val fromParams = intrinsicsHashV1(
            width = 1280,
            height = 960,
            fx = 1234.567,
            fy = 1345.678,
            cx = 640.123,
            cy = 480.456,
        )

        assertThat(fromObject).isEqualTo(fromParams)
    }
}
