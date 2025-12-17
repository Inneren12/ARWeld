package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ProfileType

internal fun normalizeDesignation(type: ProfileType, rawDesignation: String): String {
    val sanitized = normalizeRawProfileInput(rawDesignation)
    val compact = sanitized
        .replace("\\s+".toRegex(), "")
        .replace('×', 'x')
        .replace('X', 'x')

    val uppercase = compact.uppercase()
    val bodyWithoutPrefix = when (type) {
        ProfileType.HSS -> if (uppercase.startsWith("HSS")) compact.drop(3) else compact
        ProfileType.PL -> if (uppercase.startsWith("PL")) compact.drop(2) else compact
        ProfileType.C -> when {
            uppercase.startsWith("MC") -> compact.drop(2)
            uppercase.startsWith("C") -> compact.drop(1)
            else -> compact
        }

        ProfileType.L -> if (uppercase.startsWith("L")) compact.drop(1) else compact
        ProfileType.W -> if (uppercase.startsWith("W")) compact.drop(1) else compact
    }
    val cleanedBody = bodyWithoutPrefix.replace('X', 'x')

    return when (type) {
        ProfileType.HSS -> "HSS ${cleanedBody}"
        ProfileType.PL -> "PL${cleanedBody}"
        else -> "${type.name}${cleanedBody}"
    }
}
