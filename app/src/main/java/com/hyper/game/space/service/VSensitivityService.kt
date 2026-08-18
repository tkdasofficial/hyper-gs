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

import android.provider.Settings
import com.hyper.game.space.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.accessibilityservice.AccessibilityServiceInfo

class VSensitivityService : AccessibilityService() {
    companion object {
        var instance: VSensitivityService? = null
        const val ACTION_START_FOREGROUND = "START_FOREGROUND"
        const val ACTION_APPLY_SETTINGS = "APPLY_SETTINGS"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var failSafeRunnable: Runnable? = null
    var currentMultiplier: Float = 1.0f
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("VSensitivityService", "Service Connected")
        
        // Inject high-priority touch event listeners to reduce touch sampling latency
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 0 // Zero latency timeout
        serviceInfo = info

        startFailSafeLoop()
        applyVirtualSensitivity()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> startForegroundService()
            ACTION_APPLY_SETTINGS -> applyVirtualSensitivity()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun applyVirtualSensitivity() {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = SettingsRepository(this@VSensitivityService)
            val x = repository.getFloat(SettingsRepository.VSENS_X, 5f).first()
            val y = repository.getFloat(SettingsRepository.VSENS_Y, 5f).first()
            val z = repository.getFloat(SettingsRepository.VSENS_Z, 5f).first()
            
            // Calculate dynamic multiplier from 1.0x to 3.0x
            currentMultiplier = 1.0f + ((x + y + z) / 30f) * 2.0f
            val formattedMultiplier = String.format("%.1fx", currentMultiplier)
            
            Log.d("VSensitivityService", "Applying V-Sensitivity Multiplier: $formattedMultiplier")
            
            // Dynamic Pointer Speed Scaling via Settings.System
            try {
                if (Settings.System.canWrite(this@VSensitivityService)) {
                    // pointer_speed ranges from -7 to 7 on standard Android, or 0 to 7. 
                    // Let's scale based on the multiplier (1.0 to 3.0 maps roughly to 0 to 7)
                    val speed = ((currentMultiplier - 1.0f) / 2.0f * 7f).toInt().coerceIn(0, 7)
                    Settings.System.putInt(contentResolver, "pointer_speed", speed)
                    Log.d("VSensitivityService", "System pointer speed scaled to: $speed")
                } else {
                    Log.w("VSensitivityService", "WRITE_SETTINGS permission missing for pointer speed scaling.")
                }
            } catch (e: Exception) {
                Log.e("VSensitivityService", "Failed to scale pointer speed", e)
            }
        }
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
            .setContentTitle("Hyper Game Space V-Sens")
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
