package com.example.arweld.core.data.system

import com.example.arweld.core.domain.system.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
