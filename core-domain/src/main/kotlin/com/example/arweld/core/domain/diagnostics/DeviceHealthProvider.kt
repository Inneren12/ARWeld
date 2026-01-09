package com.example.arweld.core.domain.diagnostics

import kotlinx.coroutines.flow.StateFlow

interface DeviceHealthProvider {
    val deviceHealth: StateFlow<DeviceHealthSnapshot>
}
