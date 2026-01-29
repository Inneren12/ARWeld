package com.example.arweld.core.ar.alignment

import com.example.arweld.core.ar.api.DriftMonitorFactory
class DefaultDriftMonitorFactory : DriftMonitorFactory {
    override fun create(): DriftMonitor = DriftMonitor()
}
