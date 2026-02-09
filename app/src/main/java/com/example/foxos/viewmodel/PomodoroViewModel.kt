package com.example.foxos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    private val _timeLeft = MutableStateFlow(25 * 60) // 25 minutes in seconds
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak.asStateFlow()

    private var timerJob: Job? = null

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
        if (_isBreak.value) {
            _isBreak.value = false
            _timeLeft.value = 25 * 60
        } else {
            _isBreak.value = true
            _timeLeft.value = 5 * 60
        }
    }
}