package com.example.foxos.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaInfo(
    val title: String = "Not Playing",
    val artist: String = "",
    val isPlaying: Boolean = false
)

class MediaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _mediaInfo = MutableStateFlow(MediaInfo())
    val mediaInfo: StateFlow<MediaInfo> = _mediaInfo.asStateFlow()
    
    private var mediaSessionManager: MediaSessionManager? = null
    
    init {
        try {
            mediaSessionManager = application.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        } catch (e: Exception) {
            // Service not available
        }
        startPolling()
    }
    
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                updateMediaInfo()
                delay(2000) // Poll every 2 seconds
            }
        }
    }
    
    private fun updateMediaInfo() {
        try {
            val context = getApplication<Application>()
            val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            
            // Try to get active sessions (requires notification listener permission)
            val controllers = try {
                mediaSessionManager?.getActiveSessions(
                    ComponentName(context, MediaNotificationListener::class.java)
                )
            } catch (e: SecurityException) {
                // Permission not granted - notification listener not enabled
                null
            }
            
            if (!controllers.isNullOrEmpty()) {
                val controller = controllers.first()
                val metadata = controller.metadata
                val playbackState = controller.playbackState
                
                val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                val isPlaying = playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
                
                _mediaInfo.value = MediaInfo(
                    title = title ?: "Unknown Track",
                    artist = artist ?: "Unknown Artist",
                    isPlaying = isPlaying
                )
            } else {
                _mediaInfo.value = MediaInfo()
            }
        } catch (e: Exception) {
            // Fallback if anything fails
            _mediaInfo.value = MediaInfo()
        }
    }
}

// Empty NotificationListenerService - must be declared in manifest
class MediaNotificationListener : NotificationListenerService()
