package com.example.foxos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.AssistantState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

val HarmonyCorner = 28.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = HarmonyCorner,
    content: @Composable () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.surface.copy(alpha = if (colors.isLight) 0.8f else 0.4f))
            .border(
                width = 0.5.dp,
                color = colors.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun NebulaBackground() {
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.primary.copy(alpha = pulseAlpha), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = size.maxDimension * 0.8f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.accent.copy(alpha = pulseAlpha * 0.5f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                        radius = size.maxDimension * 0.6f
                    )
                )
            }
    )
}

@Composable
fun HarmonyDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun DockIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    onHoldStart: (() -> Unit)? = null,
    onHoldEnd: (() -> Unit)? = null,
    isActive: Boolean = false
) {
    val colors = FoxLauncherTheme.colors
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isActive) colors.primary else Color.White.copy(alpha = 0.1f))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var isLongPress = false
                    val longPressJob = scope.launch {
                        delay(500)
                        isLongPress = true
                        onHoldStart?.invoke()
                    }
                    
                    var upEvent = false
                    while (!upEvent) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        if (event.changes.any { it.id == down.id && it.changedToUp() }) {
                            upEvent = true
                            longPressJob.cancel()
                            if (isLongPress) {
                                onHoldEnd?.invoke()
                            } else {
                                onClick()
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (isActive) Color.White else Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun AssistantOverlay(
    state: AssistantState,
    recognizedText: String,
    onDismiss: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = recognizedText.ifEmpty { "Listening..." },
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated Waveform
            Row(
                modifier = Modifier.height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(15) { index ->
                    val heightFactor = if (state is AssistantState.Listening) {
                        0.5f + 0.5f * sin(phase + index * 0.5f)
                    } else if (state is AssistantState.Processing) {
                        0.2f + 0.1f * sin(phase * 2f + index * 0.8f)
                    } else 0.1f
                    
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight(heightFactor)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.primary, colors.accent)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun IOSWidget(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    GlassCard(modifier = modifier, cornerRadius = 28.dp) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ServiceWidget(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    IOSWidget(modifier = modifier, title = title, content = content)
}

@Composable
fun ControlCenterToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val bgColor = if (isActive) colors.primary else Color.White.copy(alpha = 0.1f)
    val tint = if (isActive) Color.White else Color.White.copy(alpha = 0.7f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun FloatingWindow(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset(100f, 200f)) }
    val colors = FoxLauncherTheme.colors
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .size(width = 320.dp, height = 450.dp)
            .clip(RoundedCornerShape(HarmonyCorner))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(HarmonyCorner))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                }
            }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = colors.onSurface, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = colors.onSurface)
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { content() }
        }
    }
}

@Composable
fun EdgeSidebar(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(100.dp)
            .padding(vertical = 64.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
            .background(colors.surface.copy(alpha = 0.9f))
            .border(0.5.dp, colors.onSurface.copy(alpha = 0.1f), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    showLabel = false
                )
            }
        }
    }
}

@Composable
fun FuturisticText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    glowColor: Color? = null
) {
    val colors = FoxLauncherTheme.colors
    val shadowColor = glowColor ?: colors.primary
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = colors.onSurface,
            shadow = Shadow(
                color = shadowColor.copy(alpha = 0.3f),
                blurRadius = 8f,
                offset = Offset(0f, 0f)
            )
        )
    )
}

@Composable
fun MacDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    HarmonyDock(modifier = modifier, content = content)
}

@Composable
fun ShimmerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.primary.copy(alpha = 0.15f))
            .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.onSurface.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - 200f, shimmerTranslate - 200f),
                    end = Offset(shimmerTranslate, shimmerTranslate)
                )
                drawRect(brush)
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun HarmonySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.onSurface.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp)
    ) {
        Icon(icon, null, tint = colors.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = colors.onSurface,
                activeTrackColor = colors.onSurface,
                inactiveTrackColor = colors.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = FoxLauncherTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.onSurface.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.onSurface, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = colors.onSurface, fontSize = 12.sp)
    }
}

@Composable
fun NeonGlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val shadowColor = glowColor ?: colors.primary
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(shadowColor.copy(alpha = alpha * 0.2f), Color.Transparent),
                        center = center,
                        radius = size.maxDimension
                    )
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.sweepGradient(
                    listOf(shadowColor.copy(alpha = 0.1f), shadowColor, shadowColor.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        GlassCard(cornerRadius = 24.dp) {
            content()
        }
    }
}
