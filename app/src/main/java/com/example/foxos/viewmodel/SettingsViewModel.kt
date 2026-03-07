package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.SettingsRepository
import com.example.foxos.utils.GenerativeAIEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val geminiApiKey: StateFlow<String> = repository.geminiApiKey.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    val geminiModel: StateFlow<String> = repository.geminiModel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "gemini-1.5-flash"
    )

    private val _apiVerificationResult = MutableStateFlow<Result<String>?>(null)
    val apiVerificationResult: StateFlow<Result<String>?> = _apiVerificationResult.asStateFlow()

    private val _availableGeminiModels = MutableStateFlow<List<GenerativeAIEngine.ModelMetadata>>(emptyList())
    val availableGeminiModels: StateFlow<List<GenerativeAIEngine.ModelMetadata>> = _availableGeminiModels.asStateFlow()

    val hiddenApps: StateFlow<Set<String>> = repository.hiddenApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val gridColumns: StateFlow<Int> = repository.gridColumns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 4
    )

    val showLabels: StateFlow<Boolean> = repository.showLabels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val iconSize: StateFlow<Int> = repository.iconSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 56
    )

    val doubleTapToLock: StateFlow<Boolean> = repository.doubleTapToLock.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val hapticFeedback: StateFlow<Boolean> = repository.hapticFeedback.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val showClockOnHome: StateFlow<Boolean> = repository.showClockOnHome.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val assistantName: StateFlow<String> = repository.assistantName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "Fox"
    )

    val wallpaperId: StateFlow<String> = repository.wallpaperId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "pastel"
    )

    val wallpaperStyle: StateFlow<String> = repository.wallpaperStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "aurora"
    )

    val customWallpaperUri: StateFlow<String> = repository.customWallpaperUri.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    val homePageCount: StateFlow<Int> = repository.homePageCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 1
    )

    val iconShape: StateFlow<String> = repository.iconShape.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "rounded_square"
    )

    val sidebarApps: StateFlow<Set<String>> = repository.sidebarApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val dockApps: StateFlow<Set<String>> = repository.dockApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val homeScreenApps: StateFlow<Set<String>> = repository.homeScreenApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val isMinimalisticMode: StateFlow<Boolean> = repository.isMinimalisticMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        // Automatically fetch models if API key exists
        viewModelScope.launch {
            repository.geminiApiKey.collect { key ->
                if (key.isNotBlank()) {
                    fetchModels()
                }
            }
        }
    }

    fun updateGeminiApiKey(key: String) {
        _apiVerificationResult.value = null
        if (key.isNotBlank()) {
            GenerativeAIEngine.setApiKey(key, geminiModel.value)
        }
        viewModelScope.launch {
            repository.updateGeminiApiKey(key)
        }
    }

    fun updateGeminiModel(model: String) {
        resetVerification()
        if (geminiApiKey.value.isNotBlank()) {
            GenerativeAIEngine.setApiKey(geminiApiKey.value, model)
        }
        viewModelScope.launch {
            repository.updateGeminiModel(model)
        }
    }

    fun resetVerification() {
        _apiVerificationResult.value = null
    }

    fun verifyApiKey(key: String = geminiApiKey.value, model: String? = null) {
        viewModelScope.launch {
            val targetModel = model ?: geminiModel.value
            val result = GenerativeAIEngine.verifyApiKey(key, targetModel)
            _apiVerificationResult.value = result
            if (result.isSuccess) {
                fetchModels()
            }
        }
    }

    fun fetchModels() {
        viewModelScope.launch {
            val result = GenerativeAIEngine.fetchAvailableModels(geminiApiKey.value)
            if (result.isSuccess) {
                _availableGeminiModels.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            repository.toggleAppHidden(packageName, false)
        }
    }

    fun updateGridColumns(columns: Int) {
        viewModelScope.launch {
            repository.updateGridColumns(columns)
        }
    }

    fun updateShowLabels(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowLabels(show)
        }
    }

    fun updateIconSize(size: Int) {
        viewModelScope.launch {
            repository.updateIconSize(size)
        }
    }

    fun updateDoubleTapToLock(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDoubleTapToLock(enabled)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticFeedback(enabled)
        }
    }

    fun updateShowClockOnHome(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowClockOnHome(show)
        }
    }

    fun updateAssistantName(name: String) {
        viewModelScope.launch {
            repository.updateAssistantName(name)
        }
    }

    fun updateWallpaperId(id: String) {
        viewModelScope.launch {
            repository.updateWallpaperId(id)
        }
    }

    fun updateWallpaperStyle(style: String) {
        viewModelScope.launch {
            repository.updateWallpaperStyle(style)
        }
    }

    fun updateCustomWallpaperUri(uri: String) {
        viewModelScope.launch {
            repository.updateCustomWallpaperUri(uri)
            repository.updateWallpaperId("custom")
        }
    }

    fun updateHomePageCount(count: Int) {
        viewModelScope.launch {
            repository.updateHomePageCount(count)
        }
    }

    fun updateIconShape(shape: String) {
        viewModelScope.launch {
            repository.updateIconShape(shape)
        }
    }

    fun updateSidebarApps(packages: Set<String>) {
        viewModelScope.launch {
            repository.updateSidebarApps(packages)
        }
    }

    fun updateDockApps(packages: Set<String>) {
        viewModelScope.launch {
            repository.updateDockApps(packages)
        }
    }

    fun updateHomeScreenApps(packages: Set<String>) {
        viewModelScope.launch {
            repository.updateHomeScreenApps(packages)
        }
    }

    fun toggleMinimalisticMode() {
        viewModelScope.launch {
            repository.setMinimalisticMode(!isMinimalisticMode.value)
        }
    }

    fun setMinimalisticMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMinimalisticMode(enabled)
        }
    }
}
