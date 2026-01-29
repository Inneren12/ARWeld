package com.example.arweld.core.ar.api

import com.example.arweld.core.structural.model.StructuralModel

/**
 * Core AR engine interface for ARWeld.
 *
 * This interface defines the contract for AR rendering and alignment operations
 * that are independent of UI frameworks (Compose, View system) and feature-level
 * concerns. Implementations will bridge ARCore/Filament to structural model data.
 *
 * ## Boundary Rules
 * - core-ar does NOT depend on core-domain, core-data, or any feature modules
 * - core-ar depends only on core-structural for model geometry
 * - UI/Compose integration lives in feature-arview, which depends on core-ar
 *
 * @see com.example.arweld.core.structural.model.StructuralModel
 */
interface ArEngine {

    /**
     * Initializes the AR engine with the given structural model.
     *
     * @param model The structural model containing nodes, members, and geometry
     * @return true if initialization succeeded, false otherwise
     */
    suspend fun initialize(model: StructuralModel): Boolean

    /**
     * Releases all AR resources. Call when AR session ends.
     */
    fun release()

    /**
     * Returns whether the AR engine is currently initialized and ready.
     */
    val isInitialized: Boolean

    // TODO: Add methods for:
    // - loadMemberMesh(memberId: String): Loads mesh data for a structural member
    // - setWorldPose(pose: Pose3D): Sets the world-to-model transform after alignment
    // - getReferenceFrame(nodeId: String): Returns reference frame for a node
    // - captureFrame(): Captures current AR frame for evidence/alignment
}
