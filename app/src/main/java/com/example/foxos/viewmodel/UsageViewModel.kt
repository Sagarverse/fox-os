package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.AppUsageInfo
import com.example.foxos.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsageRepository(application)

    private val _usageStats = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val usageStats: StateFlow<List<AppUsageInfo>> = _usageStats.asStateFlow()

    init {
        refreshUsageStats()
    }

    fun refreshUsageStats() {
        viewModelScope.launch {
            _usageStats.value = repository.getAppUsageStats()
        }
    }
}