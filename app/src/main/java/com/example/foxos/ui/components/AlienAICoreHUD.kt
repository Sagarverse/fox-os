package com.example.foxos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.AssistantState
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AlienAICoreHUD(
    state: AssistantState,
    recognizedText: String,
    onDismiss: () -> Unit,
    onCancelTimer: (() -> Unit)? = null
) {
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "aiCore")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Check if we're showing a timer
    val isTimerState = state is AssistantState.Timer
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { 
                if (!isTimerState) onDismiss() 
            },
        contentAlignment = Alignment.Center
    ) {
        if (isTimerState) {
            val timerState = state as AssistantState.Timer
            TimerDisplay(
                totalSeconds = timerState.totalSeconds,
                remainingSeconds = timerState.remainingSeconds,
                label = timerState.label,
                colors = colors,
                phase = phase,
                onCancel = onCancelTimer ?: onDismiss
            )
        } else {
            // Regular AI Core Visuals
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .drawBehind {
                        val radius = size.width / 2

                        // Inner Energy Core
                        val corePulse = if (state is AssistantState.Listening) {
                            0.8f + 0.2f * sin(phase * 4)
                        } else if (state is AssistantState.Processing) {
                            0.7f + 0.3f * sin(phase * 8)
                        } else {
                            0.9f + 0.1f * sin(phase)
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.accent.copy(alpha = 0.8f), Color.Transparent),
                                radius = radius * corePulse
                            ),
                            radius = radius * corePulse
                        )

                        // Rotating Quantum Rings
                        rotate(rotation) {
                            drawCircle(
                                color = colors.primary,
                                radius = radius * 0.8f,
                                style = Stroke(
                                    width = 4f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 40f), phase * 10f)
                                )
                            )
                        }

                        rotate(-rotation * 1.5f) {
                            drawCircle(
                                color = colors.primary.copy(alpha = 0.5f),
                                radius = radius * 0.9f,
                                style = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f)
                                )
                            )
                        }

                        // Outer containment field
                        drawCircle(
                            color = colors.primary.copy(alpha = 0.2f),
                            radius = radius,
                            style = Stroke(width = 1f)
                        )
                    }
            )

            // Status Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 400.dp)
            ) {
                val displayText = when (state) {
                    is AssistantState.Listening -> recognizedText.ifEmpty { "Listening..." }
                    is AssistantState.Processing -> "Processing...\n\"$recognizedText\""
                    is AssistantState.Generating -> state.partialText
                    is AssistantState.Success -> state.response
                    is AssistantState.Error -> state.message
                    else -> "Tap the mic to start"
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                
                val statusText = when (state) {
                    is AssistantState.Listening -> "MIC ACTIVE"
                    is AssistantState.Processing -> "THINKING"
                    is AssistantState.Generating -> "RESPONDING"
                    is AssistantState.Success -> "TAP TO DISMISS"
                    is AssistantState.Idle -> "READY"
                    is AssistantState.Error -> "TAP TO DISMISS"
                    is AssistantState.Timer -> "TIMER ACTIVE"
                }
                
                Text(
                    text = "[$statusText]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.accent,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerDisplay(
    totalSeconds: Int,
    remainingSeconds: Int,
    label: String,
    colors: com.example.foxos.ui.theme.FoxThemeColors,
    phase: Float,
    onCancel: () -> Unit
) {
    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    
    val pulseScale by animateFloatAsState(
        targetValue = if (remainingSeconds <= 10 && remainingSeconds > 0) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val timerColor = when {
        remainingSeconds <= 10 -> Color(0xFFFF5252)
        remainingSeconds <= 30 -> Color(0xFFFFB74D)
        else -> colors.primary
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Timer label
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = colors.accent,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Circular progress timer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .drawBehind {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.width - strokeWidth) / 2
                    
                    // Background circle
                    drawCircle(
                        color = timerColor.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        )
                    )
                    
                    // Inner glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                timerColor.copy(alpha = 0.3f * (0.8f + 0.2f * sin(phase * 2))),
                                Color.Transparent
                            ),
                            radius = radius * 0.8f
                        ),
                        radius = radius * 0.8f
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            ) {
                // Time display
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = timerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp
                    )
                )
                
                if (remainingSeconds == 0) {
                    Text(
                        text = "DONE!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Cancel button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel Timer",
                tint = colors.accent,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "TAP TO CANCEL",
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.accent.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )
        )
    }
}
