package com.example.foxos.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.foxos.model.AppCategory
import com.example.foxos.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LauncherRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        
        resolveInfos.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            
            val appCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    mapAndroidCategory(appInfo.category)
                } catch (e: Exception) {
                    AppCategory.OTHER
                }
            } else {
                guessCategory(packageName)
            }

            AppInfo(
                label = label,
                packageName = packageName,
                icon = icon,
                category = appCategory
            )
        }.sortedBy { it.label.lowercase() }
    }

    private fun mapAndroidCategory(category: Int): AppCategory {
        return when (category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_AUDIO, 
            ApplicationInfo.CATEGORY_VIDEO, 
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_MAPS, 
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.OTHER
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.STUDY
            else -> AppCategory.OTHER
        }
    }

    private fun guessCategory(packageName: String): AppCategory {
        val lowerCasePkg = packageName.lowercase()
        return when {
            lowerCasePkg.contains("education") || lowerCasePkg.contains("study") || lowerCasePkg.contains("notion") -> AppCategory.STUDY
            lowerCasePkg.contains("social") || lowerCasePkg.contains("messenger") || lowerCasePkg.contains("whatsapp") || lowerCasePkg.contains("instagram") -> AppCategory.SOCIAL
            lowerCasePkg.contains("tool") || lowerCasePkg.contains("calculator") || lowerCasePkg.contains("settings") -> AppCategory.TOOLS
            lowerCasePkg.contains("game") || lowerCasePkg.contains("pubg") || lowerCasePkg.contains("freefire") -> AppCategory.GAMES
            else -> AppCategory.OTHER
        }
    }
    
    fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
        }
    }
}
