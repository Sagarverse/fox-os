package com.example.foxos.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.foxos.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeRepository(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("selected_theme")
        private val PRIMARY_COLOR_KEY = longPreferencesKey("primary_color")
    }

    val selectedTheme: Flow<Theme> = context.dataStore.data.map {
        Theme.valueOf(it[THEME_KEY] ?: Theme.HARMONY_OS.name)
    }

    val primaryColor: Flow<Long?> = context.dataStore.data.map {
        it[PRIMARY_COLOR_KEY]
    }

    suspend fun setTheme(theme: Theme) {
        context.dataStore.edit {
            it[THEME_KEY] = theme.name
        }
    }

    suspend fun setPrimaryColor(colorLong: Long) {
        context.dataStore.edit {
            it[PRIMARY_COLOR_KEY] = colorLong
        }
    }
}