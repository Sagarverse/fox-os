package com.example.foxos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_shortcuts")
data class QuickShortcut(
    @PrimaryKey val position: Int, // 0 to N
    val packageName: String
)