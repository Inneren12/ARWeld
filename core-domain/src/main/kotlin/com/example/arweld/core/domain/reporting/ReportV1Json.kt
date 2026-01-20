package com.example.arweld.core.domain.reporting

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ReportV1Json {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }

    fun encode(report: ReportV1): String = json.encodeToString(report)
}
