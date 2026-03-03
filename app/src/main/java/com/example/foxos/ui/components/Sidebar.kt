package com.example.foxos.ui.components

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.viewmodel.ControlAction
import com.example.foxos.viewmodel.ControlCenterViewModel

data class SidebarItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val badge: String? = null
)

@Composable
fun Sidebar(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenQuickNotes: () -> Unit,
    onOpenStudentHub: () -> Unit = {},
    pendingTaskCount: Int = 0,
    controlViewModel: ControlCenterViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = FoxLauncherTheme.colors
    
    // State from ViewModel
    val wifiEnabled = controlViewModel?.isWifiEnabled?.collectAsState()?.value ?: false
    val btEnabled = controlViewModel?.isBluetoothEnabled?.collectAsState()?.value ?: false
    val dndEnabled = controlViewModel?.isDndEnabled?.collectAsState()?.value ?: false
    val pendingAction = controlViewModel?.pendingAction?.collectAsState()?.value
    
    // Torch state (local since not in ViewModel)
    var torchEnabled by remember { mutableStateOf(false) }
    
    // Handle pending actions
    LaunchedEffect(pendingAction) {
        when (val action = pendingAction) {
            is ControlAction.OpenSettings -> {
                context.startActivity(action.intent)
                controlViewModel?.clearPendingAction()
            }
            else -> {}
        }
    }
    
    val taskBadge = if (pendingTaskCount > 0) pendingTaskCount.toString() else null
    
    val sidebarItems = listOf(
        SidebarItem(Icons.Default.Apps, "All Apps", onOpenDrawer),
        SidebarItem(Icons.Default.Checklist, "Tasks", onOpenTasks, badge = taskBadge),
        SidebarItem(Icons.Default.Note, "Quick Notes", onOpenQuickNotes),
        SidebarItem(Icons.Default.School, "Student Hub", onOpenStudentHub),
        SidebarItem(Icons.Default.Shield, "Focus Mode", onOpenFocus),
        SidebarItem(Icons.Default.Mic, "Assistant", onOpenAssistant),
        SidebarItem(Icons.Default.Settings, "Settings", onOpenSettings)
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -50) {
                            onDismiss()
                        }
                    }
                }
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(onClick = onDismiss)
            )
            
            // Sidebar panel
            Box(
                modifier = modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (colors.isLight) {
                                listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color.White.copy(alpha = 0.9f)
                                )
                            } else {
                                listOf(
                                    colors.surface.copy(alpha = 0.95f),
                                    colors.background.copy(alpha = 0.9f)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
                    .clickable(enabled = false, onClick = {}) // Prevent clicks from passing through
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "FoxOS User",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Welcome back!",
                                fontSize = 12.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Quick toggles
                    Text(
                        text = "Quick Toggles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Wi-Fi toggle
                        QuickToggleButton(
                            icon = Icons.Default.Wifi,
                            label = "Wi-Fi",
                            isActive = wifiEnabled,
                            onClick = { controlViewModel?.toggleWifi() }
                        )
                        
                        // Bluetooth toggle
                        QuickToggleButton(
                            icon = Icons.Default.Bluetooth,
                            label = "Bluetooth",
                            isActive = btEnabled,
                            onClick = { controlViewModel?.toggleBluetooth() }
                        )
                        
                        // Torch toggle
                        QuickToggleButton(
                            icon = Icons.Default.FlashlightOn,
                            label = "Torch",
                            isActive = torchEnabled,
                            onClick = {
                                torchEnabled = !torchEnabled
                                toggleTorch(context, torchEnabled)
                            }
                        )
                        
                        // DND toggle
                        QuickToggleButton(
                            icon = Icons.Default.DoNotDisturb,
                            label = "DND",
                            isActive = dndEnabled,
                            onClick = { controlViewModel?.toggleDnd() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Divider(
                        color = colors.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Navigation items
                    Text(
                        text = "Navigation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(sidebarItems) { item ->
                            SidebarNavItem(
                                icon = item.icon,
                                label = item.label,
                                badge = item.badge,
                                onClick = {
                                    item.onClick()
                                    onDismiss()
                                }
                            )
                        }
                    }
                    
                    Divider(
                        color = colors.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Footer - version info
                    Text(
                        text = "FoxOS Launcher v1.0",
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickToggleButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) colors.primary.copy(alpha = 0.2f)
                    else colors.onSurface.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SidebarNavItem(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun toggleTorch(context: Context, enable: Boolean) {
    try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        cameraId?.let {
            cameraManager.setTorchMode(it, enable)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
