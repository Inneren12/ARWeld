package com.example.arweld

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.arweld.core.data.seed.DbSeedInitializer
import com.example.arweld.diagnostics.DeviceHealthMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class ArWeldApplication : Application() {

    @Inject
    lateinit var dbSeedInitializer: DbSeedInitializer

    @Inject
    lateinit var deviceHealthMonitor: DeviceHealthMonitor

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
        applicationScope.launch {
            dbSeedInitializer.seedIfEmpty()
        }
        deviceHealthMonitor.start()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        deviceHealthMonitor.onTrimMemory(level)
    }
}
