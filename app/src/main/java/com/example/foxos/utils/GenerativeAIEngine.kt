package com.example.foxos.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object GenerativeAIEngine {
    
    private var generativeModel: GenerativeModel? = null
    private var currentApiKey: String = ""
    private var currentModel: String = "gemini-1.5-flash"
    private val client = OkHttpClient()
    
    fun setApiKey(apiKey: String, modelName: String = "gemini-1.5-flash") {
        Log.d("GenerativeAIEngine", "setApiKey called with key length: ${apiKey.length}, model: $modelName")
        if (apiKey.isNotBlank()) {
            currentApiKey = apiKey
            currentModel = modelName
            generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                }
            )
            Log.d("GenerativeAIEngine", "Gemini model initialized successfully. isApiKeySet: ${isApiKeySet()}")
        } else {
            Log.w("GenerativeAIEngine", "setApiKey called with blank key")
        }
    }
    
    suspend fun verifyApiKey(apiKey: String, modelName: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("API Key cannot be empty"))
        try {
            val testModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig { maxOutputTokens = 10 }
            )
            val response = testModel.generateContent("AI Verification Test. Respond with 'OK'.")
            if (response.text?.isNotEmpty() == true) {
                Result.success("Connection successful!")
            } else {
                Result.failure(Exception("Received unexpected response from API"))
            }
        } catch (e: Exception) {
            Log.e("GenerativeAIEngine", "API Verification failed", e)
            val errorMessage = when {
                e.message?.contains("API_KEY_INVALID") == true -> "Invalid API Key"
                e.message?.contains("model not found") == true -> "Model $modelName not supported by this key"
                e.message?.contains("quota") == true -> "Quota exceeded or trial ended"
                e.message?.contains("permission") == true -> "Permission denied (check subscription)"
                else -> e.message ?: "Unknown interaction error"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    data class ModelMetadata(
        val name: String,
        val displayName: String,
        val description: String,
        val supportsGenerate: Boolean
    )

    suspend fun fetchAvailableModels(apiKey: String): Result<List<ModelMetadata>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("API Key is required"))
        
        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    val code = response.code
                    val message = when (code) {
                        400 -> "Bad Request: Check API format"
                        401 -> "Invalid API Key: Unauthorized"
                        403 -> "Forbidden: Key restricted or trial ended"
                        429 -> "Quota Exceeded: Too many requests"
                        else -> "HTTP $code: $errorBody"
                    }
                    return@withContext Result.failure(Exception(message))
                }

                val bodyString = response.body?.string() ?: "{}"
                val jsonResponse = JSONObject(bodyString)
                val modelsArray = jsonResponse.optJSONArray("models") ?: return@withContext Result.success(emptyList())
                
                val modelList = mutableListOf<ModelMetadata>()
                for (i in 0 until modelsArray.length()) {
                    val modelObj = modelsArray.getJSONObject(i)
                    val name = modelObj.getString("name").substringAfter("models/")
                    val displayName = modelObj.optString("displayName", name)
                    val description = modelObj.optString("description", "")
                    
                    val methods = modelObj.optJSONArray("supportedGenerationMethods")
                    var supportsGenerate = false
                    if (methods != null) {
                        for (j in 0 until methods.length()) {
                            val method = methods.getString(j)
                            if (method == "generateContent" || method == "generateText") {
                                supportsGenerate = true
                                break
                            }
                        }
                    } 
                    
                    // Always include models that have "gemini" in their name
                    if (name.contains("gemini", ignoreCase = true)) {
                        val isSupported = supportsGenerate || 
                                         name.contains("1.5") || 
                                         name.contains("2.0") || 
                                         name.contains("pro") || 
                                         name.contains("flash")
                        
                        modelList.add(ModelMetadata(name, displayName, description, isSupported))
                    }
                }
                
                // Superior sorting: 2.0 > 1.5 > Pro > Flash
                val sortedList = modelList.sortedWith(
                    compareByDescending<ModelMetadata> { it.name.contains("2.0") }
                        .thenByDescending { it.name.contains("1.5") }
                        .thenByDescending { it.name.contains("pro") }
                        .thenByDescending { it.name.contains("flash") }
                        .thenBy { it.name }
                )
                
                Result.success(sortedList)
            }
        } catch (e: Exception) {
            Log.e("GenerativeAIEngine", "Failed to fetch models", e)
            Result.failure(e)
        }
    }
    
    fun isApiKeySet(): Boolean = currentApiKey.isNotBlank() && generativeModel != null
    
    // Generates a response using Gemini API if key is set, otherwise uses fallback
    fun generateStreamingResponse(prompt: String): Flow<String> = flow {
        Log.d("GenerativeAIEngine", "generateStreamingResponse called, isApiKeySet: ${isApiKeySet()}, currentApiKey length: ${currentApiKey.length}")
        
        if (isApiKeySet()) {
            try {
                val systemPrompt = """You are Fox, an advanced AI assistant deeply integrated into FoxOS. 
                    |Speak directly to the user in a natural, concise, and friendly tone. Start answering immediately without repetitive robotic greetings.
                    |Use **bold** for emphasis on important words, but do not use any other markdown formatting.
                    |Current user request: $prompt""".trimMargin()
                
                Log.d("GenerativeAIEngine", "Calling Gemini API with prompt length: ${systemPrompt.length}")
                val response = generativeModel?.generateContentStream(systemPrompt)
                var fullText = ""
                
                response?.collect { chunk ->
                    chunk.text?.let { text ->
                        fullText += text
                        Log.d("GenerativeAIEngine", "Received chunk length: ${text.length}")
                        emit(text)
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
                // Fall back to local response on error, with a hint that API failed
                emitFallbackResponse(prompt, isError = true).collect { emit(it) }
            }
        } else {
            Log.d("GenerativeAIEngine", "Using fallback response (no API key)")
            emitFallbackResponse(prompt).collect { emit(it) }
        }
    }.flowOn(Dispatchers.IO)
    
    // Fallback responses when no API key is set or API fails
    private fun emitFallbackResponse(prompt: String, isError: Boolean = false): Flow<String> = flow {
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
        
        val errorSuffix = if (isError) "\n\n(Note: Your Gemini API key is set but encountered an error. Please check it in Settings.)" else ""
        val finalResponse = fullResponse + errorSuffix
        
        val words = finalResponse.split(" ")
        for (i in words.indices) {
            val chunk = words[i] + if (i < words.size - 1) " " else ""
            emit(chunk)
            delay((10..40).random().toLong())
        }
    }
}
