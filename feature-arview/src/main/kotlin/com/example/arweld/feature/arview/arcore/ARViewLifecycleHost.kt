package com.example.arweld.feature.arview.arcore

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Observes [Lifecycle] events and forwards them to [ARViewController].
 */
class ARViewLifecycleHost(
    private val lifecycle: Lifecycle,
    private val controller: ARViewController,
) : LifecycleEventObserver {

    private var created = false

    fun start() {
        lifecycle.addObserver(this)
        ensureCreated()
    }

    fun stop() {
        lifecycle.removeObserver(this)
        if (lifecycle.currentState == State.DESTROYED) {
            controller.onDestroy()
        }
    }

    private fun ensureCreated() {
        if (!created && lifecycle.currentState.isAtLeast(State.CREATED)) {
            controller.onCreate()
            created = true
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        when (event) {
            Event.ON_CREATE -> ensureCreated()
            Event.ON_RESUME -> controller.onResume()
            Event.ON_PAUSE -> controller.onPause()
            Event.ON_DESTROY -> controller.onDestroy()
            else -> Unit
        }
    }
}
