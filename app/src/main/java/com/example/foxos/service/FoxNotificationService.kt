package com.example.foxos.service

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

data class GroupedNotification(
    val packageName: String,
    val appLabel: String,
    val messages: List<String>,
    val latestTime: Long
)

class FoxNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateNotifications()
    }

    private fun updateNotifications() {
        try {
            val activeNotifications = activeNotifications ?: return
            val pm = packageManager
            
            // Update badge counts
            val badgeCounts = mutableMapOf<String, Int>()
            activeNotifications.forEach { sbn ->
                if (sbn.packageName != packageName) {
                    badgeCounts[sbn.packageName] = (badgeCounts[sbn.packageName] ?: 0) + 1
                }
            }
            _notificationBadges.value = badgeCounts
            
            val grouped = activeNotifications
                .filter { it.packageName != packageName }
                .groupBy { it.packageName }
                .mapNotNull { (pkg, notifs) ->
                    val appLabel = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                    } catch (e: Exception) { pkg }

                    val messages = notifs
                        .sortedByDescending { it.postTime }
                        .mapNotNull { sbn ->
                            sbn.notification?.extras?.getCharSequence("android.text")?.toString()
                        }
                        .take(3)
                    
                    val latest = notifs.maxOfOrNull { it.postTime } ?: 0L
                    GroupedNotification(pkg, appLabel, messages, latest)
                }
                .sortedByDescending { it.latestTime }

            _notifications.value = grouped
        } catch (e: Exception) {
            // Service may not yet have access
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateNotifications()
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    companion object {
        private var instance: FoxNotificationService? = null
        private val _notifications = MutableStateFlow<List<GroupedNotification>>(emptyList())
        val notifications: StateFlow<List<GroupedNotification>> = _notifications.asStateFlow()
        
        // Badge counts per package
        private val _notificationBadges = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationBadges: StateFlow<Map<String, Int>> = _notificationBadges.asStateFlow()
        
        fun getBadgeCount(packageName: String): Int = _notificationBadges.value[packageName] ?: 0
        
        fun isEnabled(context: Context): Boolean {
            val enabledListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, FoxNotificationService::class.java)
            return enabledListeners.contains(myComponent.flattenToString())
        }
    }
}
