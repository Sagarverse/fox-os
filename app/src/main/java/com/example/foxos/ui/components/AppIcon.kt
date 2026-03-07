package com.example.foxos.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
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
    
    // Animation for scale on press
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "app_icon_scale"
    )

    // CRITICAL PERFORMANCE FIX: Move bitmap conversion out of the composition path
    val iconBitmap = remember(app.packageName) {
        app.icon?.toBitmap()?.asImageBitmap()
    }

    Box(modifier = modifier.scale(animatedScale)) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true },
                    interactionSource = interactionSource,
                    indication = null
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isPredicted) {
                    val infiniteTransition = rememberInfiniteTransition(label = "appicon_glow_transition")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "appicon_glow_scale"
                    )
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "appicon_glow_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .scale(scale)
                            .background(
                                color = Color(0xFF007DFF).copy(alpha = alpha),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                }
                
                // Enhanced Glass Icon Container
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    iconBitmap?.let { bitmap ->
                        Image(
                            painter = BitmapPainter(bitmap),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            if (showLabel) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
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
