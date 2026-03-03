package com.example.foxos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dueDate: Long, // timestamp
    val isCompleted: Boolean = false,
    val isUrgent: Boolean = false,
    val notes: String = ""
)
