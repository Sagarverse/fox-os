package com.example.foxos.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

object GenerativeAIEngine {
    
    private var generativeModel: GenerativeModel? = null
    private var currentApiKey: String = ""
    
    fun setApiKey(apiKey: String) {
        if (apiKey.isNotBlank() && apiKey != currentApiKey) {
            currentApiKey = apiKey
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                }
            )
            Log.d("GenerativeAIEngine", "Gemini model initialized")
        }
    }
    
    fun isApiKeySet(): Boolean = currentApiKey.isNotBlank() && generativeModel != null
    
    // Generates a response using Gemini API if key is set, otherwise uses fallback
    fun generateStreamingResponse(prompt: String): Flow<String> = flow {
        Log.d("GenerativeAIEngine", "generateStreamingResponse called, isApiKeySet: ${isApiKeySet()}, currentApiKey length: ${currentApiKey.length}")
        
        if (isApiKeySet()) {
            try {
                val systemPrompt = """You are Fox, a helpful AI assistant built into FoxOS, a custom Android launcher. 
                    |You help users with their phone, answer questions, and provide concise, friendly responses.
                    |Keep responses brief and helpful. You can help open apps, provide information, and assist with tasks.
                    |Current user request: $prompt""".trimMargin()
                
                Log.d("GenerativeAIEngine", "Calling Gemini API with prompt length: ${systemPrompt.length}")
                val response = generativeModel?.generateContentStream(systemPrompt)
                var fullText = ""
                
                response?.collect { chunk ->
                    chunk.text?.let { text ->
                        fullText += text
                        Log.d("GenerativeAIEngine", "Received chunk, total length: ${fullText.length}")
                        emit(fullText)
                    }
                }
                
                if (fullText.isEmpty()) {
                    Log.w("GenerativeAIEngine", "Gemini returned empty response")
                    emit("I'm having trouble responding right now. Please try again.")
                } else {
                    Log.d("GenerativeAIEngine", "Gemini response complete, total length: ${fullText.length}")
                }
            } catch (e: Exception) {
                Log.e("GenerativeAIEngine", "Gemini API error: ${e.javaClass.simpleName} - ${e.message}", e)
                // Fall back to local response on error
                emitFallbackResponse(prompt).collect { emit(it) }
            }
        } else {
            Log.d("GenerativeAIEngine", "Using fallback response (no API key)")
            emitFallbackResponse(prompt).collect { emit(it) }
        }
    }.flowOn(Dispatchers.IO)
    
    // Fallback responses when no API key is set
    private fun emitFallbackResponse(prompt: String): Flow<String> = flow {
        val lowerPrompt = prompt.lowercase()
        
        delay(300)
        
        val fullResponse = when {
            lowerPrompt.contains("sleep") || lowerPrompt.contains("bed") -> 
                "Understood. Preparing the OS for sleep. Enabling Focus Mode to block distractions and dimming the screen. Goodnight!"
            lowerPrompt.contains("poem") || lowerPrompt.contains("haiku") -> 
                "Here is a haiku for you:\n\nSilvery fox leaps,\nThrough the glass of open code,\nThe screen now awakes."
            lowerPrompt.contains("weather") -> 
                "For accurate weather, I recommend checking your weather app. Would you like me to open it?"
            lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("hey") -> 
                "Hello! I'm Fox, your AI assistant. I can help you open apps, set timers, play music, or answer questions. What would you like to do?"
            lowerPrompt.contains("what can you do") || lowerPrompt.contains("help") ->
                "I can help you with:\n• Opening apps - say 'Open [app name]'\n• Playing music - say 'Play music'\n• Setting alarms - say 'Set alarm'\n• Sending messages - say 'Send message'\n• Web searches - say 'Search for [topic]'\n\nTip: Add a Gemini API key in Settings for smarter responses!"
            lowerPrompt.contains("thank") ->
                "You're welcome! Let me know if you need anything else."
            lowerPrompt.contains("time") ->
                "You can check the time on your home screen clock. Would you like me to open the Clock app?"
            lowerPrompt.contains("who are you") || lowerPrompt.contains("your name") ->
                "I'm Fox, the AI assistant built into FoxOS. I'm here to help make your phone experience smoother and more intuitive."
            lowerPrompt.contains("joke") ->
                "Why do programmers prefer dark mode? Because light attracts bugs! 🦊"
            lowerPrompt.contains("good morning") ->
                "Good morning! Ready to start a productive day? I can help you check your tasks or open your favorite apps."
            lowerPrompt.contains("good night") ->
                "Good night! Sleep well. I'll keep things quiet for you."
            else -> 
                "I heard: \"$prompt\"\n\nFor smarter responses, add your Gemini API key in Settings → AI Assistant. Otherwise, try: 'Open [app]', 'Play music', or 'Set alarm'."
        }
        
        val words = fullResponse.split(" ")
        for (i in words.indices) {
            val chunk = words.subList(0, i + 1).joinToString(" ")
            emit(chunk)
            delay((10..40).random().toLong())
        }
    }
}
