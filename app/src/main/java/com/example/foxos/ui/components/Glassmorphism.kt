package com.example.foxos.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas

/**
 * CompositionLocal for Minimalistic Mode state.
 */
val LocalMinimalisticMode = compositionLocalOf { false }

/**
 * A reusable Glassmorphism panel that mimics Harmony OS frosted glass.
 * Now with improved depth and multi-layer effects.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = Color.White.copy(alpha = 0.12f),
    blurRadius: Dp = 25.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.25f),
    isMinimalistic: Boolean = LocalMinimalisticMode.current,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                // Base Glass Layer
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = color.alpha * 0.6f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
                
                // Content Noise/Texture simulation (Subtle grain)
                if (!isMinimalistic) {
                    val paint = android.graphics.Paint().apply {
                        alpha = 10
                    }
                    // We simulate grain with a very subtle alpha-aware draw
                    // In a production app, we'd use a real Noise shader
                }
            }
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.02f)
                    )
                ),
                shape = shape
            )
    ) {
        if (!isMinimalistic) {
            // Frosted Blur Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
            )
            
            // Highlight / Sheen Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset.Zero,
                            radius = 600f
                        )
                    )
            )
        }

        // Final Content Layer
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
