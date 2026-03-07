package com.example.foxos.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

class VoiceSpeaker(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    // A buffer to hold speech chunks while TTS initializes or if speaking rapidly
    private val speechQueue = LinkedList<String>()
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configureVoice()
                Log.d("VoiceSpeaker", "TTS Initialized Successfully")
                // Process any queued speech
                while (speechQueue.isNotEmpty()) {
                    speak(speechQueue.poll() ?: "")
                }
            } else {
                Log.e("VoiceSpeaker", "TTS Initialization Failed")
            }
        }
    }
    
    private fun configureVoice() {
        val currentTts = tts ?: return
        
        // 1. Set language to English
        val result = currentTts.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("VoiceSpeaker", "Language not supported or missing data")
        }
        
        // 2. Try to find a high-quality network voice to avoid robotic offline sounds
        try {
            val voices = currentTts.voices
            if (voices != null) {
                // Look for a network-based voice, usually ends with '-network'
                val preferredVoice = voices.firstOrNull { 
                    it.name.contains("network", ignoreCase = true) && 
                    it.locale.language == "en" && 
                    !it.isNetworkConnectionRequired // Ensure it won't crash if offline, though network voices usually require it.
                } ?: voices.firstOrNull { 
                    it.name.contains("en-us-x-sfg", ignoreCase = true) || // A common pleasant Google TTS voice
                    it.name.contains("en-gb-x-rjs", ignoreCase = true) 
                } ?: voices.firstOrNull { it.locale.language == "en" && it.quality > Voice.QUALITY_NORMAL }
                
                if (preferredVoice != null) {
                    currentTts.voice = preferredVoice
                    Log.d("VoiceSpeaker", "Selected voice: ${preferredVoice.name} (Quality: ${preferredVoice.quality})")
                } else {
                    Log.d("VoiceSpeaker", "No specific high-quality network voice found, using default.")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeaker", "Failed to configure specific voice, falling back to default", e)
        }
        
        // 3. Make the cadence feel slightly more conversational (less robotic)
        currentTts.setSpeechRate(1.05f) // Slightly faster than default
        currentTts.setPitch(1.1f) // Slightly higher pitch for a friendlier tone
    }
    
    fun speak(text: String, flush: Boolean = false) {
        if (text.isBlank()) return
        
        // Clean up markdown tokens if present before speaking
        val cleanText = text.replace("*", "").replace("#", "").trim()
        
        if (!isInitialized) {
            if (flush) speechQueue.clear()
            speechQueue.add(cleanText)
            return
        }
        
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        
        // Bundle to request network synthesis if available
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString())
        
        tts?.speak(cleanText, queueMode, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }
    
    fun stop() {
        speechQueue.clear()
        tts?.stop()
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
