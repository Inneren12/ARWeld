package com.example.arweld.core.data.system

import android.os.Build
import com.example.arweld.core.domain.system.DeviceInfoProvider
import javax.inject.Inject
import javax.inject.Singleton

private const val UNKNOWN_DEVICE_ID = "unknown-device"

@Singleton
class AndroidDeviceInfoProvider @Inject constructor() : DeviceInfoProvider {
    override val deviceId: String = Build.MODEL.ifBlank { UNKNOWN_DEVICE_ID }
}
