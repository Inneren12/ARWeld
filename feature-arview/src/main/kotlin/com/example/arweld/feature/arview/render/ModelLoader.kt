package com.example.arweld.feature.arview.render

import com.google.android.filament.Engine
import com.google.android.filament.gltfio.FilamentAsset

/**
 * Abstraction for loading 3D models that can be attached to the AR renderer.
 */
interface ModelLoader {
    /**
     * Loads a GLB file from the Android assets directory.
     *
     * @param assetPath Path relative to `src/main/assets`.
     * @return [LoadedModel] ready to be attached to a Filament/AR scene graph.
     */
    suspend fun loadGlbFromAssets(assetPath: String): LoadedModel

    /**
     * Releases engine resources associated with the model when it is no longer needed.
     */
    fun destroyModel(model: LoadedModel)
}

/**
 * Engine-specific wrapper around a loaded asset.
 */
data class LoadedModel(
    val engine: Engine,
    val asset: FilamentAsset,
) {
    /**
     * Entities that should be added to a scene to display the model.
     */
    val entities: IntArray get() = asset.entities
}
