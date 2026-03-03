package com.example.foxos.worker

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.foxos.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 24 * 60 * 60 * 1000L // last 24 hours

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            if (usageStatsList != null && usageStatsList.isNotEmpty()) {
                // Sort by total foreground time, take top 5 unique packages
                val topApps = usageStatsList
                    .filter { it.packageName != applicationContext.packageName }
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(5)
                    .map { it.packageName }
                    .toSet()

                // Persist to DataStore
                val settingsRepo = SettingsRepository(applicationContext)
                settingsRepo.updateTopUsedApps(topApps)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
