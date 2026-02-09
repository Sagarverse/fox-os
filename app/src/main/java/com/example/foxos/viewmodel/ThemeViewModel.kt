package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.ThemeRepository
import com.example.foxos.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ThemeRepository(application)

    val theme: StateFlow<Theme> = repository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme.HARMONY_OS)

    val themeColors: StateFlow<FoxThemeColors> = theme.map {
        when (it) {
            Theme.ORANGE_BLACK -> OrangeBlackTheme
            Theme.CYBERPUNK -> CyberpunkTheme
            Theme.HARMONY_OS -> HarmonyOSTheme
            Theme.MINIMALIST -> MinimalistTheme
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HarmonyOSTheme)

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }
}