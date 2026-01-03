package com.example.arweld.feature.arview.render

import android.content.Context
import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.Utils
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
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
        val asset = assetLoader.createAssetFromBinaryCompat(buffer)
            ?: throw IllegalArgumentException("Unable to parse GLB at $assetPath")

        resourceLoader.loadResources(asset)
        asset.releaseSourceDataCompat()

        LoadedModel(
            engine = engine,
            asset = asset,
        ).also {
            Log.d(TAG, "Loaded GLB asset: $assetPath with ${asset.entitiesCountCompat()} entities")
        }
    }

    override fun destroyModel(model: LoadedModel) {
        runCatching {
            resourceLoader.destroyAssetCompat(model.asset)
            assetLoader.destroyAssetCompat(model.asset)
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

private fun AssetLoader.createAssetFromBinaryCompat(buffer: ByteBuffer): FilamentAsset? {
    val methods = javaClass.methods.filter { it.name == "createAssetFromBinary" }
    for (m in methods) {
        val candidate = runCatching {
            when (m.parameterTypes.size) {
                1 -> m.invoke(this, buffer)
                2 -> m.invoke(this, buffer, buffer.remaining())
                else -> null
            }
        }.getOrNull()
        if (candidate is FilamentAsset) return candidate
    }
    return null
}

private fun AssetLoader.destroyAssetCompat(asset: FilamentAsset) {
    val methods = javaClass.methods.filter { it.name == "destroyAsset" && it.parameterTypes.size == 1 }
    for (m in methods) {
        runCatching { m.invoke(this, asset) }
            .onSuccess { return }
    }
}

private fun ResourceLoader.destroyAssetCompat(asset: FilamentAsset) {
    // Filament versions differ: some have destroyAsset(), some rely on destroyResources()
    val methods = javaClass.methods.filter {
        (it.name == "destroyAsset" || it.name == "destroyResources") && it.parameterTypes.size == 1
    }
    for (m in methods) {
        runCatching { m.invoke(this, asset) }
            .onSuccess { return }
    }
}

private fun FilamentAsset.releaseSourceDataCompat() {
    val method = javaClass.methods.firstOrNull {
        it.name == "releaseSourceData" && it.parameterTypes.isEmpty()
    }
    runCatching { method?.invoke(this) }.getOrNull()
}

private fun FilamentAsset.entitiesCountCompat(): Int {
    val method = javaClass.methods.firstOrNull {
        it.name == "getEntities" && it.parameterTypes.isEmpty()
    } ?: return 0
    val result = runCatching { method.invoke(this) }.getOrNull()
    return when (result) {
        is IntArray -> result.size
        is Array<*> -> result.size
        else -> 0
    }
}
