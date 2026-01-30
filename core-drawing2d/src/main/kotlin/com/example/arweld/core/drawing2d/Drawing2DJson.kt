package com.example.arweld.core.drawing2d

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Centralized JSON configuration for Drawing2D schema serialization.
 *
 * This object provides a pre-configured [Json] instance that ensures consistent,
 * deterministic serialization across all Drawing2D types.
 *
 * Configuration choices:
 * - [classDiscriminator] = "type": For future sealed class hierarchies (entities)
 * - [prettyPrint] = false: Compact output for storage efficiency
 * - [encodeDefaults] = true: Always include default values for schema completeness
 * - [explicitNulls] = false: Omit null values to reduce payload size
 * - [ignoreUnknownKeys] = false: Strict parsing to catch schema mismatches early
 *
 * Usage:
 * ```
 * val json = Drawing2DJson.encodeToString(point)
 * val point = Drawing2DJson.decodeFromString<PointV1>(json)
 * ```
 */
object Drawing2DJson {

    /**
     * The configured Json instance for Drawing2D serialization.
     *
     * Use this directly if you need access to the Json instance,
     * or use the convenience functions [encodeToString] and [decodeFromString].
     */
    val json: Json = Json {
        classDiscriminator = "type"
        prettyPrint = false
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = false
    }

    /**
     * Encodes a value to a JSON string using the Drawing2D configuration.
     *
     * @param value The value to encode
     * @return The JSON string representation
     */
    inline fun <reified T> encodeToString(value: T): String {
        return json.encodeToString(value)
    }

    /**
     * Decodes a JSON string to a value using the Drawing2D configuration.
     *
     * @param string The JSON string to decode
     * @return The decoded value
     * @throws kotlinx.serialization.SerializationException if parsing fails
     */
    inline fun <reified T> decodeFromString(string: String): T {
        return json.decodeFromString(string)
    }
}
