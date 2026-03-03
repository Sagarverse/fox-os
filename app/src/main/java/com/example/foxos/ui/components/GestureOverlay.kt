package com.example.foxos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import com.example.foxos.ui.theme.FoxLauncherTheme

@Composable
fun GestureOverlay(
    onGestureComplete: (List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val colors = FoxLauncherTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val path = mutableListOf<Offset>()
                    path.add(down.position)
                    currentPath = path.toList()
                    
                    // Consume immediately to claim this gesture
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        
                        if (change == null || !change.pressed) {
                            break
                        }

                        path.add(change.position)
                        currentPath = path.toList()
                        change.consume()
                    }

                    if (path.size > 10) {
                        onGestureComplete(path)
                    }
                    currentPath = emptyList()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (currentPath.size > 1) {
                val gesturePath = Path().apply {
                    moveTo(currentPath.first().x, currentPath.first().y)
                    currentPath.drop(1).forEach { offset ->
                        lineTo(offset.x, offset.y)
                    }
                }
                // Draw glow effect
                drawPath(
                    path = gesturePath,
                    color = colors.primary.copy(alpha = 0.3f),
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
                // Draw main stroke
                drawPath(
                    path = gesturePath,
                    color = colors.primary,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Draw points
                currentPath.forEach { point ->
                    drawCircle(
                        color = colors.primary,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}