package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class UserContext {
    HOME, WORK, COMMUTING, SLEEPING
}

class ContextViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentContext = MutableStateFlow(UserContext.HOME)
    val currentContext: StateFlow<UserContext> = _currentContext.asStateFlow()

    init {
        startContextMonitoring()
    }

    private fun startContextMonitoring() {
        viewModelScope.launch {
            while (true) {
                updateContextBasedOnTime()
                delay(60000) // Check every minute
            }
        }
    }

    private fun updateContextBasedOnTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        _currentContext.value = when {
            hour in 23..24 || hour in 0..6 -> UserContext.SLEEPING
            !isWeekend && hour in 7..8 -> UserContext.COMMUTING
            !isWeekend && hour in 9..17 -> UserContext.WORK
            !isWeekend && hour in 17..18 -> UserContext.COMMUTING
            else -> UserContext.HOME
        }
    }
    
    // For testing/demo purposes
    fun overrideContext(context: UserContext) {
        _currentContext.value = context
    }
}
