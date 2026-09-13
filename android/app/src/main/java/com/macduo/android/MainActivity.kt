package com.macduo.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private var projectionCode = 0
    private var projectionData: Intent? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { startIfReady() }

    private val projectionPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                projectionCode = result.resultCode
                projectionData = result.data
                startService()
            }
        }

    private val overlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { startIfReady() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        setContentView(buildUi())
    }

    private fun buildUi(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Mac Duo · 安卓折叠玻璃动画"
            textSize = 20f
        }
        val status = TextView(this).apply {
            text = "拖动滑块手动触发，或在折叠屏上自动跟随铰链角度。"
            textSize = 14f
        }
        val angleText = TextView(this).apply {
            text = "0°"
            textSize = 28f
        }
        val seek = SeekBar(this).apply { max = 100 }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                val degrees = progress / 100f * FoldState.MAX_FOLD_DEGREES
                FoldState.manual = true
                FoldState.manualFoldDegrees = degrees
                angleText.text = "%.0f°".format(degrees)
            }

            override fun onStartTrackingTouch(bar: SeekBar) {}
            override fun onStopTrackingTouch(bar: SeekBar) {}
        })

        val start = Button(this).apply { text = "开始 / 重新捕捉屏幕" }
        start.setOnClickListener { startIfReady() }

        val stop = Button(this).apply { text = "停止" }
        stop.setOnClickListener { stopService(Intent(this, DuoService::class.java)) }

        root.addView(title)
        root.addView(status)
        root.addView(angleText)
        root.addView(seek)
        root.addView(start)
        root.addView(stop)
        return root
    }

    private fun startIfReady() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermission.launch(intent)
            return
        }
        if (projectionData == null) {
            projectionPermission.launch(projectionManager.createScreenCaptureIntent())
        } else {
            startService()
        }
    }

    private fun startService() {
        val data = projectionData ?: return
        val intent = Intent(this, DuoService::class.java)
        intent.putExtra(DuoService.EXTRA_RESULT_CODE, projectionCode)
        intent.putExtra(DuoService.EXTRA_RESULT_DATA, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
