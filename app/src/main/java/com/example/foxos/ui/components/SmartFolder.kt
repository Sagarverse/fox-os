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
import com.example.foxos.ui.components.bounceClick
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.foxos.ui.components.HarmonyAppIcon

@Composable
fun SmartFolder(
    title: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .size(160.dp)
            .bounceClick { isExpanded = true }
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

    if (isExpanded) {
        Dialog(
            onDismissRequest = { isExpanded = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)) // Semi-transparent scrim
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                com.example.foxos.ui.components.GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .heightIn(max = 500.dp)
                        .clickable( // Consume clicks so they don't close the dialog
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    blurRadius = 50.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(apps.size) { index ->
                                val app = apps[index]
                                val iconBitmap = remember(app.packageName) {
                                    app.icon?.let { it.toBitmap().asImageBitmap() }
                                }
                                HarmonyAppIcon(
                                    icon = iconBitmap,
                                    label = app.label,
                                    onClick = { 
                                        isExpanded = false
                                        onAppClick(app) 
                                    }
                                )
                            }
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
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        val iconBitmap = remember(app.packageName) {
            app.icon?.let { it.toBitmap().asImageBitmap() }
        }
        if (iconBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = iconBitmap,
                contentDescription = app.label,
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
