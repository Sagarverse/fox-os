package com.example.foxos.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RenderEffect

/**
 * A reusable Glassmorphism panel that mimics Harmony OS frosted glass.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = Color.White.copy(alpha = 0.4f),
    blurRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                // On Android 12+, we can use RenderEffect for true backdrop blur
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        // This blurs the content behind this layer
                        // but since we want to blur the background *behind* us, 
                        // we'd normally need to apply this to the parent or a separate layer.
                        // For simplicity in this widget, we'll use a semi-transparent fill.
                    }
                } else Modifier
            )
            .background(color)
    ) {
        // Inner content without blur (blurring the main Box blurs the content)
        content()
    }
}
