package com.example.arweld.feature.arview.zone

import android.content.res.AssetManager
import android.util.Log
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.ZoneTransform
import com.example.arweld.feature.arview.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Provides known marker-to-zone transforms for aligning the AR model.
 */
class ZoneRegistry(
    private val zones: Map<String, ZoneTransform> = DEFAULT_ZONES,
) {

    private val alignedZones: MutableMap<String, Pose3D> = mutableMapOf()

    fun get(markerId: String): ZoneTransform? {
        val zone = zones[markerId] ?: run {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No zone configured for markerId=$markerId")
            }
            return null
        }
        if (!isMarkerSizeValid(zone.markerSizeMeters)) {
            Log.w(
                TAG,
                "Invalid markerSizeMeters=${zone.markerSizeMeters} for markerId=${zone.markerId}. " +
                    "Expected range ${MIN_MARKER_SIZE_METERS}..${MAX_MARKER_SIZE_METERS}.",
            )
            return null
        }
        return zone
    }

    @Synchronized
    fun recordAlignment(markerId: String, worldZonePose: Pose3D) {
        alignedZones[markerId] = worldZonePose
    }

    @Synchronized
    fun lastAlignedPose(): Pose3D? = alignedZones.values.lastOrNull()

    companion object {
        private const val TAG = "ZoneRegistry"
        private const val ZONES_ASSET_FILE = "zones.json"
        private const val MIN_MARKER_SIZE_METERS = 0.05f
        private const val MAX_MARKER_SIZE_METERS = 1.0f

        private val TEST_ZONE = ZoneTransform(
            markerId = "TEST_MARKER_01",
            tMarkerZone = Pose3D.Identity,
            markerSizeMeters = 0.12f,
        )

        private val DEFAULT_ZONES: Map<String, ZoneTransform> = mapOf(
            TEST_ZONE.markerId to TEST_ZONE,
        )

        fun fromAssets(assetManager: AssetManager): ZoneRegistry {
            val parsedZones = runCatching { loadZones(assetManager) }
                .onFailure { error ->
                    Log.w(TAG, "Unable to load zones from assets; using defaults", error)
                }
                .getOrNull()

            return ZoneRegistry(parsedZones ?: DEFAULT_ZONES)
        }

        private fun loadZones(assetManager: AssetManager): Map<String, ZoneTransform> {
            val json = assetManager.open(ZONES_ASSET_FILE).bufferedReader().use { it.readText() }
            val config = Json { ignoreUnknownKeys = true }.decodeFromString<ZoneRegistryConfig>(json)
            return config.zones.associateBy(
                keySelector = { it.markerId },
                valueTransform = { it.toZoneTransform() },
            )
        }

        private fun isMarkerSizeValid(markerSizeMeters: Float): Boolean {
            return markerSizeMeters in MIN_MARKER_SIZE_METERS..MAX_MARKER_SIZE_METERS
        }
    }
}

@Serializable
private data class ZoneRegistryConfig(
    val zones: List<ZoneConfig> = emptyList(),
)

@Serializable
private data class ZoneConfig(
    val markerId: String,
    val markerSizeMeters: Float,
    val tMarkerZone: PoseConfig,
)

@Serializable
private data class PoseConfig(
    val position: VectorConfig,
    val rotation: QuaternionConfig,
)

@Serializable
private data class VectorConfig(
    val x: Double,
    val y: Double,
    val z: Double,
)

@Serializable
private data class QuaternionConfig(
    val x: Double,
    val y: Double,
    val z: Double,
    @SerialName("w") val wValue: Double,
)

private fun ZoneConfig.toZoneTransform(): ZoneTransform {
    return ZoneTransform(
        markerId = markerId,
        tMarkerZone = tMarkerZone.toPose3D(),
        markerSizeMeters = markerSizeMeters,
    )
}

private fun PoseConfig.toPose3D(): Pose3D {
    return Pose3D(
        position = position.toVector3(),
        rotation = rotation.toQuaternion(),
    )
}

private fun VectorConfig.toVector3(): Vector3 = Vector3(x, y, z)

private fun QuaternionConfig.toQuaternion(): Quaternion = Quaternion(x, y, z, wValue)
