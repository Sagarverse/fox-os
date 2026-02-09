package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.viewmodel.QuickShortcutViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    launcherViewModel: LauncherViewModel,
    shortcutViewModel: QuickShortcutViewModel,
    onUnlock: () -> Unit
) {
    val shortcuts by shortcutViewModel.shortcuts.collectAsState()
    val allApps by launcherViewModel.allApps.collectAsState()
    
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    var offsetY by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        offsetY += delta
        if (offsetY < -300f) {
            onUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B0D17), Color(0xFF1A1C2E))
                )
            )
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { offsetY = 0f }
            )
            .offset { IntOffset(0, offsetY.roundToInt()) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = timeFormat.format(currentTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            )
            Text(
                text = dateFormat.format(currentTime),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Quick Shortcuts Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                shortcuts.forEach { shortcut ->
                    val app = allApps.find { it.packageName == shortcut.packageName }
                    if (app != null) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(4.dp)
                        ) {
                            AppIcon(
                                app = app,
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

            Icon(
                Icons.Default.LockOpen,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Text(
                "Swipe up to unlock",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}