package com.macduo.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager

class DuoService : Service(), SensorEventListener {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val CHANNEL_ID = "macduo"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var windowManager: WindowManager
    private lateinit var sensorManager: SensorManager
    private lateinit var mainHandler: Handler

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var overlayView: GLSurfaceView? = null
    private var renderer: DuoRenderer? = null
    private var hingeSensor: Sensor? = null

    private var captureThread: HandlerThread? = null
    private var captureHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mainHandler = Handler(Looper.getMainLooper())
        hingeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)

        captureThread = HandlerThread("DuoCapture").also { it.start() }
        captureHandler = Handler(captureThread!!.looper)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()

        val code = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (data != null) {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            try {
                projection = manager.getMediaProjection(code, data)
                captureScreenOnce()
            } catch (_: Exception) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        if (hingeSensor != null) {
            FoldState.manual = false
            sensorManager.registerListener(this, hingeSensor, SensorManager.SENSOR_DELAY_UI, mainHandler)
        } else {
            FoldState.manual = true
        }
        return START_STICKY
    }

    private fun captureScreenOnce() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val dpi = metrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader
        reader.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                val plane = image.planes[0]
                val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(plane.buffer)
                mainHandler.post { showOverlay(bitmap) }
            } finally {
                image.close()
            }
        }, captureHandler)

        virtualDisplay = projection?.createVirtualDisplay(
            "MacDuo",
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            captureHandler
        )
    }

    private fun showOverlay(bitmap: Bitmap) {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )
        params.gravity = Gravity.TOP or Gravity.START

        val view = GLSurfaceView(this)
        view.setEGLContextClientVersion(2)
        renderer = DuoRenderer(bitmap)
        view.setRenderer(renderer)
        view.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        overlayView = view
        windowManager.addView(view, params)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HINGE_ANGLE) {
            // 180° means fully open, 0° means fully folded.
            val hinge = event.values[0]
            val progress = ((180f - hinge) / 180f).coerceIn(0f, 1f)
            FoldState.autoFoldDegrees = progress * FoldState.MAX_FOLD_DEGREES
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Mac Duo")
            .setContentText("折叠玻璃动画正在运行")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mac Duo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        virtualDisplay?.release()
        imageReader?.close()
        projection?.stop()
        overlayView?.let { windowManager.removeView(it) }
        renderer = null
        overlayView = null
        captureThread?.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
