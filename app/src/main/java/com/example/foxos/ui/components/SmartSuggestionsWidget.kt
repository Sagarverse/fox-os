package com.example.foxos.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import java.util.*

data class SmartSuggestion(
    val title: String,
    val icon: ImageVector,
    val message: String,
    val actionLabel: String,
    val action: (() -> Unit)?
)

/**
 * A Context-Aware Smart Suggestions Widget that changes content based on time.
 */
@Composable
fun SmartSuggestionsWidget(
    modifier: Modifier = Modifier,
    onOpenFocus: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val colors = FoxLauncherTheme.colors

    val suggestion = when (hour) {
        in 5..11 -> SmartSuggestion(
            title = "Good Morning",
            icon = Icons.Default.WbSunny,
            message = "Traffic is light. 25 min to work.",
            actionLabel = "Start Navigation",
            action = {
                // Open Google Maps navigation
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=work")).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fall back to any maps app
                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=work"))
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                }
            }
        )
        in 12..17 -> SmartSuggestion(
            title = "Focus Time",
            icon = Icons.Default.Computer,
            message = "You have a meeting in 15 mins.",
            actionLabel = "Focus Mode",
            action = onOpenFocus
        )
        in 18..21 -> SmartSuggestion(
            title = "Good Evening",
            icon = Icons.Default.NightlightRound,
            message = "Wind down. Time to relax.",
            actionLabel = "Play Music",
            action = {
                // Open music app
                val intent = Intent().apply {
                    action = "android.intent.action.MUSIC_PLAYER"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Try Spotify specifically
                    val spotifyIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                    spotifyIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    spotifyIntent?.let { context.startActivity(it) }
                }
            }
        )
        else -> SmartSuggestion(
            title = "Night Mode",
            icon = Icons.Default.Bedtime,
            message = "Set alarm for tomorrow.",
            actionLabel = "Set Alarm",
            action = {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, 7)
                    putExtra(AlarmClock.EXTRA_MINUTES, 0)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Open clock app
                    val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(clockIntent)
                }
            }
        )
    }

    HarmonyWidget(modifier = modifier, title = suggestion.title) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(HarmonyShapes.medium)
                        .background(colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = suggestion.icon,
                        contentDescription = suggestion.title,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = suggestion.message,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(HarmonyShapes.medium)
                    .background(colors.primary)
                    .bounceClick { suggestion.action?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suggestion.actionLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
