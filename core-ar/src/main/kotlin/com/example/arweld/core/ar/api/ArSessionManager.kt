package com.example.arweld.core.ar.api

import android.content.Context
import com.google.ar.core.Session

/**
 * Abstraction for managing the AR session lifecycle without tying callers to a
 * concrete implementation.
 */
interface ArSessionManager {
    val session: Session?

    fun onResume(displayRotation: Int, viewportWidth: Int, viewportHeight: Int): String?

    fun onPause()

    fun onDestroy()
}

fun interface ArSessionManagerFactory {
    fun create(context: Context): ArSessionManager
}
