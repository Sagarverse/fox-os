package com.example.foxos.data

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.*

data class AppUsageInfo(
    val packageName: String,
    val totalTimeInForeground: Long
)

class UsageRepository(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getAppUsageStats(limit: Int = 10): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST, startTime, endTime
        )

        val launcherPackage = context.packageName
        val ignoredPackages = setOf(
            "com.android.settings",
            "com.android.systemui",
            "com.google.android.googlequicksearchbox",
            launcherPackage
        )

        return stats?.map {
            AppUsageInfo(it.packageName, it.totalTimeInForeground)
        }?.filter { 
            it.totalTimeInForeground > 0 && it.packageName !in ignoredPackages 
        }?.sortedByDescending { 
            it.totalTimeInForeground 
        }?.take(limit) ?: emptyList()
    }

    fun getTopPackageNames(limit: Int = 8): Set<String> {
        return getAppUsageStats(limit).map { it.packageName }.toSet()
    }
}