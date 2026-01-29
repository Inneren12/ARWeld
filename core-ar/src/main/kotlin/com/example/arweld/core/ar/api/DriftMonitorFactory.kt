package com.example.arweld.core.ar.api

import com.example.arweld.core.ar.alignment.DriftMonitor

fun interface DriftMonitorFactory {
    fun create(): DriftMonitor
}
