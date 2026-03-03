package com.example.foxos.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import com.example.foxos.ui.components.*
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.ControlAction
import com.example.foxos.viewmodel.ControlCenterViewModel
import com.example.foxos.viewmodel.PomodoroViewModel
import com.example.foxos.viewmodel.StudentHubViewModel
import java.util.concurrent.TimeUnit

// Media control helper functions
private fun sendMediaKeyEvent(context: Context, keyCode: Int) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    // Send both down and up events
    val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
    val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
    audioManager.dispatchMediaKeyEvent(downEvent)
    audioManager.dispatchMediaKeyEvent(upEvent)
}

private fun playPauseMedia(context: Context) {
    sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
}

private fun nextTrack(context: Context) {
    sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT)
}

private fun previousTrack(context: Context) {
    sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
}

@Composable
fun SuperHubScreen(
    controlViewModel: ControlCenterViewModel,
    pomodoroViewModel: PomodoroViewModel,
    studentHubViewModel: StudentHubViewModel? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val colors = FoxLauncherTheme.colors
    
    // Get upcoming exams from StudentHub
    val exams = studentHubViewModel?.exams?.collectAsState()?.value ?: emptyList()
    val nextExam = exams.firstOrNull()
    
    // Calculate time until next exam
    val examCountdown = remember(nextExam) {
        nextExam?.let {
            val diff = it.examDate - System.currentTimeMillis()
            if (diff > 0) {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                "${days}d ${hours}h"
            } else "Now!"
        } ?: "None"
    }
    val wifiActive by controlViewModel.isWifiEnabled.collectAsState()
    val btActive by controlViewModel.isBluetoothEnabled.collectAsState()
    val dndActive by controlViewModel.isDndEnabled.collectAsState()
    val volume by controlViewModel.volume.collectAsState()
    val brightness by controlViewModel.brightness.collectAsState()
    val pendingAction by controlViewModel.pendingAction.collectAsState()
    
    val timeLeft by pomodoroViewModel.timeLeft.collectAsState()
    
    // Track if music is playing (toggle state)
    var isPlaying by remember { mutableStateOf(false) }

    // Refresh states when screen opens
    LaunchedEffect(Unit) {
        controlViewModel.refreshStates()
    }

    // Handle pending actions (open settings)
    LaunchedEffect(pendingAction) {
        when (val action = pendingAction) {
            is ControlAction.OpenSettings -> {
                context.startActivity(action.intent)
                controlViewModel.clearPendingAction()
            }
            ControlAction.None -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)) // Deeper darkened backdrop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FuturisticText(
                    text = "SUPERHUB",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Close", tint = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HarmonyOS Connectivity Panel (using HarmonyControlCenter)
            HarmonyControlCenter(
                modifier = Modifier.fillMaxWidth(),
                wifiEnabled = wifiActive,
                bluetoothEnabled = btActive,
                dndEnabled = dndActive,
                brightness = brightness,
                onWifiToggle = { controlViewModel.toggleWifi() },
                onBluetoothToggle = { controlViewModel.toggleBluetooth() },
                onDndToggle = { controlViewModel.toggleDnd() },
                onBrightnessChange = { controlViewModel.setBrightness(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            // Volume slider in separate card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                HarmonySlider(
                    value = volume,
                    onValueChange = { controlViewModel.setVolume(it) },
                    icon = Icons.Default.VolumeUp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Super Device (Unique HarmonyOS UI)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "SUPER DEVICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        
                        // Animated Radar Rings
                        val infiniteTransition = rememberInfiniteTransition(label = "RadarRings")
                        val ringScale by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "RingScale"
                        )
                        val ringAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "RingAlpha"
                        )

                        Canvas(modifier = Modifier.size(140.dp)) {
                            drawCircle(
                                color = colors.primary.copy(alpha = ringAlpha),
                                radius = size.minDimension / 2 * ringScale,
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = colors.primary.copy(alpha = 0.1f),
                                radius = size.minDimension / 2
                            )
                        }

                        // Central Phone Node
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(colors.primary, CircleShape)
                                .bounceClick { /* Start Scan */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneIphone, "This Device", tint = Color.White)
                        }
                        
                        // Nearby Nodes (Tablets, Laptops)
                        NearbyNode(Modifier.align(Alignment.TopStart).offset(x = 32.dp, y = 16.dp), Icons.Default.TabletMac, "MatePad")
                        NearbyNode(Modifier.align(Alignment.BottomEnd).offset(x = (-32).dp, y = (-16).dp), Icons.Default.LaptopMac, "MateBook")
                        NearbyNode(Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 32.dp), Icons.Default.Headphones, "FreeBuds")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unique Productivity Hub
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ServiceWidget(modifier = Modifier.weight(1f), title = "Focus Stats") {
                    Column {
                        val pomodoroState = pomodoroViewModel.state.collectAsState().value
                        val completedSessions = pomodoroState.totalCompletedSessions
                        val efficiency = if (completedSessions > 0) {
                            ((completedSessions * 25f) / (completedSessions * 25f + 5f) * 100).toInt().coerceIn(0, 100)
                        } else 0
                        Text("Sessions: $completedSessions", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("$efficiency%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
                ServiceWidget(modifier = Modifier.weight(1f), title = "Upcoming") {
                    Column {
                        Text(
                            if (nextExam != null) nextExam.subject.take(12) else "No exams", 
                            fontSize = 12.sp, 
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(examCountdown, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Focus Music Control (using HarmonyMusicCard)
            HarmonyMusicCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Media Control",
                artist = if (isPlaying) "Playing" else "Tap to play",
                isPlaying = isPlaying,
                onPlayPause = {
                    playPauseMedia(context)
                    isPlaying = !isPlaying
                },
                onPrevious = { previousTrack(context) },
                onNext = { nextTrack(context) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ShimmerButton(
                text = "START FOCUS SESSION",
                onClick = { pomodoroViewModel.startTimer() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NearbyNode(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, name: String) {
    val colors = FoxLauncherTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.bounceClick { /* Connect */ }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = name, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
        }
        Text(name, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
    }
}
