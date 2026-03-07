package com.example.foxos.ai

import android.util.Log
import com.example.foxos.utils.GenerativeAIEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import org.json.JSONObject

sealed class AssistantIntent {
    data class HardwareControl(val device: String, val action: String, val value: Any? = null) : AssistantIntent()
    data class AppControl(val app: String, val action: String) : AssistantIntent()
    data class Communicate(val contact: String, val type: String, val message: String? = null) : AssistantIntent()
    data class Productivity(val type: String, val action: String, val details: Map<String, Any?> = emptyMap()) : AssistantIntent()
    data class Information(val query: String) : AssistantIntent()
    data class Utility(val type: String, val params: Map<String, Any?> = emptyMap()) : AssistantIntent()
    data class MediaControl(val action: String) : AssistantIntent()
    data class Answer(val text: String) : AssistantIntent()
    object Unknown : AssistantIntent()
}

object FoxAIIntelligence {
    private const val TAG = "FoxAI"
    
    private val SYSTEM_PROMPT = """
        You are Fox, the intelligent core of FoxOS. Your goal is to parse user requests into structured JSON intents.
        Categories and Actions:
        1. HARDWARE: devices (flashlight, wifi, bluetooth, brightness, volume, hotspot, airplane), actions (on, off, toggle, increase, decrease, set)
        2. APPS: apps (name), actions (open, close, recents)
        3. COMM: type (call, sms, whatsapp), contact (name), message (text)
        4. PRODUCTIVITY: type (alarm, timer, reminder, note, schedule), actions (set, show, add), details (time, label, content)
        5. MEDIA: actions (play, pause, next, prev, vol_up, vol_down)
        6. UTILITY: type (calc, currency, stopwatch, coin, dice), params (expression, amount, from, to)
        7. INFO: query (text) - used for general questions that don't fit above.

        Output ONLY a JSON object in this format:
        {
          "type": "HARDWARE|APPS|COMM|PRODUCTIVITY|MEDIA|UTILITY|INFO|ANSWER",
          "action": "action_name",
          "target": "target_name",
          "details": { ... },
          "response": "Brief verbal confirmation"
        }
        
        Example 1: "Turn on flashlight" -> {"type":"HARDWARE", "action":"on", "target":"flashlight", "response":"Flashlight turned on."}
        Example 2: "Open YouTube" -> {"type":"APPS", "action":"open", "target":"YouTube", "response":"Opening YouTube."}
        Example 3: "Set alarm for 7 AM" -> {"type":"PRODUCTIVITY", "action":"set", "target":"alarm", "details":{"time":"07:00"}, "response":"Alarm set for 7 AM."}
        Example 4: "What is 25 times 8" -> {"type":"UTILITY", "action":"calculate", "target":"calc", "details":{"expression":"25 * 8"}, "response":"25 times 8 is 200."}
        Example 5: "Who is the PM of India" -> {"type":"ANSWER", "response":"The Prime Minister of India is Narendra Modi."}
    """.trimIndent()

    suspend fun parseIntent(command: String): AssistantIntent {
        // 1. Try local parsing first for speed and offline capability
        val localIntent = parseLocalIntent(command)
        if (localIntent !is AssistantIntent.Unknown) {
            Log.d(TAG, "Parsed local intent: $localIntent")
            return localIntent
        }

        // 2. If local fails, try Gemini if API key is set
        if (!GenerativeAIEngine.isApiKeySet()) return AssistantIntent.Unknown
        
        try {
            val fullPrompt = "$SYSTEM_PROMPT\n\nUser request: \"$command\"\nJSON Output:"
            val responseFlow = GenerativeAIEngine.generateStreamingResponse(fullPrompt)
            val fullResponse = responseFlow.toList().joinToString("")
            if (fullResponse.isEmpty()) return AssistantIntent.Unknown
            
            // Extract JSON from response (sometimes AI wraps in ```json)
            val jsonStr = extractJson(fullResponse)
            val json = JSONObject(jsonStr)
            
            val type = json.optString("type")
            val action = json.optString("action")
            val target = json.optString("target")
            val response = json.optString("response")
            
            val details = mutableMapOf<String, Any?>()
            val detailsJson = json.optJSONObject("details")
            detailsJson?.keys()?.forEach { key ->
                details[key] = detailsJson.get(key)
            }

            return when (type) {
                "HARDWARE" -> AssistantIntent.HardwareControl(target, action, details["value"])
                "APPS" -> AssistantIntent.AppControl(target, action)
                "COMM" -> AssistantIntent.Communicate(target, action, details["message"]?.toString())
                "PRODUCTIVITY" -> AssistantIntent.Productivity(target, action, details)
                "MEDIA" -> AssistantIntent.MediaControl(action)
                "UTILITY" -> AssistantIntent.Utility(target, details)
                "INFO" -> AssistantIntent.Information(command)
                "ANSWER" -> AssistantIntent.Answer(response)
                else -> AssistantIntent.Unknown
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing intent with Gemini", e)
            return AssistantIntent.Unknown
        }
    }

    private fun parseLocalIntent(command: String): AssistantIntent {
        val lower = command.lowercase()
        return when {
            // Hardware Controls
            lower.contains("flashlight") || lower.contains("torch") -> {
                val action = if (lower.contains("off") || lower.contains("stop") || lower.contains("disable")) "off" else "on"
                AssistantIntent.HardwareControl("flashlight", action)
            }
            lower.contains("rotate") || lower.contains("rotation") -> AssistantIntent.HardwareControl("rotate", "toggle")
            lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet") -> {
                val action = if (lower.contains("off") || lower.contains("disable") || lower.contains("stop")) "off" else "on"
                AssistantIntent.HardwareControl("wifi", action)
            }
            lower.contains("location") || lower.contains("gps") -> AssistantIntent.HardwareControl("location", "toggle")
            lower.contains("bluetooth") -> {
                val action = if (lower.contains("off") || lower.contains("disable") || lower.contains("stop")) "off" else "on"
                AssistantIntent.HardwareControl("bluetooth", action)
            }
            lower.contains("brightness") || lower.contains("screen light") -> {
                val action = when {
                    lower.contains("increase") || lower.contains("up") || lower.contains("higher") || lower.contains("brighter") -> "increase"
                    lower.contains("decrease") || lower.contains("down") || lower.contains("lower") || lower.contains("dim") -> "decrease"
                    else -> "set"
                }
                AssistantIntent.HardwareControl("brightness", action)
            }
            lower.contains("volume") || lower.contains("sound") || lower.contains("audio") || lower.contains("loudness") -> {
                val action = when {
                    lower.contains("increase") || lower.contains("up") || lower.contains("higher") || lower.contains("louder") -> "increase"
                    lower.contains("decrease") || lower.contains("down") || lower.contains("lower") || lower.contains("quieter") -> "decrease"
                    lower.contains("mute") || lower.contains("silence") -> "mute"
                    else -> "set"
                }
                AssistantIntent.HardwareControl("volume", action)
            }
            lower.contains("hotspot") || lower.contains("tethering") -> {
                val action = if (lower.contains("off") || lower.contains("disable") || lower.contains("stop")) "off" else "on"
                AssistantIntent.HardwareControl("hotspot", action)
            }
            lower.contains("airplane") || lower.contains("flight mode") -> {
                val action = if (lower.contains("off") || lower.contains("disable") || lower.contains("stop")) "off" else "on"
                AssistantIntent.HardwareControl("airplane", action)
            }

            // App Controls
            lower.startsWith("open ") || lower.contains("launch ") || lower.startsWith("start ") || lower.startsWith("run ") -> {
                val appName = lower.substringAfter("open ").substringAfter("launch ").substringAfter("start ").substringAfter("run ").trim()
                if (appName.isNotEmpty()) AssistantIntent.AppControl(appName, "open") else AssistantIntent.Unknown
            }
            lower.contains("close app") || lower.contains("exit app") || lower.contains("kill app") -> AssistantIntent.AppControl("", "close")
            lower.contains("recent") || lower.contains("switch app") || lower.contains("overview") || lower.contains("recents") -> {
                AssistantIntent.AppControl("", "recents")
            }

            // Communication (Basic)
            lower.contains("call ") || lower.startsWith("dial ") -> {
                val contact = lower.substringAfter("call ").substringAfter("dial ").trim()
                if (contact.isNotEmpty()) AssistantIntent.Communicate(contact, "call") else AssistantIntent.Unknown
            }
            lower.contains("message ") || lower.contains("sms ") || lower.contains("text ") -> {
                val contact = when {
                    lower.contains("to ") -> lower.substringAfter("to ").trim()
                    lower.contains("message ") -> lower.substringAfter("message ").trim()
                    lower.contains("sms ") -> lower.substringAfter("sms ").trim()
                    lower.contains("text ") -> lower.substringAfter("text ").trim()
                    else -> "someone"
                }
                AssistantIntent.Communicate(if (contact.isNotBlank()) contact else "someone", "sms")
            }

            // Productivity
            (lower.contains("alarm") || lower.contains("wake me up")) && (lower.contains("set") || lower.contains("for") || lower.contains("at")) -> {
                AssistantIntent.Productivity("alarm", "set")
            }
            lower.contains("timer") && (lower.contains("for") || lower.contains("set")) -> {
                AssistantIntent.Productivity("timer", "set")
            }
            lower.contains("stopwatch") -> AssistantIntent.Productivity("stopwatch", "show")
            lower.contains("reminder") || lower.contains("remind") -> AssistantIntent.Productivity("reminder", "add")
            lower.contains("note") || lower.contains("write down") -> AssistantIntent.Productivity("note", "add")

            // Media
            lower.contains("play") || lower.contains("resume") -> AssistantIntent.MediaControl("play")
            lower.contains("pause") || lower.contains("stop music") -> AssistantIntent.MediaControl("pause")
            lower.contains("next") && (lower.contains("song") || lower.contains("track")) -> AssistantIntent.MediaControl("next")
            lower.contains("previous") && (lower.contains("song") || lower.contains("track")) -> AssistantIntent.MediaControl("prev")
            
            else -> AssistantIntent.Unknown
        }
    }

    private fun extractJson(text: String): String {
        // Remove markdown code blocks if present
        val cleaned = text.replace("```json", "").replace("```", "").trim()
        val start = cleaned.indexOf("{")
        val end = cleaned.lastIndexOf("}")
        return if (start != -1 && end != -1) {
            cleaned.substring(start, end + 1)
        } else {
            cleaned
        }
    }
}
