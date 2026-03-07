package com.example.foxos.ui.theme

import androidx.compose.ui.graphics.Color

enum class Theme {
    ORANGE_BLACK, CYBERPUNK, HARMONY_OS, MINIMALIST, DYNAMIC, AR_CAMERA
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
    accent = Color(0xFFFFAB40),
    isLight = false
)

val CyberpunkTheme = FoxThemeColors(
    primary = NeonCyan,
    background = DeepSpaceBlue,
    surface = Color(0xFF16213E),
    onSurface = Color(0xFFE6E6FA),
    accent = ElectricViolet,
    isLight = false
)

val HarmonyOSTheme = FoxThemeColors(
    primary = FoxHarmonyBlue,
    background = Color(0xFFF5F5F7),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1D1F),
    accent = FoxHarmonyBlue,
    isLight = true
)

val MinimalistTheme = FoxThemeColors(
    primary = Color(0xFF1D1D1F),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF2F2F7),
    onSurface = Color(0xFF1D1D1F),
    accent = Color(0xFF007AFF),
    isLight = true
)

val ArCameraTheme = FoxThemeColors(
    primary = Color(0xFFFFFFFF),
    background = Color.Transparent,
    surface = Color(0x4D000000),
    onSurface = Color(0xFFFFFFFF),
    accent = NeonCyan,
    isLight = false
)
