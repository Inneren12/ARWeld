package com.example.arweld

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ARWeld.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ArWeldApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization logic here
    }
}
