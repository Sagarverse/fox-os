package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.AppDatabase
import com.example.foxos.model.CustomGesture
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

class GestureViewModel(application: Application) : AndroidViewModel(application) {
    private val gestureDao = AppDatabase.getDatabase(application).gestureDao()

    val allGestures: StateFlow<List<CustomGesture>> = gestureDao.getAllGestures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveGesture(name: String, action: String, points: List<Offset>) {
        val pointsString = points.joinToString(";") { "${it.x},${it.y}" }
        viewModelScope.launch {
            gestureDao.insertGesture(CustomGesture(name = name, action = action, pathPoints = pointsString))
        }
    }

    fun deleteGesture(gesture: CustomGesture) {
        viewModelScope.launch {
            gestureDao.deleteGesture(gesture)
        }
    }

    fun recognizeGesture(drawnPoints: List<Offset>): String? {
        if (drawnPoints.size < 5) return null
        
        val savedGestures = allGestures.value
        for (gesture in savedGestures) {
            val savedPoints = gesture.pathPoints.split(";").map {
                val coords = it.split(",")
                Offset(coords[0].toFloat(), coords[1].toFloat())
            }
            
            if (isMatch(drawnPoints, savedPoints)) {
                return gesture.action
            }
        }
        return null
    }

    private fun isMatch(drawn: List<Offset>, saved: List<Offset>): Boolean {
        // Simplified Procrustes-like matching or basic point-by-point distance check
        // For a launcher, we normalize and compare bounding box relative points
        if (drawn.isEmpty() || saved.isEmpty()) return false
        
        // Basic heuristic: check if start, end, and middle points are roughly in the same quadrant
        val dStart = drawn.first()
        val dEnd = drawn.last()
        val sStart = saved.first()
        val sEnd = saved.last()

        val distStart = (dStart - sStart).getDistance()
        val distEnd = (dEnd - sEnd).getDistance()

        // Very loose matching for demo; in production use a gesture recognition library
        return distStart < 300 && distEnd < 300 
    }
}