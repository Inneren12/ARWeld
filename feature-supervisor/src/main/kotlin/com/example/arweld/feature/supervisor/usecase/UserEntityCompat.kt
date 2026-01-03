package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.domain.model.Role

internal fun Any.userNameCompat(fallback: String = toString()): String {
    return readStringCompat(
        "displayName",
        "name",
        "fullName",
        "username",
        "email",
    ) ?: fallback
}

internal fun Any.userRoleCompat(): Role {
    val raw = readStringCompat("role", "userRole", "user_type")
    val parsed = raw?.let { runCatching { Role.valueOf(it) }.getOrNull() }
    return parsed ?: Role.values().first()
}

private fun Any.readStringCompat(vararg candidates: String): String? {
    val cls = javaClass
    for (name in candidates) {
        val getterName = "get" + name.replaceFirstChar { it.uppercaseChar() }
        val m = cls.methods.firstOrNull { it.name == getterName && it.parameterTypes.isEmpty() }
            ?: cls.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }
        val v = runCatching { m?.invoke(this) }.getOrNull()
        if (v is String && v.isNotBlank()) return v

        val f = runCatching { cls.getDeclaredField(name).apply { isAccessible = true } }.getOrNull()
        val fv = runCatching { f?.get(this) }.getOrNull()
        if (fv is String && fv.isNotBlank()) return fv
    }
    return null
}
