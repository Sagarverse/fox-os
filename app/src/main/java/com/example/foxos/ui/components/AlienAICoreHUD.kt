package com.example.foxos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.AssistantState
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AlienAICoreHUD(
    state: AssistantState,
    recognizedText: String,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onTextSubmit: (String) -> Unit = {},
    rmsLevel: Float = 0f,
    isMinimalistic: Boolean = LocalMinimalisticMode.current,
    onCancelTimer: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val pulseAlpha = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }
    val colors = FoxLauncherTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "aiCore")
    
    var showKeyboardInput by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

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
    
    // Dynamic background gradient based on state
    val bgGlowColor = when (state) {
        is AssistantState.Listening -> colors.accent.copy(alpha = 0.3f)
        is AssistantState.Processing -> colors.primary.copy(alpha = 0.4f)
        is AssistantState.Generating -> Color.Cyan.copy(alpha = 0.3f)
        is AssistantState.Success -> Color.Green.copy(alpha = 0.2f)
        is AssistantState.Error -> Color.Red.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    val animatedBgAlpha by animateFloatAsState(
        targetValue = if (state !is AssistantState.Idle) 1f else 0f,
        animationSpec = tween(1000),
        label = "bgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bgGlowColor.copy(alpha = bgGlowColor.alpha * animatedBgAlpha),
                            Color.Black
                        )
                    )
                )
            }
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { 
                    if (showKeyboardInput) showKeyboardInput = false
                    else onDismiss()
                }
            ),
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
                    .clip(CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            onReset()
                            // Quantum Pulse Animation
                            pulseAlpha.snapTo(0.6f)
                            pulseScale.snapTo(1f)
                            launch {
                                pulseAlpha.animateTo(0f, tween(600))
                            }
                            launch {
                                pulseScale.animateTo(2f, tween(600, easing = FastOutSlowInEasing))
                            }
                        }
                    }
                    .drawBehind {
                        val radius = size.width / 2

                        // Quantum Pulse Ring
                        if (pulseAlpha.value > 0f) {
                            drawCircle(
                                color = colors.accent.copy(alpha = pulseAlpha.value),
                                radius = radius * pulseScale.value,
                                style = Stroke(width = 4f)
                            )
                        }

                        // Inner Energy Core
                        val corePulse = if (isMinimalistic) {
                            0.9f
                        } else if (state is AssistantState.Listening) {
                            0.8f + 0.2f * sin(phase * 4)
                        } else if (state is AssistantState.Processing) {
                            0.7f + 0.3f * sin(phase * 8)
                        } else {
                            0.9f + 0.1f * sin(phase)
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    (if (state is AssistantState.Generating) Color.Cyan else colors.accent).copy(alpha = 0.8f),
                                    Color.Transparent
                                ),
                                radius = radius * corePulse
                            ),
                            radius = radius * corePulse
                        )

                        // Energy Flow Particles (Simulated)
                        if (!isMinimalistic && (state is AssistantState.Listening || state is AssistantState.Processing)) {
                            val particleCount = 8
                            for (i in 0 until particleCount) {
                                val angle = (phase * (i + 1) * 2f) % (2f * PI.toFloat())
                                val pRadius = radius * (0.6f + 0.3f * sin(phase + i))
                                val x = center.x + pRadius * kotlin.math.cos(angle)
                                val y = center.y + pRadius * kotlin.math.sin(angle)
                                drawCircle(
                                    color = colors.accent.copy(alpha = 0.6f),
                                    radius = 4f,
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }

                        // Rotating Quantum Rings
                        if (!isMinimalistic) {
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
                        } else {
                            // Static Rings for minimalistic mode
                            drawCircle(
                                color = colors.primary.copy(alpha = 0.3f),
                                radius = radius * 0.8f,
                                style = Stroke(width = 2f)
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

            // Status Text + Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 400.dp)
            ) {
                // Real-time waveform bars during Listening
                if (state is AssistantState.Listening) {
                    val barCount = 20
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until barCount) {
                            val barPhase = phase + i * 0.4f
                            val barHeight = (0.2f + 0.8f * rmsLevel * (0.5f + 0.5f * sin(barPhase * 3f))).coerceIn(0.05f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(barHeight)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(colors.accent, colors.primary.copy(alpha = 0.5f))
                                        )
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state is AssistantState.Success && state.category != null) {
                    val categoryIcon = when (state.category) {
                        "HARDWARE" -> Icons.Default.Settings
                        "APPS" -> Icons.Default.Apps
                        "COMM" -> Icons.Default.Phone
                        "PRODUCTIVITY" -> Icons.Default.Edit
                        "MEDIA" -> Icons.Default.Collections
                        "UTILITY" -> Icons.Default.Calculate
                        else -> null
                    }
                    if (categoryIcon != null) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(48.dp).padding(bottom = 16.dp)
                        )
                    }
                }

                val displayText = when (state) {
                    is AssistantState.Listening -> recognizedText.ifEmpty { "Listening..." }
                    is AssistantState.Processing -> "Processing...\n\"$recognizedText\""
                    is AssistantState.Generating -> state.partialText
                    is AssistantState.Success -> state.response
                    is AssistantState.Error -> state.message
                    else -> "Tap the mic to start"
                }

                Text(
                    text = parseMarkdown(displayText),
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

                Spacer(modifier = Modifier.height(32.dp))

                if (showKeyboardInput) {
                    // Glassmorphic Text Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, colors.accent.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = colors.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(colors.accent),
                            decorationBox = { innerTextField ->
                                if (textInput.isEmpty()) {
                                    Text(
                                        "Type a command...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = colors.primary.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                innerTextField()
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textInput.isNotBlank()) {
                                        onTextSubmit(textInput)
                                        textInput = ""
                                        showKeyboardInput = false
                                    }
                                }
                            )
                        )
                        
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onTextSubmit(textInput)
                                    textInput = ""
                                    showKeyboardInput = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, null, tint = colors.accent)
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    // Keyboard Toggle Button
                    IconButton(
                        onClick = { showKeyboardInput = true },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Keyboard Input",
                            tint = colors.accent.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
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

private fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text.substring(i))
                        break
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1 && !text.startsWith("**", end)) {
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
