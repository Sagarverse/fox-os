package com.example.foxos.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ControlAction {
    data class OpenSettings(val intent: Intent) : ControlAction()
    object None : ControlAction()
}

class ControlCenterViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _isFlashlightEnabled = MutableStateFlow(false)
    val isFlashlightEnabled: StateFlow<Boolean> = _isFlashlightEnabled.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(wifiManager.isWifiEnabled)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled ?: false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _isAutoRotateEnabled = MutableStateFlow(getAutoRotateState())
    val isAutoRotateEnabled: StateFlow<Boolean> = _isAutoRotateEnabled.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(getLocationState())
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private val _isDesktopModeEnabled = MutableStateFlow(false)
    val isDesktopModeEnabled: StateFlow<Boolean> = _isDesktopModeEnabled.asStateFlow()

    private val _volume = MutableStateFlow(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _brightness = MutableStateFlow(getCurrentBrightness())
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _pendingAction = MutableStateFlow<ControlAction>(ControlAction.None)
    val pendingAction: StateFlow<ControlAction> = _pendingAction.asStateFlow()

    fun clearPendingAction() {
        _pendingAction.value = ControlAction.None
    }

    fun refreshStates() {
        _isWifiEnabled.value = wifiManager.isWifiEnabled
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled ?: false
        _isDndEnabled.value = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
        _isAutoRotateEnabled.value = getAutoRotateState()
        _isLocationEnabled.value = getLocationState()
        _brightness.value = getCurrentBrightness()
    }

    private fun getAutoRotateState(): Boolean {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocationState(): Boolean {
        return try {
            val mode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            mode != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentBrightness(): Float {
        return try {
            val brightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            brightness / 255f
        } catch (e: Exception) {
            0.5f
        }
    }

    fun toggleWifi() {
        // On Android 10+, apps can't directly toggle WiFi - open settings panel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _pendingAction.value = ControlAction.OpenSettings(intent)
        } else {
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
            _isWifiEnabled.value = wifiManager.isWifiEnabled
        }
    }

    fun toggleBluetooth() {
        // On Android 12+, need BLUETOOTH_CONNECT permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _pendingAction.value = ControlAction.OpenSettings(intent)
        } else {
            try {
                if (bluetoothAdapter?.isEnabled == true) {
                    @Suppress("DEPRECATION", "MissingPermission")
                    bluetoothAdapter.disable()
                } else {
                    @Suppress("DEPRECATION", "MissingPermission")
                    bluetoothAdapter?.enable()
                }
                _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled ?: false
            } catch (e: SecurityException) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                _pendingAction.value = ControlAction.OpenSettings(intent)
            }
        }
    }

    fun toggleDnd() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            // Need to request DND access
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _pendingAction.value = ControlAction.OpenSettings(intent)
            return
        }

        val currentFilter = notificationManager.currentInterruptionFilter
        if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            // Enable DND (silence all)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            _isDndEnabled.value = true
        } else {
            // Disable DND
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            _isDndEnabled.value = false
        }
    }

    fun toggleDesktopMode() {
        _isDesktopModeEnabled.value = !_isDesktopModeEnabled.value
    }

    fun setVolume(value: Float) {
        _volume.value = value
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (value * maxVolume).toInt(), 0)
    }

    fun setBrightness(value: Float) {
        if (!Settings.System.canWrite(context)) {
            // Need WRITE_SETTINGS permission
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _pendingAction.value = ControlAction.OpenSettings(intent)
            return
        }

        _brightness.value = value
        val brightnessInt = (value * 255).toInt().coerceIn(1, 255)
        
        try {
            // Disable auto brightness first
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            // Set brightness
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessInt
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleFlashlight() {
        setFlashlight(!_isFlashlightEnabled.value)
    }

    fun setFlashlight(enabled: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            cameraId?.let {
                cameraManager.setTorchMode(it, enabled)
                _isFlashlightEnabled.value = enabled
            }
        } catch (e: Exception) {
            Log.e("ControlCenter", "Error toggling flashlight", e)
        }
    }

    fun openHotspotSettings() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        // Specific hotspot intent if available
        val hotspotIntent = Intent().apply {
            setClassName("com.android.settings", "com.android.settings.TetherSettings")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(hotspotIntent)
        } catch (e: Exception) {
            context.startActivity(intent)
        }
    }

    fun toggleAutoRotate() {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _pendingAction.value = ControlAction.OpenSettings(intent)
            return
        }

        val newValue = if (_isAutoRotateEnabled.value) 0 else 1
        try {
            Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, newValue)
            _isAutoRotateEnabled.value = newValue == 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        _pendingAction.value = ControlAction.OpenSettings(intent)
    }

    fun toggleAirplaneMode() {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        _pendingAction.value = ControlAction.OpenSettings(intent)
    }
    
    fun adjustVolume(increase: Boolean) {
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        _volume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun adjustBrightness(increase: Boolean) {
        val step = if (increase) 0.1f else -0.1f
        setBrightness((_brightness.value + step).coerceIn(0f, 1f))
    }

    /** Returns list of paired Bluetooth devices as (name, deviceClass string) pairs */
    @SuppressWarnings("MissingPermission")
    fun getPairedBluetoothDevices(): List<Pair<String, String>> {
        return try {
            bluetoothAdapter?.bondedDevices?.map { device ->
                val deviceClass = when (device.bluetoothClass?.majorDeviceClass) {
                    android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio"
                    android.bluetooth.BluetoothClass.Device.Major.COMPUTER -> "Computer"
                    android.bluetooth.BluetoothClass.Device.Major.PHONE -> "Phone"
                    android.bluetooth.BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
                    android.bluetooth.BluetoothClass.Device.Major.WEARABLE -> "Wearable"
                    else -> "Device"
                }
                (device.name ?: "Unknown Device") to deviceClass
            } ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /** Returns true if any music/audio stream is currently active */
    fun isMusicActive(): Boolean {
        return audioManager.isMusicActive
    }
}