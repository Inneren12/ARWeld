package com.example.arweld.feature.arview.render

import android.content.Context
import android.util.Log
import com.example.arweld.feature.arview.BuildConfig
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.Utils
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject
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
    private val lodDecisions = ConcurrentHashMap<String, LodDecision>()

    override suspend fun loadGlbFromAssets(assetPath: String): LoadedModel = withContext(dispatcher) {
        val decision = lodDecisions.getOrPut(assetPath) { evaluateLodDecision(assetPath) }
        when (decision) {
            is LodDecision.TooComplex -> throw ModelTooComplexException(
                assetPath = decision.assetPath,
                triangleCount = decision.triangleCount,
                maxTriangles = decision.maxTriangles,
            )
            is LodDecision.Selected -> {
                val buffer = readAsset(decision.assetPath)
                val asset = assetLoader.createAssetFromBinaryCompat(buffer)
                    ?: throw IllegalArgumentException("Unable to parse GLB at ${decision.assetPath}")

                resourceLoader.loadResources(asset)
                asset.releaseSourceDataCompat()

                LoadedModel(
                    engine = engine,
                    asset = asset,
                ).also {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "Loaded GLB asset: ${decision.assetPath} with ${asset.entitiesCountCompat()} entities",
                        )
                    }
                }
            }
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
        val bytes = readAssetBytes(assetPath)
        return ByteBuffer.allocateDirect(bytes.size).apply {
            order(ByteOrder.nativeOrder())
            put(bytes)
            rewind()
        }
    }

    private fun readAssetBytes(assetPath: String): ByteArray =
        context.assets.open(assetPath).use { input -> input.readBytes() }

    private fun evaluateLodDecision(assetPath: String): LodDecision {
        val candidatePaths = buildLodCandidates(assetPath)
        var lastTriangleCount = 0
        for (candidate in candidatePaths) {
            val triangleCount = runCatching { estimateTriangleCount(candidate) }
                .getOrElse { error ->
                    Log.w(TAG, "Failed to estimate triangle count for $candidate", error)
                    MAX_TRIANGLES + 1
                }
            lastTriangleCount = triangleCount
            if (triangleCount <= MAX_TRIANGLES) {
                return LodDecision.Selected(candidate, triangleCount)
            }
        }
        return LodDecision.TooComplex(
            assetPath = candidatePaths.firstOrNull() ?: assetPath,
            triangleCount = lastTriangleCount,
            maxTriangles = MAX_TRIANGLES,
        )
    }

    private fun buildLodCandidates(assetPath: String): List<String> {
        if (!assetPath.endsWith(GLB_EXTENSION)) {
            return listOf(assetPath)
        }
        val basePath = assetPath.removeSuffix(GLB_EXTENSION)
        val lowPath = "${basePath}_low$GLB_EXTENSION"
        val mediumPath = "${basePath}_medium$GLB_EXTENSION"
        val candidates = mutableListOf<String>()
        if (assetExists(lowPath)) {
            candidates.add(lowPath)
        }
        if (assetExists(mediumPath)) {
            candidates.add(mediumPath)
        }
        if (assetPath !in candidates) {
            candidates.add(assetPath)
        }
        return candidates
    }

    private fun assetExists(assetPath: String): Boolean =
        runCatching { context.assets.open(assetPath).close() }
            .isSuccess

    private fun estimateTriangleCount(assetPath: String): Int {
        val bytes = readAssetBytes(assetPath)
        val gltf = parseGlbJson(bytes)
        val accessors = gltf.optJSONArray(JSON_ACCESSORS) ?: JSONArray()
        val meshes = gltf.optJSONArray(JSON_MESHES) ?: JSONArray()
        var triangleCount = 0
        for (i in 0 until meshes.length()) {
            val mesh = meshes.optJSONObject(i) ?: continue
            val primitives = mesh.optJSONArray(JSON_PRIMITIVES) ?: continue
            for (j in 0 until primitives.length()) {
                val primitive = primitives.optJSONObject(j) ?: continue
                val mode = primitive.optInt(JSON_MODE, GLTF_TRIANGLES_MODE)
                if (mode != GLTF_TRIANGLES_MODE) continue
                val indicesAccessor = primitive.optInt(JSON_INDICES, -1)
                triangleCount += if (indicesAccessor >= 0) {
                    val accessor = accessors.optJSONObject(indicesAccessor)
                    (accessor?.optInt(JSON_COUNT, 0) ?: 0) / TRIANGLE_DIVISOR
                } else {
                    val attributes = primitive.optJSONObject(JSON_ATTRIBUTES)
                    val positionAccessor = attributes?.optInt(JSON_POSITION, -1) ?: -1
                    if (positionAccessor >= 0) {
                        val accessor = accessors.optJSONObject(positionAccessor)
                        (accessor?.optInt(JSON_COUNT, 0) ?: 0) / TRIANGLE_DIVISOR
                    } else {
                        0
                    }
                }
            }
        }
        return triangleCount
    }

    private fun parseGlbJson(bytes: ByteArray): JSONObject {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        require(buffer.remaining() >= GLB_HEADER_SIZE) { "GLB header too small" }
        val magic = buffer.int
        require(magic == GLB_MAGIC) { "Invalid GLB magic" }
        buffer.int // version
        buffer.int // length
        while (buffer.remaining() >= GLB_CHUNK_HEADER_SIZE) {
            val chunkLength = buffer.int
            val chunkType = buffer.int
            if (chunkLength < 0 || chunkLength > buffer.remaining()) {
                throw IllegalStateException("Invalid GLB chunk length")
            }
            if (chunkType == GLB_JSON_CHUNK_TYPE) {
                val jsonBytes = ByteArray(chunkLength)
                buffer.get(jsonBytes)
                val jsonString = String(jsonBytes, Charsets.UTF_8)
                return JSONObject(jsonString)
            }
            buffer.position(buffer.position() + chunkLength)
        }
        throw IllegalStateException("Missing JSON chunk in GLB")
    }

    companion object {
        private const val TAG = "AndroidFilamentModelLoader"
        // Based on pilot model sizes to keep mobile GPUs at ~30 FPS without decimation.
        private const val MAX_TRIANGLES = 200_000
        private const val GLB_EXTENSION = ".glb"
        private const val GLB_MAGIC = 0x46546C67
        private const val GLB_HEADER_SIZE = 12
        private const val GLB_CHUNK_HEADER_SIZE = 8
        private const val GLB_JSON_CHUNK_TYPE = 0x4E4F534A
        private const val GLTF_TRIANGLES_MODE = 4
        private const val TRIANGLE_DIVISOR = 3
        private const val JSON_ACCESSORS = "accessors"
        private const val JSON_MESHES = "meshes"
        private const val JSON_PRIMITIVES = "primitives"
        private const val JSON_MODE = "mode"
        private const val JSON_INDICES = "indices"
        private const val JSON_COUNT = "count"
        private const val JSON_ATTRIBUTES = "attributes"
        private const val JSON_POSITION = "POSITION"
    }
}

sealed class LodDecision {
    data class Selected(val assetPath: String, val triangleCount: Int) : LodDecision()
    data class TooComplex(val assetPath: String, val triangleCount: Int, val maxTriangles: Int) : LodDecision()
}

class ModelTooComplexException(
    val assetPath: String,
    val triangleCount: Int,
    val maxTriangles: Int,
) : IllegalStateException("Model $assetPath has $triangleCount triangles (max $maxTriangles)")

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
