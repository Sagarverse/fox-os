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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import android.app.WallpaperManager
import android.os.Build
import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ThemeRepository(application)

    private val _dynamicColors = MutableStateFlow<FoxThemeColors?>(null)

    val theme: StateFlow<Theme> = repository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme.HARMONY_OS)

    val themeColors: StateFlow<FoxThemeColors> = combine(theme, _dynamicColors) { selectedTheme, dynamic ->
        when (selectedTheme) {
            Theme.ORANGE_BLACK -> OrangeBlackTheme
            Theme.CYBERPUNK -> CyberpunkTheme
            Theme.HARMONY_OS -> HarmonyOSTheme
            Theme.MINIMALIST -> MinimalistTheme
            Theme.DYNAMIC -> dynamic ?: HarmonyOSTheme
            Theme.AR_CAMERA -> ArCameraTheme
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HarmonyOSTheme)

    init {
        updateDynamicColors()
    }

    @SuppressLint("MissingPermission")
    fun updateDynamicColors() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(getApplication())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val colors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                if (colors != null) {
                    val primaryColor = Color(colors.primaryColor.toArgb())
                    val secondaryColor = colors.secondaryColor?.toArgb()?.let { Color(it) } ?: primaryColor
                    val tertiaryColor = colors.tertiaryColor?.toArgb()?.let { Color(it) } ?: primaryColor
                    
                    _dynamicColors.value = FoxThemeColors(
                        primary = primaryColor,
                        background = Color(0xFF1E1E1E), // Dark aesthetic
                        surface = Color(0xFF2A2A2A),
                        onSurface = Color.White,
                        accent = secondaryColor,
                        isLight = false
                    )
                }
            }
        } catch (e: SecurityException) {
            // Ignore if no permission
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            repository.setTheme(theme)
            if (theme == Theme.DYNAMIC) {
                updateDynamicColors()
            }
        }
    }
}