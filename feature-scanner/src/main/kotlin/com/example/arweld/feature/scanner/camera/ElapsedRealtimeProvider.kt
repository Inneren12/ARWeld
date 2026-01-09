package com.example.arweld.feature.scanner.camera

import android.os.SystemClock

interface ElapsedRealtimeProvider {
    fun elapsedRealtime(): Long
}

object SystemElapsedRealtimeProvider : ElapsedRealtimeProvider {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}
