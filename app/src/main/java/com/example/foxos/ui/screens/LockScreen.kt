package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.AppIcon
import com.example.foxos.ui.components.GlassCard
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.components.HarmonyAppIcon
import com.example.foxos.ui.components.HarmonyBackground
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.viewmodel.QuickShortcutViewModel
import com.example.foxos.viewmodel.ContextViewModel
import com.example.foxos.viewmodel.UserContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    launcherViewModel: LauncherViewModel,
    shortcutViewModel: QuickShortcutViewModel,
    contextViewModel: ContextViewModel,
    onUnlock: () -> Unit
) {
    val shortcuts by shortcutViewModel.shortcuts.collectAsState()
    val allApps by launcherViewModel.allApps.collectAsState()
    val colors = FoxLauncherTheme.colors
    
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000 * 60) // Update every minute
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    var offsetY by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        offsetY = (offsetY + delta).coerceAtMost(0f)
        if (offsetY < -400f) {
            onUnlock()
        }
    }

    val currentContext by contextViewModel.currentContext.collectAsState()

    val (greeting, contextIcon) = when (currentContext) {
        UserContext.HOME -> "Welcome Home" to Icons.Default.Home
        UserContext.WORK -> "Focus Mode" to Icons.Default.Work
        UserContext.COMMUTING -> "On the Go" to Icons.Default.DirectionsCar
        UserContext.SLEEPING -> "Sweet Dreams" to Icons.Default.NightlightRound
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { 
                    if (offsetY > -400f) offsetY = 0f 
                }
            )
            .offset { IntOffset(0, offsetY.roundToInt()) }
    ) {
        HarmonyBackground(wallpaperId = "cosmic") // Use a premium wallpaper
        
        // Dark overlay for legibility
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeFormat.format(currentTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )
            )
            Text(
                text = dateFormat.format(currentTime).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Context Badge with Glassmorphism
            GlassPanel(
                modifier = Modifier,
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f),
                blurRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(contextIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Shortcuts Row - Enhanced
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                shortcuts.take(4).forEach { shortcut ->
                    val app = allApps.find { it.packageName == shortcut.packageName }
                    if (app != null) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .clip(CircleShape)
                        ) {
                            HarmonyAppIcon(
                                icon = app.icon?.toBitmap()?.asImageBitmap(),
                                label = "",
                                onClick = {
                                    launcherViewModel.launchApp(app.packageName)
                                    onUnlock()
                                },
                                showLabel = false
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Swipe up to unlock",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}