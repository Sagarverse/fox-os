package com.example.foxos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_gestures")
data class CustomGesture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // Label for the gesture
    val action: String, // Action to trigger (e.g., "START_POMODORO", "OPEN_DRAWER")
    val pathPoints: String // Simplified string representation of the points (e.g., "x1,y1;x2,y2")
)

enum class GestureAction(val label: String) {
    START_POMODORO("Start Pomodoro"),
    TOGGLE_STUDY_MODE("Toggle Study Mode"),
    OPEN_DRAWER("Open App Drawer"),
    OPEN_TASKS("Open Tasks"),
    OPEN_FOCUS_DASHBOARD("Open Focus Dashboard")
}