package com.example.foxos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.theme.FoxLauncherTheme

/**
 * HarmonyOS-style Service Card - Expandable widget card
 */
@Composable
fun HarmonyServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    iconTint: Color = FoxLauncherTheme.colors.primary,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val colors = FoxLauncherTheme.colors
    
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardHeight"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isExpanded) 12.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(24.dp),
                ambientColor = iconTint.copy(alpha = 0.3f),
                spotColor = iconTint.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onExpandToggle() }
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(iconTint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface
                            )
                            if (subtitle.isNotEmpty()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS-style Quick Toggle Button
 */
@Composable
fun HarmonyQuickToggle(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colors = FoxLauncherTheme.colors
    val bgColor by animateColorAsState(
        targetValue = if (isActive) colors.primary else colors.surface.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "toggleBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive) Color.White else colors.onSurface.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "toggleIcon"
    )

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

/**
 * HarmonyOS-style Control Center Panel
 */
@Composable
fun HarmonyControlCenter(
    modifier: Modifier = Modifier,
    wifiEnabled: Boolean = false,
    bluetoothEnabled: Boolean = false,
    mobileDataEnabled: Boolean = false,
    airplaneModeEnabled: Boolean = false,
    flashlightEnabled: Boolean = false,
    dndEnabled: Boolean = false,
    brightness: Float = 0.5f,
    onWifiToggle: () -> Unit = {},
    onBluetoothToggle: () -> Unit = {},
    onMobileDataToggle: () -> Unit = {},
    onAirplaneModeToggle: () -> Unit = {},
    onFlashlightToggle: () -> Unit = {},
    onDndToggle: () -> Unit = {},
    onBrightnessChange: (Float) -> Unit = {}
) {
    val colors = FoxLauncherTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.surface.copy(alpha = 0.95f),
                        colors.surface.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        // Quick Toggles Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HarmonyQuickToggle(
                icon = Icons.Default.Wifi,
                label = "Wi-Fi",
                isActive = wifiEnabled,
                onClick = onWifiToggle
            )
            HarmonyQuickToggle(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth",
                isActive = bluetoothEnabled,
                onClick = onBluetoothToggle
            )
            HarmonyQuickToggle(
                icon = Icons.Default.NetworkCell,
                label = "Mobile",
                isActive = mobileDataEnabled,
                onClick = onMobileDataToggle
            )
            HarmonyQuickToggle(
                icon = Icons.Default.AirplanemodeActive,
                label = "Airplane",
                isActive = airplaneModeEnabled,
                onClick = onAirplaneModeToggle
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HarmonyQuickToggle(
                icon = Icons.Default.FlashlightOn,
                label = "Flashlight",
                isActive = flashlightEnabled,
                onClick = onFlashlightToggle
            )
            HarmonyQuickToggle(
                icon = Icons.Default.DoNotDisturb,
                label = "DND",
                isActive = dndEnabled,
                onClick = onDndToggle
            )
            HarmonyQuickToggle(
                icon = Icons.Default.ScreenRotation,
                label = "Rotate",
                isActive = false,
                onClick = {}
            )
            HarmonyQuickToggle(
                icon = Icons.Default.LocationOn,
                label = "Location",
                isActive = true,
                onClick = {}
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Brightness Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BrightnessLow,
                    contentDescription = "Brightness",
                    tint = colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.onSurface.copy(alpha = 0.2f)
                    )
                )
                Icon(
                    Icons.Default.BrightnessHigh,
                    contentDescription = "Brightness High",
                    tint = colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * HarmonyOS-style Weather Card
 */
@Composable
fun HarmonyWeatherCard(
    modifier: Modifier = Modifier,
    temperature: String,
    condition: String,
    location: String = "Current Location",
    high: String = "",
    low: String = ""
) {
    val colors = FoxLauncherTheme.colors
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.surface.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                    color = colors.onSurface
                )
                Text(
                    text = condition,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                if (high.isNotEmpty() && low.isNotEmpty()) {
                    Text(
                        text = "H:$high  L:$low",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = when {
                        condition.contains("sun", ignoreCase = true) -> Icons.Default.WbSunny
                        condition.contains("cloud", ignoreCase = true) -> Icons.Default.Cloud
                        condition.contains("rain", ignoreCase = true) -> Icons.Default.WaterDrop
                        else -> Icons.Default.WbSunny
                    },
                    contentDescription = condition,
                    tint = colors.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * HarmonyOS-style Floating Dock
 */
@Composable
fun HarmonyFloatingDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val colors = FoxLauncherTheme.colors
    
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface.copy(alpha = 0.92f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * HarmonyOS-style Dock Icon
 */
@Composable
fun HarmonyFloatingDockIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    badgeCount: Int = 0
) {
    val colors = FoxLauncherTheme.colors
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) colors.primary.copy(alpha = 0.15f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Badge
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

/**
 * HarmonyOS-style Music Player Card
 */
@Composable
fun HarmonyMusicCard(
    modifier: Modifier = Modifier,
    title: String,
    artist: String,
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val colors = FoxLauncherTheme.colors
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.surface.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Album",
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (title == "Not Playing") "No Music" else title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (artist.isEmpty()) "Tap to play" else artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
