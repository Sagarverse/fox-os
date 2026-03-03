package com.example.foxos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        val STUDY_MODE_ENABLED = booleanPreferencesKey("study_mode_enabled")
        val LOCKED_APPS = stringSetPreferencesKey("locked_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val TOP_USED_APPS = stringSetPreferencesKey("top_used_apps")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val ICON_SIZE = intPreferencesKey("icon_size")
        val DOUBLE_TAP_TO_LOCK = booleanPreferencesKey("double_tap_to_lock")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SHOW_CLOCK_ON_HOME = booleanPreferencesKey("show_clock_on_home")
        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val HOME_PAGE_COUNT = intPreferencesKey("home_page_count")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val FLOATING_NOTE_CONTENT = stringPreferencesKey("floating_note_content")
        val SIDEBAR_APPS = stringSetPreferencesKey("sidebar_apps")
        val DOCK_APPS = stringSetPreferencesKey("dock_apps")
        val HOME_SCREEN_APPS = stringSetPreferencesKey("home_screen_apps")
    }

    val gridColumns: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GRID_COLUMNS] ?: 4
    }

    val showLabels: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_LABELS] ?: true
    }

    val assistantName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ASSISTANT_NAME] ?: "Fox"
    }

    val isStudyModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.STUDY_MODE_ENABLED] ?: false
    }

    val lockedApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCKED_APPS] ?: emptySet()
    }

    val hiddenApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIDDEN_APPS] ?: emptySet()
    }

    val topUsedApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOP_USED_APPS] ?: emptySet()
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GEMINI_API_KEY] ?: ""
    }

    val iconSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ICON_SIZE] ?: 56
    }

    val doubleTapToLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] ?: true
    }

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
    }

    val showClockOnHome: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_CLOCK_ON_HOME] ?: true
    }

    val wallpaperId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WALLPAPER_ID] ?: "pastel"
    }

    val customWallpaperUri: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_WALLPAPER_URI] ?: ""
    }

    val homePageCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HOME_PAGE_COUNT] ?: 1
    }

    val iconShape: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ICON_SHAPE] ?: "rounded_square"
    }

    val floatingNoteContent: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FLOATING_NOTE_CONTENT] ?: ""
    }

    val sidebarApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SIDEBAR_APPS] ?: emptySet()
    }

    val dockApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DOCK_APPS] ?: emptySet()
    }

    val homeScreenApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HOME_SCREEN_APPS] ?: emptySet()
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

    suspend fun setStudyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STUDY_MODE_ENABLED] = enabled
        }
    }

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) {
        context.dataStore.edit { preferences ->
            val currentLocks = preferences[PreferencesKeys.LOCKED_APPS] ?: emptySet()
            val newLocks = currentLocks.toMutableSet()
            if (isLocked) {
                newLocks.add(packageName)
            } else {
                newLocks.remove(packageName)
            }
            preferences[PreferencesKeys.LOCKED_APPS] = newLocks
        }
    }

    suspend fun toggleAppHidden(packageName: String, isHidden: Boolean) {
        context.dataStore.edit { preferences ->
            val currentHidden = preferences[PreferencesKeys.HIDDEN_APPS] ?: emptySet()
            val newHidden = currentHidden.toMutableSet()
            if (isHidden) {
                newHidden.add(packageName)
            } else {
                newHidden.remove(packageName)
            }
            preferences[PreferencesKeys.HIDDEN_APPS] = newHidden
        }
    }

    suspend fun updateTopUsedApps(packages: Set<String>) {
        context.dataStore.edit { pref ->
            pref[PreferencesKeys.TOP_USED_APPS] = packages
        }
    }

    suspend fun updateGeminiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = apiKey
        }
    }

    suspend fun updateIconSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ICON_SIZE] = size
        }
    }

    suspend fun updateDoubleTapToLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = enabled
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun updateShowClockOnHome(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_CLOCK_ON_HOME] = show
        }
    }

    suspend fun updateWallpaperId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WALLPAPER_ID] = id
        }
    }

    suspend fun updateCustomWallpaperUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_WALLPAPER_URI] = uri
        }
    }

    suspend fun updateHomePageCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOME_PAGE_COUNT] = count.coerceIn(1, 7)
        }
    }

    suspend fun updateIconShape(shape: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ICON_SHAPE] = shape
        }
    }

    suspend fun updateFloatingNoteContent(content: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLOATING_NOTE_CONTENT] = content
        }
    }

    suspend fun updateSidebarApps(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIDEBAR_APPS] = packages
        }
    }

    suspend fun updateDockApps(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOCK_APPS] = packages
        }
    }

    suspend fun updateHomeScreenApps(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOME_SCREEN_APPS] = packages
        }
    }
}