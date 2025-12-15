package com.example.arweld.feature.arview.render

import android.content.Context
import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.Utils
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads GLB models from the app's assets using Filament's gltfio utilities.
 */
class AndroidFilamentModelLoader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ModelLoader {

    override val engine: Engine by lazy { Engine.create() }
    private val materialProvider by lazy { UbershaderProvider(engine) }
    private val assetLoader: AssetLoader by lazy {
        Utils.init()
        AssetLoader(engine, materialProvider, EntityManager.get())
    }
    private val resourceLoader: ResourceLoader by lazy { ResourceLoader(engine) }

    override suspend fun loadGlbFromAssets(assetPath: String): LoadedModel = withContext(dispatcher) {
        val buffer = readAsset(assetPath)
        val asset = assetLoader.createAssetFromBinary(buffer)
            ?: throw IllegalArgumentException("Unable to parse GLB at $assetPath")

        resourceLoader.loadResources(asset)
        asset.releaseSourceData()

        LoadedModel(
            engine = engine,
            asset = asset,
        ).also {
            Log.d(TAG, "Loaded GLB asset: $assetPath with ${asset.entities.size} entities")
        }
    }

    override fun destroyModel(model: LoadedModel) {
        runCatching {
            resourceLoader.destroyAsset(model.asset)
            assetLoader.destroyAsset(model.asset)
        }.onFailure { error ->
            Log.w(TAG, "Failed to destroy model", error)
        }
    }

    private fun readAsset(assetPath: String): ByteBuffer {
        val bytes = context.assets.open(assetPath).use { input ->
            input.readBytes()
        }
        return ByteBuffer.allocateDirect(bytes.size).apply {
            order(ByteOrder.nativeOrder())
            put(bytes)
            rewind()
        }
    }

    companion object {
        private const val TAG = "AndroidFilamentModelLoader"
    }
}
