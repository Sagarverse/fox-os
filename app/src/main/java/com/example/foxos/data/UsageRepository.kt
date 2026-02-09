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

    fun getAppUsageStats(): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        return stats?.map {
            AppUsageInfo(it.packageName, it.totalTimeInForeground)
        }?.filter { it.totalTimeInForeground > 0 }?.sortedByDescending { it.totalTimeInForeground } ?: emptyList()
    }
}