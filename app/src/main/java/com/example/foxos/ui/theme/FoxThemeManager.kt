package com.example.foxos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalFoxColors = staticCompositionLocalOf { OrangeBlackTheme }

object FoxLauncherTheme {
    val colors: FoxThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFoxColors.current
}

@Composable
fun FoxThemeWrapper(
    colors: FoxThemeColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalFoxColors provides colors) {
        content()
    }
}
