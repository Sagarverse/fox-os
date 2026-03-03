package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.LauncherRepository
import com.example.foxos.data.SettingsRepository
import com.example.foxos.model.AppInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class LaunchEvent {
    data class DirectLaunch(val packageName: String) : LaunchEvent()
    data class RequireBiometric(val packageName: String) : LaunchEvent()
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LauncherRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _launchEvents = MutableSharedFlow<LaunchEvent>()
    val launchEvents: SharedFlow<LaunchEvent> = _launchEvents.asSharedFlow()

    val lockedApps: StateFlow<Set<String>> = settingsRepository.lockedApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val hiddenApps: StateFlow<Set<String>> = settingsRepository.hiddenApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val allApps: StateFlow<List<AppInfo>> = repository.getInstalledApps().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Apps visible in drawer (excluding hidden ones)
    val visibleApps: StateFlow<List<AppInfo>> = combine(allApps, hiddenApps) { apps, hidden ->
        apps.filter { it.packageName !in hidden }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val filteredApps: StateFlow<List<AppInfo>> = combine(visibleApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            val queryLower = query.lowercase()
            apps.filter { app ->
                // Match by label (name)
                app.label.lowercase().contains(queryLower) ||
                // Match by package name
                app.packageName.lowercase().contains(queryLower) ||
                // Fuzzy match - all letters appear in order
                fuzzyMatch(app.label.lowercase(), queryLower)
            }.sortedBy { app ->
                // Prioritize exact prefix matches
                when {
                    app.label.lowercase().startsWith(queryLower) -> 0
                    app.label.lowercase().contains(queryLower) -> 1
                    else -> 2
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Helper function for fuzzy matching (all query chars appear in order)
    private fun fuzzyMatch(text: String, query: String): Boolean {
        var queryIndex = 0
        for (char in text) {
            if (queryIndex < query.length && char == query[queryIndex]) {
                queryIndex++
            }
        }
        return queryIndex == query.length
    }

    private val topUsedPackages: StateFlow<Set<String>> = settingsRepository.topUsedApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val suggestedApps: StateFlow<List<AppInfo>> = combine(allApps, topUsedPackages) { apps, topPkgs ->
        if (topPkgs.isNotEmpty()) {
            // Real neural usage order: sort installed apps by known top packages first
            val topApps = apps.filter { it.packageName in topPkgs }
            val remainder = apps.filter { it.packageName !in topPkgs }.take(8 - topApps.size)
            (topApps + remainder).take(8)
        } else {
            apps.take(8)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val predictedApps: StateFlow<Set<String>> = suggestedApps.map { apps ->
        // Simulate "AI Sentiment" by highlighting the first two suggested apps as "Highly likely"
        apps.take(2).map { it.packageName }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val isStudyModeActive: StateFlow<Boolean> = settingsRepository.isStudyModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            if (lockedApps.value.contains(packageName)) {
                _launchEvents.emit(LaunchEvent.RequireBiometric(packageName))
            } else {
                repository.launchApp(packageName)
                _launchEvents.emit(LaunchEvent.DirectLaunch(packageName))
            }
        }
    }

    fun launchAppDirectly(packageName: String) {
        repository.launchApp(packageName)
    }

    fun toggleAppLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleAppLock(packageName, isLocked)
        }
    }

    fun hideApp(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleAppHidden(packageName, true)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleAppHidden(packageName, false)
        }
    }

    fun toggleStudyMode() {
        viewModelScope.launch {
            settingsRepository.setStudyMode(!isStudyModeActive.value)
        }
    }
}