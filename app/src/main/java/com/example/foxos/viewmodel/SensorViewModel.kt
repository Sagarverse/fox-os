package com.example.foxos.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TiltAngles(val x: Float = 0f, val y: Float = 0f)

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Use accelerometer - most reliable and direct for device tilt
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltAngles = MutableStateFlow(TiltAngles())
    val tiltAngles: StateFlow<TiltAngles> = _tiltAngles.asStateFlow()

    private var smoothedX = 0f
    private var smoothedY = 0f
    private val alpha = 0.15f // Smoothing factor
    
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return
            
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Accelerometer values: x = left/right tilt, y = forward/back tilt
                // Values roughly -10 to +10 (m/s²), with 9.8 being gravity
                val rawX = event.values[0] // Positive = tilt left
                val rawY = event.values[1] // Positive = tilt back
                
                // Apply low-pass filter for smooth movement
                smoothedX = smoothedX + alpha * (rawX - smoothedX)
                smoothedY = smoothedY + alpha * (rawY - smoothedY)
                
                // Scale up for visible effect (accelerometer returns ~0-10)
                val tiltX = (smoothedX * 8f).coerceIn(-80f, 80f)
                val tiltY = (smoothedY * 8f).coerceIn(-80f, 80f)
                
                _tiltAngles.value = TiltAngles(x = tiltX, y = tiltY)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            Log.d("SensorViewModel", "Accelerometer registered successfully")
        } else {
            Log.e("SensorViewModel", "No accelerometer available!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(sensorListener)
    }
}
