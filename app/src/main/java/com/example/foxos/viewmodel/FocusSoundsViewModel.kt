package com.example.foxos.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AmbientSound(val label: String) {
    NONE("None"),
    RAIN("Rain"),
    WAVES("Waves"),
    LOFI("Lo-Fi Beats"),
    FOREST("Forest")
}

class FocusSoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentSound = MutableStateFlow(AmbientSound.NONE)
    val currentSound: StateFlow<AmbientSound> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // In a real app, we would have raw resource files for these.
    // For now, we simulate the logic.
    private var mediaPlayer: MediaPlayer? = null

    fun toggleSound(sound: AmbientSound) {
        if (_currentSound.value == sound) {
            if (_isPlaying.value) {
                pause()
            } else {
                play()
            }
        } else {
            _currentSound.value = sound
            play()
        }
    }

    private fun play() {
        _isPlaying.value = true
        // Logic to start MediaPlayer with specific sound resource would go here
    }

    private fun pause() {
        _isPlaying.value = false
        // Logic to pause MediaPlayer
    }

    fun setVolume(value: Float) {
        _volume.value = value
        mediaPlayer?.setVolume(value, value)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}