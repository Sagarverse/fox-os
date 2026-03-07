package com.example.foxos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.foxos.model.AppInfo
import com.example.foxos.search.SearchResult
import com.example.foxos.search.SearchResultType
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.components.bounceClick

import com.example.foxos.viewmodel.ControlCenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralPulseSearch(
    isVisible: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    onClose: () -> Unit,
    controlViewModel: ControlCenterViewModel
) {
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "neuralPulse")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by animateFloatAsState(
        targetValue = if (searchQuery.isNotEmpty()) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pulseScale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        exit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        ) {
            // Background Neural Grid (Subtle)
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
                val step = 40.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(Color.White, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth = 1f)
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(Color.White, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth = 1f)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 64.dp)
                    .clickable(enabled = false) {}
            ) {
                // Advanced Neural Search Bar
                Box(contentAlignment = Alignment.CenterStart) {
                    // Quantum Pulse Ring behind the search bar
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .scale(pulseScale)
                                .drawBehind {
                                    drawRoundRect(
                                        color = colors.primary.copy(alpha = 0.2f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx()),
                                        style = Stroke(
                                            width = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), phase * 50f)
                                        )
                                    )
                                }
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { 
                            Text(
                                "Neural Intelligence Search...", 
                                color = Color.White.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 1.sp)
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                if (searchQuery.isEmpty()) Icons.Default.Search else Icons.Default.AutoAwesome, 
                                contentDescription = "Search", 
                                tint = if (searchQuery.isEmpty()) Color.White else colors.primary,
                                modifier = Modifier.size(26.dp)
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.12f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = colors.primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (searchQuery.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "INTELLIGENCE ENTITIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            "${searchResults.size} matches",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassPanel(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        blurRadius = 50.dp,
                        borderColor = Color.White.copy(alpha = 0.1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { result ->
                                NeuralResultCard(result, onResultClick, onClose, controlViewModel)
                            }
                            if (searchResults.isEmpty()) {
                                item {
                                    EmptyStateView(searchQuery)
                                }
                            }
                        }
                    }
                } else {
                    // Intelligence Command Center (Brightness)
                    val brightness by controlViewModel.brightness.collectAsState()
                    
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        blurRadius = 30.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LightMode, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("LUMINANCE CONTROL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = brightness,
                                onValueChange = { controlViewModel.setBrightness(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = colors.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Neural Suggestions
                    NeuralSuggestions(controlViewModel, onClose)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Neural Waveform Visualization (Bottom)
                NeuralWaveform(phase, colors.primary)
            }
        }
    }
}

@Composable
fun NeuralWaveform(phase: Float, color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .alpha(0.6f)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val points = 50
        val step = width / points
        
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, centerY)
        
        for (i in 0..points) {
            val x = i * step
            val freq = 2.0 // Adjust for more/less waves
            val amp = 20 * Math.sin(phase.toDouble() + i * 0.2).toFloat()
            val y = centerY + amp * Math.sin(i * 0.2 * freq).toFloat()
            path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Secondary reflection wave
        val path2 = androidx.compose.ui.graphics.Path()
        path2.moveTo(0f, centerY)
        for (i in 0..points) {
            val x = i * step
            val amp = 10 * Math.cos(phase.toDouble() * 0.5 + i * 0.1).toFloat()
            val y = centerY + amp * Math.sin(i * 0.3).toFloat()
            path2.lineTo(x, y)
        }
        drawPath(
            path = path2,
            color = color.copy(alpha = 0.3f),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun NeuralResultCard(
    result: SearchResult,
    onResultClick: (SearchResult) -> Unit,
    onClose: () -> Unit,
    controlViewModel: ControlCenterViewModel
) {
    val colors = FoxLauncherTheme.colors
    var isHovered by remember { mutableStateOf(false) }
    
    val animatedPadding by animateDpAsState(if (isHovered) 16.dp else 12.dp, label = "padding")
    val animatedBgColor by animateColorAsState(
        if (isHovered) Color.White.copy(alpha = 0.1f) else Color.Transparent, 
        label = "bg"
    )

    val wifiEnabled by controlViewModel.isWifiEnabled.collectAsState()
    val bluetoothEnabled by controlViewModel.isBluetoothEnabled.collectAsState()
    val locationEnabled by controlViewModel.isLocationEnabled.collectAsState()

    val isSettingWithToggle = result.settingKey != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBgColor)
            .clickable {
                if (!isSettingWithToggle) {
                    onResultClick(result)
                    onClose()
                }
            }
            .padding(animatedPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Futuristic Icon Container
        Box(
            modifier = Modifier
                .size(52.dp)
                .drawBehind {
                    drawRoundRect(
                        color = colors.primary.copy(alpha = 0.3f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (result.type) {
                SearchResultType.APP -> Icons.Default.Apps
                SearchResultType.SETTING -> Icons.Default.Settings
                SearchResultType.CONTACT -> Icons.Default.Person
                SearchResultType.WHATSAPP -> Icons.Default.Message
                SearchResultType.INSTAGRAM -> Icons.Default.PhotoCamera
                SearchResultType.WEB -> Icons.Default.Language
            }
            Icon(
                icon,
                contentDescription = null,
                tint = if (isHovered) colors.primary else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.subtitle.uppercase(),
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
        
        // Contextual Intelligence Dashboard (Toggles)
        if (isSettingWithToggle) {
            val isActive = when (result.settingKey) {
                "WIFI" -> wifiEnabled
                "BLUETOOTH" -> bluetoothEnabled
                "LOCATION" -> locationEnabled
                else -> false
            }
            
            Switch(
                checked = isActive,
                onCheckedChange = {
                    when (result.settingKey) {
                        "WIFI" -> controlViewModel.toggleWifi()
                        "BLUETOOTH" -> controlViewModel.toggleBluetooth()
                        "LOCATION" -> controlViewModel.toggleLocation()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        } else {
            Icon(
                Icons.Default.ArrowForward, 
                null, 
                tint = Color.White.copy(alpha = 0.3f), 
                modifier = Modifier.size(16.dp).padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun NeuralSuggestions(controlViewModel: ControlCenterViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val colors = FoxLauncherTheme.colors
    val suggestions = listOf(
        Triple("Toggle WiFi", Icons.Default.Wifi, { controlViewModel.toggleWifi() }),
        Triple("Toggle Bluetooth", Icons.Default.Bluetooth, { controlViewModel.toggleBluetooth() }),
        Triple("System Settings", Icons.Default.Settings, { 
            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
            onClose()
        }),
        Triple("Fox Customization", Icons.Default.Palette, { 
            // This is handled in MainActivity usually, but we could trigger a broadcast or shared state
            // For now, let's open Wallpaper settings as a placeholder or real action
            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
            onClose()
        })
    )

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            "NEURAL SUGGESTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        suggestions.forEach { (label, icon, action) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { action() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyStateView(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.WifiOff, 
            null, 
            tint = Color.White.copy(alpha = 0.2f), 
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No entities found for '$query'",
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
