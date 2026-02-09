package com.example.foxos.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.OTHER
)

enum class AppCategory {
    STUDY, SOCIAL, TOOLS, GAMES, OTHER
}