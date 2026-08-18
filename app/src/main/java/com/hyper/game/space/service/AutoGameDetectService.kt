package com.hyper.game.space.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hyper.game.space.data.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AutoGameDetectService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var isPolling = false
    private var currentGame: String? = null
    
    private lateinit var repository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SettingsRepository(this)
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "auto_detect_channel")
            .setContentTitle("Hyper GS")
            .setContentText("Auto-Game Detection Active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(105, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isPolling) {
            isPolling = true
            startPolling()
        }
        return START_STICKY
    }

    private fun startPolling() {
        scope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            while (isActive && isPolling) {
                val masterToggle = repository.getBoolean(SettingsRepository.MASTER_TOGGLE, true).first()
                if (masterToggle) {
                    val enabledGames = repository.getStringSet(SettingsRepository.ENABLED_GAMES, emptySet()).first()
                    val foregroundApp = getForegroundApp(usageStatsManager)
                    
                    if (foregroundApp != null && enabledGames.contains(foregroundApp)) {
                        if (currentGame != foregroundApp) {
                            currentGame = foregroundApp
                            onGameLaunched(foregroundApp)
                        }
                    } else if (foregroundApp != null) {
                        // It's a non-game app
                        // Ignore home screen / system ui if we want, but normally just exiting game
                        // For simplicity, if foreground is NOT an enabled game, we exited.
                        if (currentGame != null) {
                            // Only exit if the new foreground app is not the same game and is not our own app?
                            // Actually, if we open our own app over the game, we might not want to close overlays.
                            if (foregroundApp != packageName) {
                                currentGame = null
                                onGameExited()
                            }
                        }
                    }
                } else {
                    if (currentGame != null) {
                        currentGame = null
                        onGameExited()
                    }
                }
                delay(1500)
            }
        }
    }
    
    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10000
        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        var foregroundPackage: String? = null
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundPackage = event.packageName
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (foregroundPackage == event.packageName) {
                    foregroundPackage = null
                }
            }
        }
        return foregroundPackage
    }

    private fun onGameLaunched(packageName: String) {
        val overlayIntent = Intent(this, OverlayService::class.java).apply {
            putExtra("SHOW_HUD", true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(overlayIntent)
        } else {
            startService(overlayIntent)
        }
    }

    private fun onGameExited() {
        val overlayIntent = Intent(this, OverlayService::class.java)
        stopService(overlayIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "auto_detect_channel",
                "Game Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isPolling = false
        scope.cancel()
        if (currentGame != null) {
            onGameExited()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
