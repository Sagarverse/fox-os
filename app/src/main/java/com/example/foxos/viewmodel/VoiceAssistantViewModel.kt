package com.example.foxos.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import com.example.foxos.data.LauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import java.util.*

sealed class AssistantState {
    object Idle : AssistantState()
    object Listening : AssistantState()
    data class Processing(val text: String) : AssistantState()
    data class Success(val response: String) : AssistantState()
    data class Error(val message: String) : AssistantState()
}

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
    private val repository = LauncherRepository(application)
    
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = AssistantState.Listening
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _state.value = AssistantState.Processing("Analyzing...")
            }
            override fun onError(error: Int) {
                _state.value = AssistantState.Error("Try again")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _recognizedText.value = text
                processCommand(text)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        if (_state.value is AssistantState.Listening) {
            _state.value = AssistantState.Idle
        }
    }

    private fun processCommand(command: String) {
        val lowerCommand = command.lowercase()
        
        if (lowerCommand.startsWith("open ")) {
            val targetAppName = lowerCommand.removePrefix("open ").trim()
            viewModelScope.launch {
                val apps = repository.getInstalledApps()
                val targetApp = apps.find { it.label.lowercase().contains(targetAppName) }
                
                if (targetApp != null) {
                    repository.launchApp(targetApp.packageName)
                    _state.value = AssistantState.Success("Opening $targetAppName")
                } else {
                    _state.value = AssistantState.Error("I couldn't find $targetAppName")
                }
            }
        } else {
            _state.value = AssistantState.Success("Understood: $command")
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