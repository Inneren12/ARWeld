package com.example.arweld.core.ar.arcore

import android.content.Context
import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException

/**
 * Manages lifecycle of a [Session] instance.
 *
 * Creates the session lazily on first resume and keeps it configured for world
 * tracking on ARCore-capable devices (Pixel 9 target).
 */
class ARCoreSessionManager(
    private val context: Context,
) {

    var session: Session? = null
        private set

    /**
     * Lazily creates and resumes the ARCore [Session].
     *
     * @param displayRotation The current display rotation for camera configuration.
     * @return A nullable error message to be surfaced to UI overlays.
     */
    fun onResume(displayRotation: Int, viewportWidth: Int, viewportHeight: Int): String? {
        return try {
            val activeSession = session ?: createSession()
            configureSession(activeSession, displayRotation, viewportWidth, viewportHeight)
            activeSession.resume()
            null
        } catch (error: UnavailableDeviceNotCompatibleException) {
            logAndReset("ARCore not supported on this device", error)
            "ARCore is not supported on this device."
        } catch (error: UnavailableArcoreNotInstalledException) {
            logAndReset("ARCore Services not installed", error)
            "ARCore needs to be installed from Play Store."
        } catch (error: UnavailableApkTooOldException) {
            logAndReset("ARCore Services too old", error)
            "Update Google Play Services for AR to continue."
        } catch (error: UnavailableSdkTooOldException) {
            logAndReset("ARCore SDK too old", error)
            "Update the app to use a newer ARCore SDK."
        } catch (error: CameraNotAvailableException) {
            logAndReset("Camera not available for ARCore", error)
            "Camera not available. Please restart AR view."
        } catch (error: Exception) {
            logAndReset("Failed to start AR session", error)
            "Failed to start AR session."
        }
    }

    fun onPause() {
        try {
            session?.pause()
        } catch (error: Exception) {
            Log.w(TAG, "Error while pausing ARCore session", error)
        }
    }

    fun onDestroy() {
        session?.close()
        session = null
    }

    private fun createSession(): Session {
        val newSession = Session(context)
        session = newSession
        return newSession
    }

    private fun configureSession(session: Session, displayRotation: Int, viewportWidth: Int, viewportHeight: Int) {
        val config = Config(session).apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        }
        session.configure(config)
        val safeWidth = viewportWidth.coerceAtLeast(1)
        val safeHeight = viewportHeight.coerceAtLeast(1)
        session.setDisplayGeometry(displayRotation, safeWidth, safeHeight)
    }

    private fun logAndReset(message: String, error: Exception) {
        Log.e(TAG, message, error)
        session?.close()
        session = null
    }

    companion object {
        private const val TAG = "ARCoreSessionManager"
    }
}
