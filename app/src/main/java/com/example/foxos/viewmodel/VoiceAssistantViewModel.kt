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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.util.*

sealed class AssistantState {
    object Idle : AssistantState()
    object Listening : AssistantState()
    data class Processing(val text: String) : AssistantState()
    data class Generating(val partialText: String) : AssistantState()
    data class Success(val response: String) : AssistantState()
    data class Error(val message: String) : AssistantState()
    data class Timer(val totalSeconds: Int, val remainingSeconds: Int, val label: String = "Timer") : AssistantState()
}

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
    private val repository = LauncherRepository(application)
    private val settingsRepository = SettingsRepository(application)
    
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private var timerJob: Job? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("VoiceAssistant", "onReadyForSpeech")
            _state.value = AssistantState.Listening
        }
        override fun onBeginningOfSpeech() {
            Log.d("VoiceAssistant", "onBeginningOfSpeech")
        }
        override fun onRmsChanged(rmsdB: Float) {}
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
        // Continuously observe API key changes
        viewModelScope.launch {
            settingsRepository.geminiApiKey.collectLatest { apiKey ->
                Log.d("VoiceAssistant", "API key changed, length: ${apiKey.length}, blank: ${apiKey.isBlank()}")
                if (apiKey.isNotBlank()) {
                    GenerativeAIEngine.setApiKey(apiKey)
                    Log.d("VoiceAssistant", "Gemini API key set, isApiKeySet: ${GenerativeAIEngine.isApiKeySet()}")
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
        _recognizedText.value = ""
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

        // If no strict intents match, pass to the Generative AI Engine for reasoning
        viewModelScope.launch {
            _state.value = AssistantState.Processing("Thinking...")
            Log.d("VoiceAssistant", "Processing command with AI, isApiKeySet: ${GenerativeAIEngine.isApiKeySet()}")
            
            try {
                GenerativeAIEngine.generateStreamingResponse(command).collect { chunk ->
                    Log.d("VoiceAssistant", "AI response chunk, length: ${chunk.length}")
                    _state.value = AssistantState.Generating(chunk)
                }
                
                // Finalize the generation
                val finalState = _state.value
                if (finalState is AssistantState.Generating) {
                    Log.d("VoiceAssistant", "AI generation complete, response length: ${finalState.partialText.length}")
                    _state.value = AssistantState.Success(finalState.partialText)
                }
            } catch (e: Exception) {
                Log.e("VoiceAssistant", "AI generation failed", e)
                _state.value = AssistantState.Error("Generation failed: ${e.message}")
            }
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
        }
    }
    
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = AssistantState.Idle
    }

    private fun launchSimulatedIntent(targetAppName: String, successMessage: String) {
        viewModelScope.launch {
            val apps = repository.getInstalledApps().first()
            val targetApp = apps.find { it.label.lowercase().contains(targetAppName.lowercase()) }
            
            if (targetApp != null) {
                repository.launchApp(targetApp.packageName)
                _state.value = AssistantState.Success(successMessage)
            } else {
                _state.value = AssistantState.Error("I couldn't find an app for that intent ($targetAppName)")
            }
        }
    }

    fun resetState() {
        _state.value = AssistantState.Idle
        _recognizedText.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }
}