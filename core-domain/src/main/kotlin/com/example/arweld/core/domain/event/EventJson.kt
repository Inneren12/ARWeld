package com.example.arweld.core.domain.event

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Stable JSON serialization for domain events (deterministic ordering, no random fields).
 */
object EventJson {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }

    fun encode(event: Event): String = json.encodeToString(event)
}
