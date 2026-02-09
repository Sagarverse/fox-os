package com.example.foxos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
    }

    val gridColumns: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GRID_COLUMNS] ?: 4
    }

    val showLabels: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_LABELS] ?: true
    }

    val assistantName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ASSISTANT_NAME] ?: "Siri"
    }

    suspend fun updateGridColumns(columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_COLUMNS] = columns
        }
    }

    suspend fun updateShowLabels(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_LABELS] = show
        }
    }

    suspend fun updateAssistantName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ASSISTANT_NAME] = name
        }
    }
}