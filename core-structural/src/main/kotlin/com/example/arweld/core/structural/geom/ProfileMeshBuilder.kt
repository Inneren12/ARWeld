package com.example.arweld.core.structural.geom

import com.example.arweld.core.structural.profiles.*

/**
 * Builds 3D meshes for structural profiles.
 * Each profile is generated in LOCAL coordinates with the member axis along +Z.
 * The cross-section is centered at the origin (0, 0) in the XY plane.
 */
object ProfileMeshBuilder {

    /**
     * Generate mesh for any ProfileSpec.
     */
    fun buildProfileMesh(profile: ProfileSpec, length: Float): Pair<FloatArray, IntArray> {
        return when (profile) {
            is WShapeSpec -> buildWShapeMesh(profile, length)
            is HssSpec -> buildHssMesh(profile, length)
            is ChannelSpec -> buildChannelMesh(profile, length)
            is AngleSpec -> buildAngleMesh(profile, length)
            is PlateSpec -> buildPlateMesh(profile, length)
        }
    }

    /**
     * Build mesh for W-shape (I-beam) profile.
     */
    private fun buildWShapeMesh(spec: WShapeSpec, length: Float): Pair<FloatArray, IntArray> {
        val d = spec.dMm.toFloat()
        val bf = spec.bfMm.toFloat()
        val tw = spec.twMm.toFloat()
        val tf = spec.tfMm.toFloat()

        // W-shape as a single polygon (outer contour)
        // Centered at origin
        val halfD = d / 2f
        val halfBf = bf / 2f
        val halfTw = tw / 2f

        // Build the I-shape contour (12 points, counter-clockwise when viewed from +Z)
        // Start from bottom-right and go counter-clockwise
        val points = listOf(
            // Bottom flange - right side
            halfBf to -halfD,
            halfBf to -halfD + tf,
            // Web - right side
            halfTw to -halfD + tf,
            halfTw to halfD - tf,
            // Top flange - right side
            halfBf to halfD - tf,
            halfBf to halfD,
            // Top flange - left side
            -halfBf to halfD,
            -halfBf to halfD - tf,
            // Web - left side
            -halfTw to halfD - tf,
            -halfTw to -halfD + tf,
            // Bottom flange - left side
            -halfBf to -halfD + tf,
            -halfBf to -halfD
        )

        return ExtrusionBuilder().extrude(points, emptyList(), length)
    }

    /**
     * Build mesh for HSS (rectangular tube) profile.
     */
    private fun buildHssMesh(spec: HssSpec, length: Float): Pair<FloatArray, IntArray> {
        val h = spec.hMm.toFloat()
        val b = spec.bMm.toFloat()
        val t = spec.tMm.toFloat()

        val halfH = h / 2f
        val halfB = b / 2f

        // Outer rectangle (CCW)
        val outer = listOf(
            -halfB to -halfH,
            halfB to -halfH,
            halfB to halfH,
            -halfB to halfH
        )

        // Inner rectangle (hole) - CW for proper winding
        val innerHalfH = halfH - t
        val innerHalfB = halfB - t
        val inner = listOf(
            -innerHalfB to -innerHalfH,
            -innerHalfB to innerHalfH,
            innerHalfB to innerHalfH,
            innerHalfB to -innerHalfH
        )

        return ExtrusionBuilder().extrude(outer, listOf(inner), length)
    }

    /**
     * Build mesh for C (channel) profile.
     */
    private fun buildChannelMesh(spec: ChannelSpec, length: Float): Pair<FloatArray, IntArray> {
        val d = spec.dMm.toFloat()
        val bf = spec.bfMm.toFloat()
        val tw = spec.twMm.toFloat()
        val tf = spec.tfMm.toFloat()

        val halfD = d / 2f

        // C-shape contour (8 points, counter-clockwise)
        // Web on the left, flanges extending to the right
        val points = listOf(
            // Bottom-left (back of web)
            0f to -halfD,
            // Bottom-right (end of bottom flange)
            bf to -halfD,
            bf to -halfD + tf,
            // Inner bottom flange
            tw to -halfD + tf,
            // Inner top flange
            tw to halfD - tf,
            // Top-right (end of top flange)
            bf to halfD - tf,
            bf to halfD,
            // Top-left (back of web)
            0f to halfD
        )

        // Center the section horizontally
        val centerX = bf / 2f
        val centeredPoints = points.map { (x, y) -> (x - centerX) to y }

        return ExtrusionBuilder().extrude(centeredPoints, emptyList(), length)
    }

    /**
     * Build mesh for L (angle) profile.
     */
    private fun buildAngleMesh(spec: AngleSpec, length: Float): Pair<FloatArray, IntArray> {
        val leg1 = spec.leg1Mm.toFloat()
        val leg2 = spec.leg2Mm.toFloat()
        val t = spec.tMm.toFloat()

        // L-shape contour (6 points, counter-clockwise)
        // Legs extending in +X and +Y directions from origin
        val points = listOf(
            // Origin corner
            0f to 0f,
            // Along leg1 (horizontal)
            leg1 to 0f,
            leg1 to t,
            // Inner corner
            t to t,
            // Along leg2 (vertical)
            t to leg2,
            0f to leg2
        )

        // Center the section
        val centerX = leg1 / 2f
        val centerY = leg2 / 2f
        val centeredPoints = points.map { (x, y) -> (x - centerX) to (y - centerY) }

        return ExtrusionBuilder().extrude(centeredPoints, emptyList(), length)
    }

    /**
     * Build mesh for PL (plate) profile.
     */
    private fun buildPlateMesh(spec: PlateSpec, length: Float): Pair<FloatArray, IntArray> {
        val w = spec.wMm.toFloat()
        val t = spec.tMm.toFloat()

        val halfW = w / 2f
        val halfT = t / 2f

        // Simple rectangle (CCW)
        val points = listOf(
            -halfW to -halfT,
            halfW to -halfT,
            halfW to halfT,
            -halfW to halfT
        )

        return ExtrusionBuilder().extrude(points, emptyList(), length)
    }
}
