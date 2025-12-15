package com.example.arweld

import android.app.Application
import com.example.arweld.core.data.seed.DbSeedInitializer
import javax.inject.Inject
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class ArWeldApplication : Application() {

    @Inject
    lateinit var dbSeedInitializer: DbSeedInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            dbSeedInitializer.seedIfEmpty()
        }
    }
}
