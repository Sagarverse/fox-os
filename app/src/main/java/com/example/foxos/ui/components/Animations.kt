package com.example.foxos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.coroutineScope

/**
 * Adds a harmony OS style spring bounce effect on click/touch.
 * Fixed to properly distinguish between taps and scrolling.
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 500f
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            coroutineScope {
                awaitPointerEventScope {
                    while (true) {
                        // Wait for press
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        
                        // Wait for up or cancellation
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        
                        // Only trigger onClick if the pointer was released (not cancelled/scrolled away)
                        if (up != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onClick?.invoke()
                        }
                    }
                }
            }
        }
}

/**
 * Premium Shimmer effect for loaders or high-end interactive surfaces.
 */
fun Modifier.shimmer(
    durationMillis: Int = 2000
) = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    this.drawBehind {
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.0f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.0f),
            ),
            start = Offset(translateAnim - 200f, translateAnim - 200f),
            end = Offset(translateAnim, translateAnim)
        )
        drawRect(brush = brush)
    }
}

/**
 * Subtle pulsate effect for "live" elements like AI bubbles or focus points.
 */
fun Modifier.pulsate(
    minScale: Float = 0.98f,
    maxScale: Float = 1.02f,
    durationMillis: Int = 1500
) = composed {
    val transition = rememberInfiniteTransition(label = "pulsate")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsateScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
