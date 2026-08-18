package com.hyper.game.space.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationShieldService : NotificationListenerService() {
    
    companion object {
        var isShieldActive = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (isShieldActive && sbn != null) {
            val packageName = sbn.packageName
            // Simple rule: block everything except system UI if shield is active
            if (packageName != "android" && packageName != "com.android.systemui") {
                Log.d("NotificationShield", "Blocking notification from $packageName")
                cancelNotification(sbn.key)
            }
        }
    }
}
