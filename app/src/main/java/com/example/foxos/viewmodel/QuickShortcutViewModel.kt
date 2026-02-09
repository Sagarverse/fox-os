package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.AppDatabase
import com.example.foxos.model.QuickShortcut
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuickShortcutViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).quickShortcutDao()

    val shortcuts: StateFlow<List<QuickShortcut>> = dao.getShortcuts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setShortcut(position: Int, packageName: String) {
        viewModelScope.launch {
            dao.setShortcut(QuickShortcut(position, packageName))
        }
    }

    fun clearShortcut(position: Int) {
        viewModelScope.launch {
            dao.clearShortcut(position)
        }
    }
}