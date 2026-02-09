package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.LauncherRepository
import com.example.foxos.data.UsageRepository
import com.example.foxos.model.AppCategory
import com.example.foxos.model.AppInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)
    private val usageRepository = UsageRepository(application)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isStudyModeActive = MutableStateFlow(false)
    val isStudyModeActive: StateFlow<Boolean> = _isStudyModeActive.asStateFlow()

    // AI Suggestions based on usage and time
    val suggestedApps: StateFlow<List<AppInfo>> = combine(_allApps, _isStudyModeActive) { apps, studyMode ->
        val usageStats = usageRepository.getAppUsageStats().take(5).map { it.packageName }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        apps.filter { app ->
            if (studyMode) app.category == AppCategory.STUDY
            else usageStats.contains(app.packageName) || isTimeRelevant(app, hour)
        }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredApps: StateFlow<List<AppInfo>> = combine(_allApps, _searchQuery, _isStudyModeActive) { apps, query, studyMode ->
        var result = if (studyMode) {
            apps.filter { it.category == AppCategory.STUDY }
        } else {
            apps
        }
        
        if (query.isNotEmpty()) {
            result = result.filter { it.label.contains(query, ignoreCase = true) }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshApps()
    }

    private fun isTimeRelevant(app: AppInfo, hour: Int): Boolean {
        return when {
            hour in 8..17 && app.category == AppCategory.STUDY -> true
            hour > 20 && app.category == AppCategory.SOCIAL -> true
            else -> false
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _allApps.value = repository.getInstalledApps()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun toggleStudyMode() {
        _isStudyModeActive.value = !_isStudyModeActive.value
    }

    fun launchApp(packageName: String) {
        repository.launchApp(packageName)
    }
}