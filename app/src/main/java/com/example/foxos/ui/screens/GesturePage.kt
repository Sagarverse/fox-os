package com.example.foxos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.GestureOverlay
import androidx.compose.ui.geometry.Offset

@Composable
fun GesturePage(
    onGestureComplete: (List<Offset>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GESTURE WORKSPACE",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Draw your custom pattern to trigger actions",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f))
            )
            
            Box(modifier = Modifier.weight(1f)) {
                GestureOverlay(onGestureComplete = onGestureComplete)
            }
            
            Text(
                text = "Swipe left to return home",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}