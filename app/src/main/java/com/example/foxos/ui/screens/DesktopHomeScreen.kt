package com.example.foxos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.components.FloatingWindow
import com.example.foxos.ui.components.GlassCard
import com.example.foxos.ui.components.AppIcon
import com.example.foxos.viewmodel.ControlCenterViewModel
import com.example.foxos.viewmodel.LauncherViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DesktopHomeScreen(
    launcherViewModel: LauncherViewModel,
    controlViewModel: ControlCenterViewModel,
    onOpenSuperHub: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val suggestedApps by launcherViewModel.suggestedApps.collectAsState()
    
    // Manage opened windows in a simple list mapping app to its visibility/state
    val openWindows = remember { mutableStateListOf<AppInfo>() }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Desktop Surface Area (Empty for floating windows)
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)) {
            openWindows.forEach { app ->
                FloatingWindow(
                    title = app.label,
                    onClose = { openWindows.remove(app) }
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha=0.9f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppIcon(app = app, onClick = { launcherViewModel.launchApp(app.packageName) }, showLabel = false)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { launcherViewModel.launchApp(app.packageName) }) {
                                Text("Launch Fullscreen")
                            }
                        }
                    }
                }
            }
        }

        // Taskbar / Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Button
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Apps, contentDescription = "Start", tint = Color.White)
                    }

                    // Pinned / Open Apps
                    LazyRow(
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(suggestedApps) { app ->
                            AppIcon(
                                app = app,
                                onClick = { 
                                    if (!openWindows.contains(app)) {
                                        openWindows.add(app)
                                    }
                                },
                                showLabel = false,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // System Tray
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onOpenSuperHub) {
                            Icon(Icons.Default.Wifi, contentDescription = "Network", tint = Color.White)
                        }
                        ClockWidget()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockWidget() {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    Column(horizontalAlignment = Alignment.End) {
        Text(timeFormat.format(Date(currentTime)), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(dateFormat.format(Date(currentTime)), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}
