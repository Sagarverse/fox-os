package com.example.foxos.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Returns animated pitch and roll offsets for parallax effects.
 * Uses rotation vector sensor for smoother, more responsive movement.
 */
@Composable
fun rememberParallaxOffsets(maxOffset: Float = 30f): State<androidx.compose.ui.geometry.Offset> {
    val context = LocalContext.current
    val offset = remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // Prefer rotation vector sensor for smoother results
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        // Fallback to gyroscope if rotation vector not available
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        // Additional fallback to accelerometer
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private val smoothingFactor = 0.15f // Lower = smoother, higher = more responsive
            
            override fun onSensorChanged(event: SensorEvent) {
                var newX = 0f
                var newY = 0f
                
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        
                        // pitch and roll in radians, convert to degrees
                        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                        
                        // More sensitive mapping for parallax effect
                        newX = (roll / 25f).coerceIn(-1f, 1f) * maxOffset
                        newY = (pitch / 25f).coerceIn(-1f, 1f) * maxOffset
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        // Gyroscope gives angular velocity, integrate for position
                        newX = lastX + event.values[1] * 2f
                        newY = lastY + event.values[0] * 2f
                        newX = newX.coerceIn(-maxOffset, maxOffset)
                        newY = newY.coerceIn(-maxOffset, maxOffset)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        // Direct mapping from accelerometer
                        newX = (event.values[0] / 5f).coerceIn(-1f, 1f) * maxOffset
                        newY = (event.values[1] / 5f).coerceIn(-1f, 1f) * maxOffset
                    }
                }
                
                // Exponential smoothing for fluid movement
                lastX = lastX + (newX - lastX) * smoothingFactor
                lastY = lastY + (newY - lastY) * smoothingFactor
                
                offset.value = androidx.compose.ui.geometry.Offset(x = lastX, y = lastY)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        // Register with fastest delay for smooth parallax
        val sensor = rotationVectorSensor ?: gyroscope ?: accelerometer
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return offset
}
