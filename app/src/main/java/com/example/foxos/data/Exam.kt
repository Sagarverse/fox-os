package com.example.foxos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val examDate: Long, // timestamp
    val notes: String = ""
)
