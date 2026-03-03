package com.example.foxos.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import com.example.foxos.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class LauncherRepository(private val context: Context) {

    fun getInstalledApps(): Flow<List<AppInfo>> = flow {
        val apps = withContext(Dispatchers.IO) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
            
            val allApps = mutableListOf<AppInfo>()
            
            // Get apps for all profiles (Main, Work, etc.)
            val profiles = userManager.userProfiles
            for (profile in profiles) {
                val activityList = launcherApps.getActivityList(null, profile)
                for (activity in activityList) {
                    allApps.add(
                        AppInfo(
                            label = activity.label.toString(),
                            packageName = activity.applicationInfo.packageName,
                            icon = activity.getIcon(0)
                        )
                    )
                }
            }
            
            // Filter out FoxOS itself and sort by label
            allApps.filter { it.packageName != context.packageName }
                  .distinctBy { it.packageName }
                  .sortedBy { it.label }
        }
        emit(apps)
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
