package com.hyper.game.space.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.os.Handler
import android.os.Looper
import android.util.Log

class VSensitivityService : AccessibilityService() {

    companion object {
        var instance: VSensitivityService? = null
        const val ACTION_START_FOREGROUND = "START_FOREGROUND"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var failSafeRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("VSensitivityService", "Service Connected")
        startFailSafeLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_FOREGROUND) {
            startForegroundService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vsens_channel",
                "V-Sensitivity Engine",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "vsens_channel")
            .setContentTitle("Hyper GS V-Sens")
            .setContentText("Sensitivity Engine Active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(102, notification)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events (not used for raw touch, but required to be implemented)
    }

    override fun onInterrupt() {
        Log.d("VSensitivityService", "Service Interrupted")
    }

    override fun onDestroy() {
        instance = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private var lastTouchTime: Long = 0
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    fun dispatchScaledGesture(startX: Float, startY: Float, dx: Float, dy: Float, multiplier: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Ghost Touch Shield: Filter 'ACTION_POINTER_DOWN' jitter.
        // If multiple touches occur in < 5ms at extreme coordinates, discard as signal noise.
        if (currentTime - lastTouchTime < 5) {
            val distance = Math.sqrt(Math.pow((startX - lastTouchX).toDouble(), 2.0) + Math.pow((startY - lastTouchY).toDouble(), 2.0))
            if (distance > 500) { // arbitrary "extreme coordinate" delta
                Log.w("VSensitivityService", "Ghost touch detected and filtered")
                return
            }
        }
        
        lastTouchTime = currentTime
        lastTouchX = startX
        lastTouchY = startY

        val scaledDx = dx * multiplier
        val scaledDy = dy * multiplier

        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(startX + scaledDx, startY + scaledDy)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 10)) // 10ms duration
        
        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                // Gesture completed
            }
        }, null)
    }

    private fun startFailSafeLoop() {
        failSafeRunnable = object : Runnable {
            override fun run() {
                if (instance == null) {
                    Log.e("VSensitivityService", "Service died unexpectedly!")
                    // In a real app we might trigger a broadcast to restart it, 
                    // but as an AccessibilityService it must be enabled by the user.
                }
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(failSafeRunnable!!)
    }
}
