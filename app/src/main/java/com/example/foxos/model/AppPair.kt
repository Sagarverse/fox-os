package com.example.foxos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_pairs")
data class AppPair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val package1: String,
    val package2: String
)