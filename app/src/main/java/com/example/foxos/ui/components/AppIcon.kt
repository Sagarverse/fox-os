package com.example.foxos.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    isPredicted: Boolean = false,
    onHideApp: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = FoxLauncherTheme.colors
    var showMenu by remember { mutableStateOf(false) }
    
    // CRITICAL PERFORMANCE FIX: Move bitmap conversion out of the composition path
    val iconBitmap = remember(app.packageName) {
        app.icon?.toBitmap()?.asImageBitmap()
    }

    Box {
        Column(
            modifier = modifier
                .padding(4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isPredicted) {
                    val infiniteTransition = rememberInfiniteTransition(label = "appicon_glow_transition")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "appicon_glow_scale"
                    )
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "appicon_glow_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(14.dp))
                            .background(com.example.foxos.ui.theme.FoxHarmonyBlue.copy(alpha = alpha))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    iconBitmap?.let { bitmap ->
                        Image(
                            painter = BitmapPainter(bitmap),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
            if (showLabel) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        
        // Context menu popup
        if (showMenu) {
            Popup(
                onDismissRequest = { showMenu = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // App Info
                        DropdownMenuItem(
                            text = { Text("App Info", color = colors.onSurface) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.onSurface.copy(alpha = 0.7f)
                                )
                            },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${app.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        // Hide app (if callback provided)
                        if (onHideApp != null) {
                            DropdownMenuItem(
                                text = { Text("Hide App", color = colors.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onHideApp(app.packageName)
                                }
                            )
                        }
                        
                        // Uninstall
                        DropdownMenuItem(
                            text = { Text("Uninstall", color = Color(0xFFE57373)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373)
                                )
                            },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_DELETE).apply {
                                    data = Uri.parse("package:${app.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}
