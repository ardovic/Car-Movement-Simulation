package com.ardovic.carmovementsimulation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.MotionEvent
import android.view.WindowManager
import com.ardovic.carmovementsimulation.engine.Renderer
import javax.microedition.khronos.opengles.GL10

class MainActivity : Activity(), Renderer.Drawer {

    private lateinit var surface: GLSurfaceView
    private lateinit var car: Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.navigationBarColor = Color.argb(0, 0, 0, 0)
        }
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        try {
            C.screenW = Display::class.java.getMethod("getRawWidth").invoke(windowManager.defaultDisplay) as Int
            C.screenH = Display::class.java.getMethod("getRawHeight").invoke(windowManager.defaultDisplay) as Int
        } catch (ignored: Exception) {
            C.screenW = metrics.widthPixels
            C.screenH = metrics.heightPixels
        }
        surface = GLSurfaceView(this)
        surface.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surface.holder.setFormat(PixelFormat.RGBA_8888)
        surface.setRenderer(Renderer(this))
        setContentView(surface)

        car = Car()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1)
            event.action = MotionEvent.ACTION_UP
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                car.driveTo(event.x, event.y)
            }
        }
        return true
    }

    override fun onDrawFrame(gl: GL10, renderer: Renderer) {
        car.update()
        car.draw(renderer)
    }
}
