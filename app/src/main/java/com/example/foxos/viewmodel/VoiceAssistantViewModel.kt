package com.example.foxos.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.foxos.data.LauncherRepository
import com.example.foxos.data.SettingsRepository
import com.example.foxos.utils.GenerativeAIEngine
import com.example.foxos.ai.FoxAIIntelligence
import com.example.foxos.ai.AssistantIntent
import com.example.foxos.ai.VoiceSpeaker
import android.media.AudioManager
import android.media.ToneGenerator
import com.example.foxos.viewmodel.ControlCenterViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.util.*

sealed class AssistantState {
    object Idle : AssistantState()
    object Listening : AssistantState()
    data class Processing(val text: String) : AssistantState()
    data class Generating(val partialText: String) : AssistantState()
    data class Success(val response: String, val category: String? = null) : AssistantState()
    data class Error(val message: String) : AssistantState()
    data class Timer(val totalSeconds: Int, val remainingSeconds: Int, val label: String = "Timer") : AssistantState()
}

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
    private val repository = LauncherRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val controlCenterViewModel = ControlCenterViewModel(application)
    private val weatherViewModel = WeatherViewModel()
    private val taskViewModel = TaskViewModel(application)
    private val noteViewModel = NoteViewModel(application)
    
    private val voiceSpeaker = VoiceSpeaker(application)
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()
    
    private var timerJob: Job? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("VoiceAssistant", "onReadyForSpeech")
            _state.value = AssistantState.Listening
        }
        override fun onBeginningOfSpeech() {
            Log.d("VoiceAssistant", "onBeginningOfSpeech")
        }
        override fun onRmsChanged(rmsdB: Float) {
            // Normalize: rmsdB is typically -2 to 10, map to 0..1
            _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            Log.d("VoiceAssistant", "onEndOfSpeech")
            _state.value = AssistantState.Processing("Analyzing...")
        }
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Please try again"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required. Please grant it in Settings."
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap to try again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Assistant busy, please wait"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap to try again."
                else -> "Something went wrong. Tap to try again."
            }
            Log.e("VoiceAssistant", "onError: $error - $errorMessage")
            _state.value = AssistantState.Error(errorMessage)
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            Log.d("VoiceAssistant", "onResults: $text")
            _recognizedText.value = text
            processCommand(text)
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                _recognizedText.value = text
            }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    init {
        speechRecognizer.setRecognitionListener(recognitionListener)
        // Continuously observe API key and model changes
        viewModelScope.launch {
            combine(
                settingsRepository.geminiApiKey,
                settingsRepository.geminiModel
            ) { apiKey, model -> apiKey to model }
                .collectLatest { (apiKey, model) ->
                    Log.d("VoiceAssistant", "Gemini config changed: Model=$model, KeyLength=${apiKey.length}")
                    if (apiKey.isNotBlank()) {
                        GenerativeAIEngine.setApiKey(apiKey, model)
                        Log.d("VoiceAssistant", "Gemini engine updated")
                    }
                }
        }
    }
    
    fun reloadApiKey() {
        viewModelScope.launch {
            try {
                val apiKey = settingsRepository.geminiApiKey.first()
                if (apiKey.isNotBlank()) {
                    GenerativeAIEngine.setApiKey(apiKey)
                    Log.d("VoiceAssistant", "API key reloaded, isApiKeySet: ${GenerativeAIEngine.isApiKeySet()}")
                }
            } catch (e: Exception) {
                Log.e("VoiceAssistant", "Error reloading API key", e)
            }
        }
    }

    fun startListening() {
        timerJob?.cancel()
        _recognizedText.value = ""
        voiceSpeaker.stop()
        
        // Play Siri-like listening chime
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        
        _state.value = AssistantState.Listening
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        viewModelScope.launch {
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e("VoiceAssistant", "startListening error", e)
                _state.value = AssistantState.Error("Could not start recognition")
            }
        }
    }

    fun resetAndListen() {
        speechRecognizer.stopListening()
        speechRecognizer.cancel()
        timerJob?.cancel()
        voiceSpeaker.stop()
        _recognizedText.value = ""
        _state.value = AssistantState.Idle
        // Small delay to ensure previous instance is cleaned up
        viewModelScope.launch {
            delay(100)
            startListening()
        }
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        if (_state.value is AssistantState.Listening) {
            _state.value = AssistantState.Idle
        }
    }

    private fun processCommand(command: String) {
        val lowerCommand = command.lowercase()
        
        // Timer command detection - "set timer for X minutes/min/seconds/sec"
        val timerRegex = """(?:set\s+)?(?:a\s+)?timer\s+(?:for\s+)?(\d+)\s*(min(?:ute)?s?|sec(?:ond)?s?|hour(?:s)?)?""".toRegex(RegexOption.IGNORE_CASE)
        val timerMatch = timerRegex.find(lowerCommand)
        if (timerMatch != null || (lowerCommand.contains("timer") && lowerCommand.any { it.isDigit() })) {
            val numberMatch = """(\d+)""".toRegex().find(lowerCommand)
            val number = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
            
            val unit = when {
                lowerCommand.contains("hour") -> "hour"
                lowerCommand.contains("sec") -> "second"
                else -> "minute"
            }
            
            val totalSeconds = when (unit) {
                "hour" -> number * 3600
                "second" -> number
                else -> number * 60
            }
            
            startTimer(totalSeconds, "$number $unit${if (number > 1) "s" else ""}")
            return
        }
        
        // Reminder/countdown detection
        if (lowerCommand.contains("remind") || lowerCommand.contains("countdown")) {
            val numberMatch = """(\d+)""".toRegex().find(lowerCommand)
            val minutes = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5
            startTimer(minutes * 60, "Reminder")
            return
        }
        
        // Simple implicit intent triggers
        if (lowerCommand.startsWith("open ")) {
            val targetAppName = lowerCommand.removePrefix("open ").trim()
            launchSimulatedIntent(targetAppName, "Opening $targetAppName")
            return
        } else if (lowerCommand.contains("play") && (lowerCommand.contains("music") || lowerCommand.contains("playlist") || lowerCommand.contains("song"))) {
            launchSimulatedIntent("Music", "Playing requested media...")
            return
        } else if (lowerCommand.contains("alarm") || lowerCommand.contains("wake")) {
            launchSimulatedIntent("Clock", "Setting alarm...")
            return
        } else if (lowerCommand.contains("text") || lowerCommand.contains("message") || lowerCommand.contains("call")) {
            launchSimulatedIntent("Messages", "Opening Messages...")
            return
        } else if (lowerCommand.contains("email") || lowerCommand.contains("mail")) {
            launchSimulatedIntent("Gmail", "Opening Email...")
            return
        } else if (lowerCommand.contains("search") || lowerCommand.contains("find")) {
            launchSimulatedIntent("Chrome", "Searching web...")
            return
        }

        // If no strict local intents match, use Fox AI Intelligence for advanced reasoning
        viewModelScope.launch {
            _state.value = AssistantState.Processing("Thinking...")
            
            try {
                val intent = FoxAIIntelligence.parseIntent(command)
                executeIntent(intent, command)
            } catch (e: Exception) {
                Log.e("VoiceAssistant", "AI processing failed", e)
                _state.value = AssistantState.Error("I couldn't process that: ${e.message}")
            }
        }
    }

    private suspend fun executeIntent(intent: AssistantIntent, originalCommand: String) {
        when (intent) {
            is AssistantIntent.HardwareControl -> {
                handleHardwareCommand(intent)
            }
            is AssistantIntent.AppControl -> {
                handleAppCommand(intent)
            }
            is AssistantIntent.Communicate -> {
                handleCommCommand(intent)
            }
            is AssistantIntent.Productivity -> {
                handleProductivityCommand(intent)
            }
            is AssistantIntent.MediaControl -> {
                handleMediaCommand(intent)
            }
            is AssistantIntent.Utility -> {
                handleUtilityCommand(intent)
            }
            is AssistantIntent.Information -> {
                // Fallback to regular streaming AI response for general queries
                generateAIResponse(originalCommand)
            }
            is AssistantIntent.Answer -> {
                _state.value = AssistantState.Success(intent.text)
                voiceSpeaker.speak(intent.text)
            }
            is AssistantIntent.Unknown -> {
                generateAIResponse(originalCommand)
            }
        }
    }

    private fun handleHardwareCommand(intent: AssistantIntent.HardwareControl) {
        when (intent.device.lowercase()) {
            "flashlight", "torch" -> {
                val enable = intent.action == "on" || (intent.action == "toggle" && !controlCenterViewModel.isFlashlightEnabled.value)
                controlCenterViewModel.setFlashlight(enable)
                _state.value = AssistantState.Success("${if (enable) "Enabled" else "Disabled"} the flashlight.", "HARDWARE")
            }
            "wifi" -> {
                if (intent.action == "on" || intent.action == "off") {
                    controlCenterViewModel.toggleWifi()
                    _state.value = AssistantState.Success("Adjusting Wi-Fi settings...", "HARDWARE")
                }
            }
            "bluetooth" -> {
                controlCenterViewModel.toggleBluetooth()
                _state.value = AssistantState.Success("Adjusting Bluetooth settings...", "HARDWARE")
            }
            "brightness" -> {
                if (intent.action == "increase") controlCenterViewModel.adjustBrightness(true)
                else if (intent.action == "decrease") controlCenterViewModel.adjustBrightness(false)
                _state.value = AssistantState.Success("Adjusted brightness.", "HARDWARE")
            }
            "volume" -> {
                if (intent.action == "increase") controlCenterViewModel.adjustVolume(true)
                else if (intent.action == "decrease") controlCenterViewModel.adjustVolume(false)
                _state.value = AssistantState.Success("Adjusted volume.", "HARDWARE")
            }
            "hotspot" -> {
                controlCenterViewModel.openHotspotSettings()
                _state.value = AssistantState.Success("Opening Hotspot settings...", "HARDWARE")
            }
            "airplane" -> {
                controlCenterViewModel.toggleAirplaneMode()
                _state.value = AssistantState.Success("Opening Airplane Mode settings...", "HARDWARE")
            }
            "rotate", "rotation" -> {
                controlCenterViewModel.toggleAutoRotate()
                _state.value = AssistantState.Success("Toggled auto-rotate.", "HARDWARE")
            }
            "location", "gps" -> {
                controlCenterViewModel.toggleLocation()
                _state.value = AssistantState.Success("Opening location settings...", "HARDWARE")
            }
        }
    }

    private fun handleAppCommand(intent: AssistantIntent.AppControl) {
        if (intent.action == "open") {
            launchSimulatedIntent(intent.app, "Opening ${intent.app}", "APPS")
        } else if (intent.action == "recents") {
            val intentRecents = Intent("com.android.systemui.recents.SHOW_RECENT_APPS")
            intentRecents.setPackage("com.android.systemui")
            intentRecents.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                getApplication<Application>().startActivity(intentRecents)
                _state.value = AssistantState.Success("Showing recent apps.", "APPS")
            } catch (e: Exception) {
                _state.value = AssistantState.Error("Could not show recents.")
            }
        }
    }

    private fun handleCommCommand(intent: AssistantIntent.Communicate) {
        val ctx = getApplication<Application>()
        when (intent.type.lowercase()) {
            "call" -> {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:${intent.contact}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(dialIntent)
                _state.value = AssistantState.Success("Calling ${intent.contact}...", "COMM")
            }
            "sms", "message" -> {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("smsto:")
                    putExtra("sms_body", intent.message ?: "")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(smsIntent)
                _state.value = AssistantState.Success("Preparing message to ${intent.contact}...", "COMM")
            }
            "whatsapp" -> {
                val url = "https://api.whatsapp.com/send?phone=${intent.contact}&text=${android.net.Uri.encode(intent.message ?: "")}"
                val waIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    ctx.startActivity(waIntent)
                    _state.value = AssistantState.Success("Opening WhatsApp...", "COMM")
                } catch (e: Exception) {
                    _state.value = AssistantState.Error("WhatsApp is not installed.")
                }
            }
        }
    }

    private suspend fun handleProductivityCommand(intent: AssistantIntent.Productivity) {
        when (intent.type.lowercase()) {
            "timer" -> {
                val seconds = (intent.details["time"]?.toString()?.toIntOrNull() ?: 5) * 60
                startTimer(seconds, intent.details["label"]?.toString() ?: "Timer")
            }
            "alarm" -> {
                val alarmIntent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, intent.details["label"]?.toString() ?: "Assistant Alarm")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                getApplication<Application>().startActivity(alarmIntent)
                _state.value = AssistantState.Success("Setting alarm...", "PRODUCTIVITY")
            }
            "note" -> {
                val content = intent.details["content"]?.toString() ?: intent.details["label"]?.toString() ?: ""
                if (content.isNotBlank()) {
                    noteViewModel.addNote(content)
                    _state.value = AssistantState.Success("Note saved: \"$content\"", "PRODUCTIVITY")
                } else {
                    _state.value = AssistantState.Error("What should the note say?")
                }
            }
            "task", "reminder" -> {
                val title = intent.details["label"]?.toString() ?: intent.details["content"]?.toString() ?: ""
                if (title.isNotBlank()) {
                    taskViewModel.addTask(title)
                    _state.value = AssistantState.Success("Task added: \"$title\"", "PRODUCTIVITY")
                } else {
                    _state.value = AssistantState.Error("What's the task?")
                }
            }
            "schedule" -> {
                _state.value = AssistantState.Success("Opening your schedule...", "PRODUCTIVITY")
            }
        }
    }

    private fun handleMediaCommand(intent: AssistantIntent.MediaControl) {
        // Broadcast media intents
        val action = when (intent.action.lowercase()) {
            "play" -> "com.android.music.musicservicecommand.togglepause"
            "pause" -> "com.android.music.musicservicecommand.pause"
            "next" -> "com.android.music.musicservicecommand.next"
            "prev" -> "com.android.music.musicservicecommand.previous"
            else -> null
        }
        if (action != null) {
            val mediaIntent = Intent(action)
            getApplication<Application>().sendBroadcast(mediaIntent)
            _state.value = AssistantState.Success("Media command: ${intent.action}", "MEDIA")
        }
    }

    private fun handleUtilityCommand(intent: AssistantIntent.Utility) {
        when (intent.type.lowercase()) {
            "coin" -> _state.value = AssistantState.Success("🪙 It's ${if (Random().nextBoolean()) "Heads" else "Tails"}!", "UTILITY")
            "dice" -> _state.value = AssistantState.Success("🎲 You rolled a ${Random().nextInt(6) + 1}!", "UTILITY")
            "calc" -> {
                val result = intent.params["result"] ?: intent.params["expression"]
                _state.value = AssistantState.Success("Result: $result", "UTILITY")
            }
            "weather" -> {
                viewModelScope.launch {
                    weatherViewModel.fetchWeather(getApplication())
                    val weather = weatherViewModel.weatherInfo.first { it.temperature != "--°C" }
                    _state.value = AssistantState.Success("The weather in ${weather.city} is ${weather.condition} at ${weather.temperature}.", "INFO")
                }
            }
            "stopwatch" -> {
                _state.value = AssistantState.Success("Stopwatch started.", "UTILITY")
            }
            else -> _state.value = AssistantState.Success("Executing ${intent.type} command...", "UTILITY")
        }
    }

    private suspend fun generateAIResponse(command: String) {
        _state.value = AssistantState.Processing("Thinking...")
        try {
            GenerativeAIEngine.generateStreamingResponse(command).collect { chunk ->
                _state.value = AssistantState.Generating(chunk)
                voiceSpeaker.speak(chunk)
            }
            val finalState = _state.value
            if (finalState is AssistantState.Generating) {
                _state.value = AssistantState.Success(finalState.partialText)
            }
        } catch (e: Exception) {
            _state.value = AssistantState.Error("Generation failed: ${e.message}")
        }
    }
    
    private fun startTimer(totalSeconds: Int, label: String) {
        timerJob?.cancel()
        _state.value = AssistantState.Timer(totalSeconds, totalSeconds, label)
        
        timerJob = viewModelScope.launch {
            for (remaining in totalSeconds downTo 0) {
                _state.value = AssistantState.Timer(totalSeconds, remaining, label)
                if (remaining > 0) {
                    delay(1000)
                }
            }
            _state.value = AssistantState.Success("⏰ Timer completed!\n$label is done.")
            voiceSpeaker.speak("Your $label timer is done!")
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000)
        }
    }
    
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = AssistantState.Idle
    }

    private fun launchSimulatedIntent(targetAppName: String, successMessage: String, category: String? = null) {
        viewModelScope.launch {
            val apps = repository.getInstalledApps().first()
            val targetApp = apps.find { it.label.lowercase().contains(targetAppName.lowercase()) }
            
            if (targetApp != null) {
                repository.launchApp(targetApp.packageName)
                _state.value = AssistantState.Success(successMessage, category)
                voiceSpeaker.speak(successMessage, flush = true)
            } else {
                val errorMsg = "I couldn't find an app for that intent ($targetAppName)"
                _state.value = AssistantState.Error(errorMsg)
                voiceSpeaker.speak(errorMsg)
            }
        }
    }

    fun submitTextCommand(command: String) {
        if (command.isBlank()) return
        
        speechRecognizer.stopListening()
        speechRecognizer.cancel()
        timerJob?.cancel()
        
        _recognizedText.value = command
        _state.value = AssistantState.Processing("Analyzing...")
        
        processCommand(command)
    }

    fun resetState() {
        _state.value = AssistantState.Idle
        _recognizedText.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        voiceSpeaker.shutdown()
        toneGenerator.release()
    }
}
