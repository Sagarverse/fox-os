package com.example.foxos.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PomodoroEvent {
    data class TimerFinished(val wasBreak: Boolean) : PomodoroEvent()
}

data class PomodoroState(
    val timeLeft: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val totalCompletedSessions: Int = 0
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val _timeLeft = MutableStateFlow(25 * 60) // 25 minutes in seconds
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak.asStateFlow()
    
    private val _totalCompletedSessions = MutableStateFlow(0)
    val totalCompletedSessions: StateFlow<Int> = _totalCompletedSessions.asStateFlow()
    
    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<PomodoroEvent>()
    val events: SharedFlow<PomodoroEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    
    companion object {
        private const val CHANNEL_ID = "pomodoro_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Timer"
            val descriptionText = "Notifications for Pomodoro timer completion"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }
            onTimerFinished()
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _isBreak.value = false
        _timeLeft.value = 25 * 60
    }

    private fun onTimerFinished() {
        _isRunning.value = false
        val wasBreak = _isBreak.value
        
        // Increment completed sessions when work session finishes
        if (!wasBreak) {
            _totalCompletedSessions.value += 1
        }
        
        // Vibrate
        vibrateDevice()
        
        // Show notification
        showNotification(wasBreak)
        
        // Emit event
        viewModelScope.launch {
            _events.emit(PomodoroEvent.TimerFinished(wasBreak))
        }
        
        if (wasBreak) {
            _isBreak.value = false
            _timeLeft.value = 25 * 60
        } else {
            _isBreak.value = true
            _timeLeft.value = 5 * 60
        }
        
        updateState()
    }
    
    private fun updateState() {
        _state.value = PomodoroState(
            timeLeft = _timeLeft.value,
            isRunning = _isRunning.value,
            isBreak = _isBreak.value,
            totalCompletedSessions = _totalCompletedSessions.value
        )
    }
    
    private fun vibrateDevice() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }
    
    private fun showNotification(wasBreak: Boolean) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val title = if (wasBreak) "Break Over!" else "Focus Session Complete!"
        val message = if (wasBreak) "Time to get back to work." else "Great job! Time for a 5 minute break."
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}