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

@Composable
fun GestureOverlay(
    onGestureComplete: (List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val viewConfiguration = LocalViewConfiguration.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val path = mutableListOf<Offset>()
                    path.add(down.position)
                    
                    var gestureDetected = false

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.find { it.id == down.id }
                        
                        if (change == null || change.changedToUp()) {
                            break
                        }

                        val dragAmount = change.position - change.previousPosition
                        
                        // Wait for the Main pass to see if a button consumed it.
                        val mainEvent = awaitPointerEvent(pass = PointerEventPass.Main)
                        val mainChange = mainEvent.changes.find { it.id == down.id }

                        if (mainChange != null && mainChange.isConsumed) {
                            break
                        }

                        if (gestureDetected) {
                            path.add(change.position)
                            currentPath = path.toList()
                            change.consume()
                        } else if (dragAmount.getDistance() > viewConfiguration.touchSlop) {
                            gestureDetected = true
                            path.add(change.position)
                            currentPath = path.toList()
                            change.consume()
                        }
                    }

                    if (gestureDetected && path.size > 10) {
                        onGestureComplete(path)
                    }
                    currentPath = emptyList()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (currentPath.size > 1) {
                val path = Path().apply {
                    moveTo(currentPath.first().x, currentPath.first().y)
                    currentPath.drop(1).forEach { offset ->
                        lineTo(offset.x, offset.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.Cyan.copy(alpha = 0.4f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}