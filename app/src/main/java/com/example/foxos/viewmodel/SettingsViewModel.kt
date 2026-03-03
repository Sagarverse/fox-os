package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.SettingsRepository
import com.example.foxos.utils.GenerativeAIEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _gridColumns = MutableStateFlow(4)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _showLabels = MutableStateFlow(true)
    val showLabels: StateFlow<Boolean> = _showLabels.asStateFlow()

    private val _iconSize = MutableStateFlow(56)
    val iconSize: StateFlow<Int> = _iconSize.asStateFlow()

    private val _doubleTapToLock = MutableStateFlow(true)
    val doubleTapToLock: StateFlow<Boolean> = _doubleTapToLock.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(true)
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _showClockOnHome = MutableStateFlow(true)
    val showClockOnHome: StateFlow<Boolean> = _showClockOnHome.asStateFlow()

    private val _assistantName = MutableStateFlow("Fox")
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _wallpaperId = MutableStateFlow("pastel")
    val wallpaperId: StateFlow<String> = _wallpaperId.asStateFlow()

    private val _customWallpaperUri = MutableStateFlow("")
    val customWallpaperUri: StateFlow<String> = _customWallpaperUri.asStateFlow()

    private val _homePageCount = MutableStateFlow(1)
    val homePageCount: StateFlow<Int> = _homePageCount.asStateFlow()

    private val _iconShape = MutableStateFlow("rounded_square")
    val iconShape: StateFlow<String> = _iconShape.asStateFlow()

    private val _sidebarApps = MutableStateFlow<Set<String>>(emptySet())
    val sidebarApps: StateFlow<Set<String>> = _sidebarApps.asStateFlow()

    private val _dockApps = MutableStateFlow<Set<String>>(emptySet())
    val dockApps: StateFlow<Set<String>> = _dockApps.asStateFlow()

    private val _homeScreenApps = MutableStateFlow<Set<String>>(emptySet())
    val homeScreenApps: StateFlow<Set<String>> = _homeScreenApps.asStateFlow()

    init {
        viewModelScope.launch {
            _geminiApiKey.value = repository.geminiApiKey.first()
            _gridColumns.value = repository.gridColumns.first()
            _showLabels.value = repository.showLabels.first()
            _iconSize.value = repository.iconSize.first()
            _doubleTapToLock.value = repository.doubleTapToLock.first()
            _hapticFeedback.value = repository.hapticFeedback.first()
            _showClockOnHome.value = repository.showClockOnHome.first()
            _assistantName.value = repository.assistantName.first()
            _wallpaperId.value = repository.wallpaperId.first()
            _customWallpaperUri.value = repository.customWallpaperUri.first()
            _homePageCount.value = repository.homePageCount.first()
            _iconShape.value = repository.iconShape.first()
            _sidebarApps.value = repository.sidebarApps.first()
            _dockApps.value = repository.dockApps.first()
            _homeScreenApps.value = repository.homeScreenApps.first()
        }
    }

    fun updateGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        // Immediately set the API key in the engine
        if (key.isNotBlank()) {
            GenerativeAIEngine.setApiKey(key)
        }
        viewModelScope.launch {
            repository.updateGeminiApiKey(key)
        }
    }

    fun updateGridColumns(columns: Int) {
        _gridColumns.value = columns
        viewModelScope.launch {
            repository.updateGridColumns(columns)
        }
    }

    fun updateShowLabels(show: Boolean) {
        _showLabels.value = show
        viewModelScope.launch {
            repository.updateShowLabels(show)
        }
    }

    fun updateIconSize(size: Int) {
        _iconSize.value = size
        viewModelScope.launch {
            repository.updateIconSize(size)
        }
    }

    fun updateDoubleTapToLock(enabled: Boolean) {
        _doubleTapToLock.value = enabled
        viewModelScope.launch {
            repository.updateDoubleTapToLock(enabled)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
        viewModelScope.launch {
            repository.updateHapticFeedback(enabled)
        }
    }

    fun updateShowClockOnHome(show: Boolean) {
        _showClockOnHome.value = show
        viewModelScope.launch {
            repository.updateShowClockOnHome(show)
        }
    }

    fun updateAssistantName(name: String) {
        _assistantName.value = name
        viewModelScope.launch {
            repository.updateAssistantName(name)
        }
    }

    fun updateWallpaperId(id: String) {
        _wallpaperId.value = id
        viewModelScope.launch {
            repository.updateWallpaperId(id)
        }
    }

    fun updateCustomWallpaperUri(uri: String) {
        _customWallpaperUri.value = uri
        _wallpaperId.value = "custom"
        viewModelScope.launch {
            repository.updateCustomWallpaperUri(uri)
            repository.updateWallpaperId("custom")
        }
    }

    fun updateHomePageCount(count: Int) {
        _homePageCount.value = count.coerceIn(1, 7)
        viewModelScope.launch {
            repository.updateHomePageCount(count)
        }
    }

    fun updateIconShape(shape: String) {
        _iconShape.value = shape
        viewModelScope.launch {
            repository.updateIconShape(shape)
        }
    }

    fun updateSidebarApps(packages: Set<String>) {
        _sidebarApps.value = packages
        viewModelScope.launch {
            repository.updateSidebarApps(packages)
        }
    }

    fun updateDockApps(packages: Set<String>) {
        _dockApps.value = packages
        viewModelScope.launch {
            repository.updateDockApps(packages)
        }
    }

    fun updateHomeScreenApps(packages: Set<String>) {
        _homeScreenApps.value = packages
        viewModelScope.launch {
            repository.updateHomeScreenApps(packages)
        }
    }
}
