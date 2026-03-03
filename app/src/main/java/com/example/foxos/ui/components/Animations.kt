package com.example.foxos.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.animation.core.spring
import androidx.compose.ui.input.pointer.PointerEventType
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
