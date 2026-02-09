package com.example.foxos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FoxOSTheme(
    colors: FoxThemeColors,
    content: @Composable () -> Unit
) {
    val darkTheme = colors.background.luminance() < 0.5f
    
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            background = colors.background,
            surface = colors.surface,
            onPrimary = Color.White,
            onBackground = colors.onSurface,
            onSurface = colors.onSurface
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            background = colors.background,
            surface = colors.surface,
            onPrimary = Color.Black,
            onBackground = colors.onSurface,
            onSurface = colors.onSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}
