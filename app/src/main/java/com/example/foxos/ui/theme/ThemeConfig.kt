package com.example.foxos.ui.theme

import androidx.compose.ui.graphics.Color

enum class Theme {
    ORANGE_BLACK, CYBERPUNK, HARMONY_OS, MINIMALIST
}

data class FoxThemeColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val accent: Color,
    val isLight: Boolean
)

val OrangeBlackTheme = FoxThemeColors(
    primary = Color(0xFFFF8F00),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFFAFAFA),
    accent = Color(0xFFFF8F00),
    isLight = false
)

val CyberpunkTheme = FoxThemeColors(
    primary = Color(0xFF00FBFF),
    background = Color(0xFF0D0221),
    surface = Color(0xFF1A1C2E),
    onSurface = Color(0xFFE6E6FA),
    accent = Color(0xFFFE019A),
    isLight = false
)

val HarmonyOSTheme = FoxThemeColors(
    primary = Color(0xFF007DFF), // Huawei Blue
    background = Color(0xFFF5F5F7), // Clean off-white
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    accent = Color(0xFF007DFF),
    isLight = true
)

val MinimalistTheme = FoxThemeColors(
    primary = Color(0xFF1A1A1A),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF2F2F7),
    onSurface = Color(0xFF000000),
    accent = Color(0xFF007AFF),
    isLight = true
)
