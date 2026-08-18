package com.hyper.game.space.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log
import android.content.ComponentCallbacks2
import android.content.res.Configuration

class AntiHangEngine(private val context: Context) : ComponentCallbacks2 {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    init {
        context.applicationContext.registerComponentCallbacks(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}
    override fun onLowMemory() {}
    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE || level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            Log.w("AntiHangEngine", "TRIM_MEMORY_COMPLETE received, flushing caches")
            optimizeMemoryIfCritical()
        }
    }

    fun boostProcessPriority() {
        try {
            // Boost the priority of the current thread (assuming this is called on the main thread/game loop)
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            Log.d("AntiHangEngine", "Thread priority set to URGENT_DISPLAY")
        } catch (e: Exception) {
            Log.e("AntiHangEngine", "Failed to set thread priority", e)
        }
    }

    fun optimizeMemoryIfCritical() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // Calculate available memory percentage
        val percentAvailable = memoryInfo.availMem.toDouble() / memoryInfo.totalMem.toDouble()

        if (percentAvailable < 0.15 || memoryInfo.lowMemory) {
            Log.w("AntiHangEngine", "Critical memory threshold hit. Cleaning up background processes.")
            val runningAppProcesses = activityManager.runningAppProcesses
            runningAppProcesses?.forEach { processInfo ->
                if (processInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    try {
                        processInfo.pkgList.forEach { pkg ->
                            activityManager.killBackgroundProcesses(pkg)
                        }
                    } catch (e: Exception) {
                        Log.e("AntiHangEngine", "Failed to kill background process", e)
                    }
                }
            }
        } else {
            Log.d("AntiHangEngine", "Memory levels optimal: ${(percentAvailable * 100).toInt()}% available")
        }
    }
}
