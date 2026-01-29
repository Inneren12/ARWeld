package com.example.arweld.core.ar.arcore

import android.content.Context
import com.example.arweld.core.ar.api.ArSessionManager
import com.example.arweld.core.ar.api.ArSessionManagerFactory
class DefaultArSessionManagerFactory : ArSessionManagerFactory {
    override fun create(context: Context): ArSessionManager = ARCoreSessionManager(context)
}
