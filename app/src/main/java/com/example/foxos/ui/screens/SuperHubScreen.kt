package com.example.foxos.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.*
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.ControlCenterViewModel
import com.example.foxos.viewmodel.PomodoroViewModel

@Composable
fun SuperHubScreen(
    controlViewModel: ControlCenterViewModel,
    pomodoroViewModel: PomodoroViewModel,
    onClose: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val wifiActive by controlViewModel.isWifiEnabled.collectAsState()
    val btActive by controlViewModel.isBluetoothEnabled.collectAsState()
    val dndActive by controlViewModel.isDndEnabled.collectAsState()
    val volume by controlViewModel.volume.collectAsState()
    val brightness by controlViewModel.brightness.collectAsState()
    
    val timeLeft by pomodoroViewModel.timeLeft.collectAsState()

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

            // HarmonyOS Connectivity Panel
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ControlCenterToggle(
                            icon = Icons.Default.Wifi,
                            label = "Wi-Fi",
                            isActive = wifiActive,
                            onClick = { controlViewModel.toggleWifi() }
                        )
                        ControlCenterToggle(
                            icon = Icons.Default.Bluetooth,
                            label = "Bluetooth",
                            isActive = btActive,
                            onClick = { controlViewModel.toggleBluetooth() }
                        )
                        ControlCenterToggle(
                            icon = Icons.Default.DoNotDisturbOn,
                            label = "DND",
                            isActive = dndActive,
                            onClick = { controlViewModel.toggleDnd() }
                        )
                        ControlCenterToggle(
                            icon = Icons.Default.FlashlightOn,
                            label = "Torch",
                            isActive = false,
                            onClick = { /* Toggle Torch */ }
                        )
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HarmonySlider(
                            value = brightness,
                            onValueChange = { controlViewModel.setBrightness(it) },
                            icon = Icons.Default.BrightnessMedium
                        )
                        HarmonySlider(
                            value = volume,
                            onValueChange = { controlViewModel.setVolume(it) },
                            icon = Icons.Default.VolumeUp
                        )
                    }
                }
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
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        // Central Phone Node
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(colors.primary.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, colors.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneIphone, null, tint = colors.primary)
                        }
                        
                        // Nearby Nodes (Tablets, Laptops - Simulated)
                        NearbyNode(Modifier.align(Alignment.TopStart), Icons.Default.Tablet)
                        NearbyNode(Modifier.align(Alignment.BottomEnd), Icons.Default.Laptop)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unique Productivity Hub
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ServiceWidget(modifier = Modifier.weight(1f), title = "Focus Stats") {
                    Column {
                        Text("Efficiency", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("85%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
                ServiceWidget(modifier = Modifier.weight(1f), title = "Upcoming") {
                    Column {
                        Text("Exam in", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("2d 4h", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Focus Music Control
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, null, tint = colors.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deep Work Lo-Fi", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Selected for focus", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                    IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
                }
            }
            
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
fun NearbyNode(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.05f), CircleShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
    }
}
