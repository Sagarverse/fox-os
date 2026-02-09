package com.example.foxos.viewmodel

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ControlCenterViewModel(application: Application) : AndroidViewModel(application) {
    private val wifiManager = application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isWifiEnabled = MutableStateFlow(wifiManager.isWifiEnabled)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled ?: false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _volume = MutableStateFlow(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _brightness = MutableStateFlow(0.5f) // Simulated
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    fun toggleWifi() { _isWifiEnabled.value = !_isWifiEnabled.value }
    fun toggleBluetooth() { _isBluetoothEnabled.value = !_isBluetoothEnabled.value }
    fun toggleDnd() { _isDndEnabled.value = !_isDndEnabled.value }

    fun setVolume(value: Float) {
        _volume.value = value
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (value * maxVolume).toInt(), 0)
    }

    fun setBrightness(value: Float) {
        _brightness.value = value
        // Actual brightness requires Settings.System.putInt and WRITE_SETTINGS permission
    }
}