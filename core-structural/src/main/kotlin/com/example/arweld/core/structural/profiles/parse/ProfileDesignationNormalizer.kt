package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ChannelSeries
import com.example.arweld.core.structural.profiles.ProfileType

internal fun normalizeDesignation(
    type: ProfileType,
    rawDesignation: String,
    channelSeries: ChannelSeries? = null
): String {
    val sanitized = normalizeRawProfileInput(rawDesignation)
    val separatorsNormalized = sanitized.replace("(?i)\\s*x\\s*".toRegex(), "x")
    val compact = separatorsNormalized.replace("\\s+".toRegex(), "")

    return when (type) {
        ProfileType.HSS -> {
            val body = removePrefixIgnoreCase(compact, "HSS")
            val cleanedBody = body.replace('X', 'x')
            "HSS ${cleanedBody}"
        }

        ProfileType.PL -> canonicalizePlateDesignation(compact) ?: run {
            val body = removePrefixIgnoreCase(compact, "PL")
            "PL${body.replace('X', 'x')}"
        }

        ProfileType.C -> {
            val series = channelSeries ?: inferChannelSeries(compact)
            val prefix = when (series) {
                ChannelSeries.MC -> "MC"
                ChannelSeries.C -> "C"
            }
            val body = removeChannelPrefix(compact)
            "$prefix${body.replace('X', 'x')}"
        }

        ProfileType.L -> {
            val body = removePrefixIgnoreCase(compact, "L")
            "L${body.replace('X', 'x')}"
        }

        ProfileType.W -> {
            val body = removePrefixIgnoreCase(compact, "W")
            "W${body.replace('X', 'x')}"
        }
    }
}

private fun inferChannelSeries(designation: String): ChannelSeries {
    val trimmed = designation.trimStart()
    return if (trimmed.startsWith("MC", ignoreCase = true)) ChannelSeries.MC else ChannelSeries.C
}

private fun removeChannelPrefix(designation: String): String {
    val trimmed = designation.trimStart()
    return when {
        trimmed.startsWith("MC", ignoreCase = true) -> trimmed.drop(2)
        trimmed.startsWith("C", ignoreCase = true) -> trimmed.drop(1)
        else -> trimmed
    }
}
