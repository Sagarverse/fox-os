package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.service.FoxNotificationService
import com.example.foxos.ui.components.FuturisticText
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val notifications by FoxNotificationService.notifications.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Glass Top Bar
            GlassPanel(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = colors.surface.copy(alpha = 0.4f),
                blurRadius = 30.dp,
                borderWidth = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.onSurface.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FuturisticText(
                        text = "NEURAL CENTER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                if (notifications.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(colors.onSurface.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = colors.onSurface.copy(alpha = 0.2f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "NEURAL VOID DETECTED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurface.copy(alpha = 0.3f),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "System status: clear",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                } else {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: com.example.foxos.service.GroupedNotification) {
    val colors = FoxLauncherTheme.colors
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface.copy(alpha = 0.3f),
        blurRadius = 25.dp,
        borderWidth = 1.dp,
        borderColor = colors.onSurface.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = notification.appLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
                Text(
                    text = timeFormat.format(Date(notification.latestTime)),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notification.messages.forEach { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
