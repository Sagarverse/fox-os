package com.example.foxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme

@Composable
fun SmartFolder(
    title: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors
    GlassCard(
        modifier = modifier.size(160.dp),
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            
            // 2x2 Grid of Apps
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    apps.take(2).forEach { app ->
                        AppIconSmall(app = app, onClick = { onAppClick(app) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (apps.size > 2) {
                        apps.drop(2).take(2).forEach { app ->
                            AppIconSmall(app = app, onClick = { onAppClick(app) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppIconSmall(app: AppInfo, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AppIcon(app = app, onClick = onClick, showLabel = false, modifier = Modifier.size(32.dp))
    }
}
