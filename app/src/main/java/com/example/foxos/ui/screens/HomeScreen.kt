package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.*
import com.example.foxos.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    pomodoroViewModel: PomodoroViewModel,
    weatherViewModel: WeatherViewModel,
    voiceViewModel: VoiceAssistantViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenLock: () -> Unit,
    onOpenReference: () -> Unit
) {
    val suggestedApps by viewModel.suggestedApps.collectAsState()
    val weather by weatherViewModel.weatherInfo.collectAsState()
    val assistantState by voiceViewModel.state.collectAsState()
    val recognizedText by voiceViewModel.recognizedText.collectAsState()
    
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    val isStudyMode by viewModel.isStudyModeActive.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        NebulaBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    FuturisticText(
                        text = timeFormat.format(currentTime),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            letterSpacing = (-2).sp
                        )
                    )
                    Text(
                        text = dateFormat.format(currentTime).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    )
                }
                
                GlassCard(cornerRadius = 20.dp, modifier = Modifier.padding(bottom = 8.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(weather.temperature, color = Color(0xFF007DFF), fontWeight = FontWeight.Bold)
                        Text(weather.condition, color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SmartFolder(
                title = "Study Tools",
                apps = suggestedApps.take(4),
                onAppClick = { viewModel.launchApp(it.packageName) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IOSWidget(modifier = Modifier.weight(1f), title = "Focus") {
                    PomodoroWidget(pomodoroViewModel)
                }
                
                IOSWidget(modifier = Modifier.weight(1f), title = "Control") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickActionItem(Icons.Default.Settings, "Settings", onOpenSettings)
                        QuickActionItem(Icons.Default.BarChart, "Stats", onOpenFocus)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HarmonyDock(modifier = Modifier.padding(bottom = 24.dp)) {
                DockIcon(Icons.Default.Apps, "Drawer", onOpenDrawer)
                DockIcon(Icons.Default.Checklist, "Tasks", onOpenTasks)
                
                // Voice Assistant Trigger
                DockIcon(
                    icon = Icons.Default.Mic,
                    label = "Fox",
                    onClick = { voiceViewModel.startListening() }
                )
                
                DockIcon(Icons.Default.MenuBook, "Books", onOpenReference)
                DockIcon(
                    icon = if (isStudyMode) Icons.Default.Shield else Icons.Default.ShieldMoon,
                    label = "Focus",
                    onClick = { viewModel.toggleStudyMode() },
                    isActive = isStudyMode
                )
            }
        }

        // Assistant Overlay
        if (assistantState !is AssistantState.Idle) {
            AssistantOverlay(
                state = assistantState,
                recognizedText = recognizedText,
                onDismiss = { voiceViewModel.resetState() }
            )
        }
    }
}
