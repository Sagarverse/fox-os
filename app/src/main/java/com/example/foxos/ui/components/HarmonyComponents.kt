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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import com.example.foxos.ui.theme.CyberpunkGradient
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

    val settingsViewModel: SettingsViewModel = viewModel()
    val isMinimalisticMode by settingsViewModel.isMinimalisticMode.collectAsState()
    val wallpaperStyle by settingsViewModel.wallpaperStyle.collectAsState()
    val isMinimalistic = isMinimalisticMode || LocalMinimalisticMode.current

    // Get sensor data at top level
    val sensorViewModel: SensorViewModel = viewModel()
    val tilt by sensorViewModel.tiltAngles.collectAsState()

    // Premium Color Palettes for FoxOS
    val premiumPalettes = mapOf(
        "pastel" to listOf(Color(0xFFE3F2FD), Color(0xFFF8BBD0), Color(0xFFFFFDE7), Color(0xFFE0F7FA)),
        "sunset" to listOf(Color(0xFFFF5E62), Color(0xFFFF9966), Color(0xFFFFD194), Color(0xFFF3904F)),
        "ocean" to listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364), Color(0xFF4CA1AF)),
        "forest" to listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF2E7D32), Color(0xFF8DC26F)),
        "midnight" to listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E), Color(0xFF141E30)),
        "cyberpunk" to listOf(Color(0xFF0D0221), Color(0xFFFF00D4), Color(0xFF00FBFF), Color(0xFF7122FA)),
        "nebula" to listOf(Color(0xFF23074D), Color(0xFFCC5333), Color(0xFFB91D73), Color(0xFF6A0572)),
        "lava" to listOf(Color(0xFF1F1C2C), Color(0xFF4B0082), Color(0xFFFF4500), Color(0xFFFF0000)),
        "ghost" to listOf(Color(0xFF1F1C2C), Color(0xFF928DAB), Color(0xFF44A08D), Color(0xFF000000)),
        "aurora" to listOf(Color(0xFF000428), Color(0xFF004E92), Color(0xFF00C9FF), Color(0xFF92FE9D))
    )

    val bgColors = premiumPalettes[wallpaperId] ?: if (colors.isLight) {
        premiumPalettes["pastel"]!!
    } else {
        premiumPalettes["midnight"]!!
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentTheme == com.example.foxos.ui.theme.Theme.AR_CAMERA) {
            ARCameraBackground()
        } else if (isMinimalistic) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColors.first())
            )
        } else {
            val animatedTiltX by animateFloatAsState(
                targetValue = tilt.x,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "tiltX"
            )
            val animatedTiltY by animateFloatAsState(
                targetValue = tilt.y,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "tiltY"
            )

            val tiltMagnitude = kotlin.math.sqrt(animatedTiltX * animatedTiltX + animatedTiltY * animatedTiltY)
            val currentScale = 1f + (tiltMagnitude / 400f).coerceIn(0f, 0.05f)
            
            val infiniteTransition = rememberInfiniteTransition(label = "aurora")
            val phase1 by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 2f * kotlin.math.PI.toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing)), label = "p1"
            )
            val phase2 by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 2f * kotlin.math.PI.toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "p2"
            )
            val phase3 by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 2f * kotlin.math.PI.toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(30000, easing = LinearEasing)), label = "p3"
            )

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
                        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-expansion, -expansion) }
                    }
                    .background(bgColors.first()) // Base color
                    .graphicsLayer {
                        translationX = animatedTiltX * 4f
                        translationY = animatedTiltY * 4f
                        scaleX = currentScale
                        scaleY = currentScale
                        rotationX = animatedTiltY * 0.3f
                        rotationY = -animatedTiltX * 0.3f
                    }
            ) {
                when (wallpaperStyle) {
                    "mesh" -> {
                        // Classic Mesh/Mesh Bubble Style
                        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.9f }) {
                            val w = size.width
                            val h = size.height
                            
                            // Draw floating glass bubbles/blocks
                            val bubbleCount = 8
                            for (i in 0 until bubbleCount) {
                                val bubblePhase = (phase1 + i * 1.5f) % (2f * kotlin.math.PI.toFloat())
                                val xOffset = w * 0.2f * kotlin.math.sin(bubblePhase * 0.5f)
                                val yOffset = h * 0.1f * kotlin.math.cos(bubblePhase * 0.8f)
                                
                                val centerX = w * (0.2f + (i % 3) * 0.3f) + xOffset
                                val centerY = h * (0.1f + (i / 3) * 0.3f) + yOffset
                                val radius = w * (0.15f + (i % 2) * 0.1f)
                                
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.4f),
                                            Color.White.copy(alpha = 0.1f),
                                            Color.Transparent
                                        ),
                                        center = Offset(centerX, centerY),
                                        radius = radius
                                    ),
                                    center = Offset(centerX, centerY),
                                    radius = radius
                                )
                                
                                // Subtle highlight
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.15f),
                                    radius = radius * 0.4f,
                                    center = Offset(centerX - radius * 0.2f, centerY - radius * 0.2f)
                                )
                            }
                        }
                    }
                    "bubbles" -> {
                        // Vertical Split with Floating Bubbles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            // 1. Draw vertically split background
                            // Left side is white/glass
                            drawRect(
                                color = Color.White.copy(alpha = 0.8f),
                                size = size.copy(width = w * 0.5f)
                            )
                            
                            // 2. Draw floating bubbles
                            val bubbleCount = 12
                            for (i in 0 until bubbleCount) {
                                val bubblePhase = (phase1 + i * 0.8f) % (2f * kotlin.math.PI.toFloat())
                                val speed = 0.4f + (i % 4) * 0.1f
                                
                                // Float upwards and sway
                                val xPos = (w * (i.toFloat() / bubbleCount)) + (w * 0.1f * kotlin.math.sin(bubblePhase))
                                val yOffset = (h * speed * (phase1 % 10f) / 10f)
                                val yPos = (h - (h * 0.1f + (i * 100) % h) - yOffset + h) % h
                                
                                val radius = w * (0.05f + (i % 3) * 0.04f)
                                
                                // Glass bubble effect
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.6f),
                                            Color.White.copy(alpha = 0.1f),
                                            Color.Transparent
                                        ),
                                        center = Offset(xPos, yPos),
                                        radius = radius
                                    ),
                                    center = Offset(xPos, yPos),
                                    radius = radius
                                )
                                
                                // Rim highlight
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.2f),
                                    radius = radius,
                                    center = Offset(xPos, yPos),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }
                        }
                    }
                    else -> {
                        // Premium Aurora Orbs (Default)
                        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.85f }) {
                            val w = size.width
                            val h = size.height
    
                            drawCircle(
                                brush = Brush.radialGradient(listOf(bgColors[1].copy(alpha = 0.8f), Color.Transparent)),
                                radius = w * 0.8f,
                                center = Offset(
                                    x = w * 0.5f + w * 0.3f * kotlin.math.sin(phase1),
                                    y = h * 0.2f + h * 0.2f * kotlin.math.cos(phase2)
                                )
                            )
                            
                            drawCircle(
                                brush = Brush.radialGradient(listOf(bgColors[2].copy(alpha = 0.7f), Color.Transparent)),
                                radius = w * 0.9f,
                                center = Offset(
                                    x = w * 0.2f + w * 0.4f * kotlin.math.cos(phase3),
                                    y = h * 0.8f + h * 0.2f * kotlin.math.sin(phase1)
                                )
                            )
                            
                            drawCircle(
                                brush = Brush.radialGradient(listOf(bgColors[3].copy(alpha = 0.6f), Color.Transparent)),
                                radius = w * 0.7f,
                                center = Offset(
                                    x = w * 0.8f + w * 0.2f * kotlin.math.sin(phase2),
                                    y = h * 0.6f + h * 0.3f * kotlin.math.cos(phase3)
                                )
                            )
                        }
                    }
                }
                
                // Overlay for extra depth
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, bgColors.first().copy(alpha = 0.3f), bgColors.first().copy(alpha = 0.7f))
                    )
                ))
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
        blurRadius = 30.dp,
        borderWidth = 1.dp,
        borderColor = colors.onSurface.copy(alpha = if (colors.isLight) 0.1f else 0.05f)
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmer(5000))
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FuturisticText(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
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
        blurRadius = 45.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmer(3000))
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
    onPinApp: ((String) -> Unit)? = null,
    onRemoveApp: ((String) -> Unit)? = null,
    isMinimalistic: Boolean = LocalMinimalisticMode.current,
    badgeCount: Int = 0,
    isPinned: Boolean = false
) {
    val context = LocalContext.current
    var isSwiping by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val colors = FoxLauncherTheme.colors
    
    // Scale animation for premium feel
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "harmony_icon_scale"
    )

    Box(modifier = Modifier.scale(animatedScale)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { if (packageName != null) showMenu = true },
                    interactionSource = interactionSource,
                    indication = null
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
                if (isPredicted && !isMinimalistic) {
                    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
                    val scaleValue by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "glow_scale"
                    )
                    val alphaValue by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "glow_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .graphicsLayer {
                                scaleX = scaleValue
                                scaleY = scaleValue
                                alpha = alphaValue
                            }
                            .background(
                                brush = Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.6f), Color.Transparent)),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                }

                // Glass container for icon
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.4f), Color.Transparent, Color.White.copy(alpha = 0.1f))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            bitmap = icon,
                            contentDescription = label,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Notification badge
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
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
                
                // Pin indicator
                if (isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(18.dp)
                            .background(colors.primary, CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            if (showLabel && label.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Context menu popup
        if (showMenu && packageName != null) {
            Popup(
                onDismissRequest = { showMenu = false },
                properties = PopupProperties(focusable = true)
            ) {
                GlassPanel(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface.copy(alpha = 0.95f),
                    blurRadius = 40.dp,
                    borderColor = Color.White.copy(alpha = 0.2f)
                ) {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = colors.onSurface.copy(alpha = 0.1f))

                        // App Info
                        MenuActionItem(
                            icon = Icons.Default.Info,
                            label = "Intelligence",
                            color = colors.onSurface,
                            onClick = {
                                showMenu = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:$packageName")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        // Hide app
                        if (onHideApp != null) {
                            MenuActionItem(
                                icon = Icons.Default.VisibilityOff,
                                label = "Conceal Entity",
                                color = colors.onSurface,
                                onClick = {
                                    showMenu = false
                                    onHideApp(packageName)
                                }
                            )
                        }
                        
                        // Pin to Home
                        if (onPinApp != null) {
                            MenuActionItem(
                                icon = Icons.Default.PushPin,
                                label = "Fix to Matrix",
                                color = colors.primary,
                                onClick = {
                                    showMenu = false
                                    onPinApp(packageName)
                                }
                            )
                        }

                        // Remove from Home
                        if (onRemoveApp != null) {
                            MenuActionItem(
                                icon = Icons.Default.BookmarkRemove,
                                label = "Detach Node",
                                color = MaterialTheme.colorScheme.error,
                                onClick = {
                                    showMenu = false
                                    onRemoveApp(packageName)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = colors.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(4.dp))

                        // Uninstall
                        MenuActionItem(
                            icon = Icons.Default.Delete,
                            label = "Purge Source",
                            color = Color(0xFFE57373),
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
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
private fun MenuActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
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
                    icon = remember(app.packageName) {
                        app.icon?.let { drawable ->
                            val bitmap = android.graphics.Bitmap.createBitmap(
                                drawable.intrinsicWidth.coerceAtLeast(1),
                                drawable.intrinsicHeight.coerceAtLeast(1),
                                android.graphics.Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            bitmap.asImageBitmap()
                        }
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
    val colors = FoxLauncherTheme.colors
    Text(
        text = text,
        style = style.copy(
            brush = Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.7f))),
            shadow = androidx.compose.ui.graphics.Shadow(
                color = colors.primary.copy(alpha = 0.5f),
                blurRadius = 8f,
                offset = Offset(0f, 0f)
            )
        ),
        modifier = modifier
    )
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

@Composable
fun ModularAppControl(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    GlassPanel(
        modifier = modifier
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.onSurface.copy(alpha = if (colors.isLight) 0.05f else 0.1f),
        blurRadius = 30.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$count Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
