package com.example.arweld.core.ar.capture

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import java.util.concurrent.CountDownLatch

class SurfaceViewCaptureTestActivity : Activity() {
    lateinit var surfaceView: SurfaceView
        private set
    val surfaceReady = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surfaceView = SurfaceView(this).apply {
            layoutParams = FrameLayout.LayoutParams(200, 200)
            holder.addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        surfaceReady.countDown()
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int,
                    ) = Unit

                    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
                },
            )
        }
        val root = FrameLayout(this).apply {
            addView(surfaceView)
        }
        setContentView(root)
    }

    fun drawSolidColor() {
        val holder = surfaceView.holder
        val canvas = holder.lockCanvas()
        canvas.drawColor(Color.BLUE)
        holder.unlockCanvasAndPost(canvas)
    }
}
