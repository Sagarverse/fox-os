package com.example.foxos.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.foxos.ui.theme.FoxHarmonyBlue
import com.example.foxos.ui.theme.DockShape
import com.example.foxos.ui.theme.WidgetShape
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.ui.theme.FoxLauncherTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout

import com.example.foxos.viewmodel.ThemeViewModel
import com.example.foxos.viewmodel.SensorViewModel
import com.example.foxos.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.graphicsLayer

// Wallpaper color mappings
private val wallpaperColors: Map<String, List<Color>> = mapOf(
    "pastel" to listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5), Color(0xFFFFFDE7)),
    "sunset" to listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFFFF8E53)),
    "ocean" to listOf(Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF6B8DD6)),
    "forest" to listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF2E7D32)),
    "midnight" to listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E), Color(0xFF16213E)),
    "cyberpunk" to listOf(Color(0xFF0D0221), Color(0xFFFF00D4), Color(0xFF00FBFF)),
    "rose" to listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF48FB1)),
    "aurora" to listOf(Color(0xFF00C9FF), Color(0xFF92FE9D), Color(0xFF00D9FF))
)

@Composable
fun HarmonyBackground(wallpaperId: String = "pastel") {
    val themeViewModel: ThemeViewModel = viewModel()
    val currentTheme by themeViewModel.theme.collectAsState()
    val colors = FoxLauncherTheme.colors
    
    // Get sensor data at top level (outside conditional)
    val sensorViewModel: SensorViewModel = viewModel()
    val tilt by sensorViewModel.tiltAngles.collectAsState()

    // Get wallpaper colors based on ID
    val bgColors = wallpaperColors[wallpaperId] ?: if (colors.isLight) {
        listOf(
            Color(0xFFE3F2FD), // Light pastel blue
            Color(0xFFF3E5F5), // Light pastel purple
            Color(0xFFFFFDE7)  // Light pastel yellow
        )
    } else {
        listOf(
            colors.background,
            colors.surface,
            colors.background
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentTheme == com.example.foxos.ui.theme.Theme.AR_CAMERA) {
            ARCameraBackground()
        } else {
            // Animate tilt values with fast spring for responsive movement
            val animatedTiltX by animateFloatAsState(
                targetValue = tilt.x, 
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ), 
                label = "tiltX"
            )
            val animatedTiltY by animateFloatAsState(
                targetValue = tilt.y, 
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ), 
                label = "tiltY"
            )
            
            // Subtle scale based on combined tilt for depth effect
            val tiltMagnitude = kotlin.math.sqrt(animatedTiltX * animatedTiltX + animatedTiltY * animatedTiltY)
            val scale = 1f + (tiltMagnitude / 300f).coerceIn(0f, 0.08f)

            // Background gradient layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        val expansion = 200.dp.roundToPx()
                        val placeable = measurable.measure(
                            constraints.copy(
                                maxWidth = constraints.maxWidth + expansion * 2,
                                maxHeight = constraints.maxHeight + expansion * 2
                            )
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(-expansion, -expansion)
                        }
                    }
                    .background(brush = Brush.verticalGradient(colors = bgColors))
                    .graphicsLayer {
                        translationX = animatedTiltX * 3f
                        translationY = animatedTiltY * 3f
                        scaleX = scale
                        scaleY = scale
                        rotationX = animatedTiltY * 0.5f
                        rotationY = -animatedTiltX * 0.5f
                    }
            )
            
            // Floating bubbles layer for visible parallax
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Bubbles move faster than background for depth
                        translationX = animatedTiltX * 5f
                        translationY = animatedTiltY * 5f
                    }
            ) {
                val bubbleColor = bgColors.getOrNull(1)?.copy(alpha = 0.3f) ?: Color.White.copy(alpha = 0.2f)
                
                // Draw floating circles at various positions
                drawCircle(bubbleColor, radius = 80.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.15f))
                drawCircle(bubbleColor, radius = 120.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.25f))
                drawCircle(bubbleColor, radius = 60.dp.toPx(), center = Offset(size.width * 0.1f, size.height * 0.5f))
                drawCircle(bubbleColor, radius = 100.dp.toPx(), center = Offset(size.width * 0.7f, size.height * 0.6f))
                drawCircle(bubbleColor, radius = 70.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.8f))
                drawCircle(bubbleColor, radius = 90.dp.toPx(), center = Offset(size.width * 0.9f, size.height * 0.85f))
            }
        }
    }
}

@Composable
fun HarmonyWidget(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val panelColor = if (colors.isLight) Color.White.copy(alpha = 0.5f) else colors.surface.copy(alpha = 0.7f)
    
    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(WidgetShape)
            .heightIn(min = 100.dp),
        shape = WidgetShape,
        color = panelColor,
        blurRadius = 30.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun HarmonyDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val dockColor = if (colors.isLight) Color.White.copy(alpha = 0.6f) else colors.surface.copy(alpha = 0.7f)
    
    GlassPanel(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(80.dp)
            .clip(DockShape),
        shape = DockShape,
        color = dockColor,
        blurRadius = 40.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun HarmonyDockIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    val colors = FoxLauncherTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(HarmonyShapes.medium)
                .background(if (isActive) colors.primary.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HarmonyAppIcon(
    icon: androidx.compose.ui.graphics.ImageBitmap?,
    label: String,
    onClick: () -> Unit,
    onSwipeUp: (() -> Unit)? = null,
    isPredicted: Boolean = false,
    showLabel: Boolean = true,
    iconShape: String = "rounded_square",
    packageName: String? = null,
    onHideApp: ((String) -> Unit)? = null,
    badgeCount: Int = 0
) {
    val context = LocalContext.current
    var isSwiping by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val colors = FoxLauncherTheme.colors
    
    // Resolve shape based on iconShape parameter
    val shape = when (iconShape) {
        "circle" -> androidx.compose.foundation.shape.CircleShape
        "rounded_square" -> RoundedCornerShape(16.dp)
        "squircle" -> RoundedCornerShape(28.dp)
        "square" -> RoundedCornerShape(4.dp)
        "teardrop" -> RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 50.dp, bottomEnd = 8.dp)
        "hexagon" -> androidx.compose.foundation.shape.CutCornerShape(8.dp)
        else -> HarmonyShapes.large
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { if (packageName != null) showMenu = true }
                )
                .then(
                    if (onSwipeUp != null) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = { isSwiping = false },
                                onDragCancel = { isSwiping = false }
                            ) { change, dragAmount ->
                                change.consume()
                                if (dragAmount.y < -30 && !isSwiping) {
                                    isSwiping = true
                                    onSwipeUp()
                                }
                            }
                        }
                    } else Modifier
                )
                .padding(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isPredicted) {
                    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "glow_scale"
                    )
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "glow_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                            .clip(shape)
                            .background(colors.primary.copy(alpha = alpha))
                    )
                }

                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = label,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(shape)
                            .background(if (colors.isLight) Color.White.copy(alpha = 0.8f) else colors.surface.copy(alpha = 0.6f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(shape)
                            .background(if (colors.isLight) Color.White.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = colors.onSurface.copy(alpha = 0.5f))
                    }
                }
                
                // Notification badge
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            if (showLabel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Context menu popup
        if (showMenu && packageName != null) {
            Popup(
                onDismissRequest = { showMenu = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // App Info
                        DropdownMenuItem(
                            text = { Text("App Info", color = colors.onSurface) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.onSurface.copy(alpha = 0.7f)
                                )
                            },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:$packageName")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        // Hide app (if callback provided)
                        if (onHideApp != null) {
                            DropdownMenuItem(
                                text = { Text("Hide App", color = colors.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onHideApp(packageName)
                                }
                            )
                        }
                        
                        // Uninstall
                        DropdownMenuItem(
                            text = { Text("Uninstall", color = Color(0xFFE57373)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373)
                                )
                            },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_DELETE).apply {
                                    data = Uri.parse("package:$packageName")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(HarmonyShapes.medium)
            .bounceClick(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = FoxHarmonyBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EdgeSidebar(
    apps: List<com.example.foxos.model.AppInfo>,
    onAppClick: (com.example.foxos.model.AppInfo) -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)),
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        color = Color.White.copy(alpha = 0.7f),
        blurRadius = 40.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            apps.forEach { app ->
                HarmonyAppIcon(
                    icon = app.icon?.let { drawable ->
                        val bitmap = android.graphics.Bitmap.createBitmap(
                            drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
                            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
                            android.graphics.Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bitmap.asImageBitmap()
                    },
                    label = "",
                    onClick = { onAppClick(app) }
                )
            }
        }
    }
}

@Composable
fun FloatingWindow(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassPanel(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
                .clip(HarmonyShapes.large),
            shape = HarmonyShapes.large,
            color = Color.White.copy(alpha = 0.8f),
            blurRadius = 50.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.clickable(onClick = onClose)
                    )
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    GlassPanel(modifier = modifier, shape = HarmonyShapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun FuturisticText(text: String, style: androidx.compose.ui.text.TextStyle, modifier: Modifier = Modifier) {
    Text(text = text, style = style, modifier = modifier)
}

@Composable
fun ControlCenterToggle(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.bounceClick(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) FoxHarmonyBlue else Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isActive) Color.White else Color.Black)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun HarmonySlider(value: Float, onValueChange: (Float) -> Unit, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = Color.Black.copy(alpha = 0.6f))
        Spacer(Modifier.width(16.dp))
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = FoxHarmonyBlue,
                activeTrackColor = FoxHarmonyBlue.copy(alpha = 0.7f),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ServiceWidget(modifier: Modifier = Modifier, title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(modifier = modifier) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f))
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ShimmerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = FoxHarmonyBlue)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
