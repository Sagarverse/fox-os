package com.example.foxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes

/**
 * A Large Interactive Folder (2x2 grid) that sits on the Home Screen.
 * Apps inside can be clicked directly without opening the folder.
 * If there are more than 4 apps, the 4th slot acts as an "expand" button or shows smaller icons.
 */
@Composable
fun InteractiveLargeFolder(
    title: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FoxLauncherTheme.colors

    GlassPanel(
        modifier = modifier
            .widthIn(max = 180.dp)
            .heightIn(max = 180.dp)
            .clickable { onExpandClick() }, // Clicking the background expands
        shape = HarmonyShapes.large,
        color = Color.White.copy(alpha = 0.2f),
        blurRadius = 30.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                    color = colors.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 2x2 Grid Layout for Apps inside the folder
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row (Apps 0 and 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (apps.isNotEmpty()) {
                        FolderAppItem(app = apps[0], onClick = { onAppClick(apps[0]) })
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                    if (apps.size > 1) {
                        FolderAppItem(app = apps[1], onClick = { onAppClick(apps[1]) })
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                }

                // Bottom Row (Apps 2 and 3/Expand)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (apps.size > 2) {
                        FolderAppItem(app = apps[2], onClick = { onAppClick(apps[2]) })
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                    
                    if (apps.size > 4) {
                        // 4th slot shows a mini-grid of the remaining apps to indicate expansion
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .bounceClick { onExpandClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.size(40.dp).padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                    MiniIcon(app = apps.getOrNull(3))
                                    MiniIcon(app = apps.getOrNull(5))
                                }
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                    MiniIcon(app = apps.getOrNull(4))
                                    MiniIcon(app = apps.getOrNull(6))
                                }
                            }
                        }
                    } else if (apps.size > 3) {
                        FolderAppItem(app = apps[3], onClick = { onAppClick(apps[3]) })
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderAppItem(app: AppInfo, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        app.icon?.let { it.toBitmap().asImageBitmap() }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .width(56.dp)
    ) {
        if (iconBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(40.dp)
                    .clip(HarmonyShapes.medium)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(HarmonyShapes.medium)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
        Text(
            text = app.label,
            fontSize = 9.sp,
            color = Color.Black.copy(alpha = 0.8f),
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MiniIcon(app: AppInfo?) {
    if (app != null) {
        val iconBitmap = remember(app.packageName) {
            app.icon?.let { it.toBitmap().asImageBitmap() }
        }
        if (iconBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = iconBitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray))
        }
    } else {
        Spacer(Modifier.size(16.dp))
    }
}
