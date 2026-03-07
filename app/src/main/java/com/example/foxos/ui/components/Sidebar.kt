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
import com.example.foxos.ui.components.GlassPanel
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
    onOpenNotifications: () -> Unit = {},
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
        SidebarItem(Icons.Default.Notifications, "Notifications", onOpenNotifications),
        SidebarItem(Icons.Default.School, "Student Hub", onOpenStudentHub),
        SidebarItem(Icons.Default.Checklist, "Tasks", onOpenTasks, badge = taskBadge),
        SidebarItem(Icons.Default.Note, "Quick Notes", onOpenQuickNotes),
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
            GlassPanel(
                modifier = modifier
                    .fillMaxHeight()
                    .width(300.dp),
                shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                color = colors.surface.copy(alpha = 0.45f),
                blurRadius = 40.dp,
                borderWidth = 1.dp,
                borderColor = colors.onSurface.copy(alpha = 0.08f)
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        GlassPanel(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = if (isActive) colors.primary.copy(alpha = 0.15f) else colors.onSurface.copy(alpha = 0.03f),
            blurRadius = 10.dp,
            borderWidth = if (isActive) 1.5.dp else 1.dp,
            borderColor = if (isActive) colors.primary.copy(alpha = 0.3f) else colors.onSurface.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.5f)
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.onSurface.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(18.dp)
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
