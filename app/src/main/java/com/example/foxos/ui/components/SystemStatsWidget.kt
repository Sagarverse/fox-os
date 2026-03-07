package com.example.foxos.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.theme.FoxLauncherTheme
import kotlinx.coroutines.delay

@Composable
fun SystemStatsWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = FoxLauncherTheme.colors
    
    var ramUsage by remember { mutableFloatStateOf(0f) }
    var storageUsage by remember { mutableFloatStateOf(0f) }
    var ramText by remember { mutableStateOf("0 / 0 GB") }
    var storageText by remember { mutableStateOf("0 / 0 GB") }

    LaunchedEffect(Unit) {
        while (true) {
            // RAM Stats
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                
                val totalRam = memoryInfo.totalMem / (1024 * 1024 * 1024f)
                val availRam = memoryInfo.availMem / (1024 * 1024 * 1024f)
                val usedRam = totalRam - availRam
                ramUsage = (usedRam / totalRam).coerceIn(0f, 1f)
                ramText = "${String.format("%.1f", usedRam)} / ${String.format("%.1f", totalRam)} GB"

                // Storage Stats
                val stat = StatFs(Environment.getDataDirectory().path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                val availBlocks = stat.availableBlocksLong
                
                val totalStorage = (totalBlocks * blockSize) / (1024 * 1024 * 1024f)
                val availStorage = (availBlocks * blockSize) / (1024 * 1024 * 1024f)
                val usedStorage = totalStorage - availStorage
                storageUsage = (usedStorage / totalStorage).coerceIn(0f, 1f)
                storageText = "${String.format("%.1f", usedStorage)} / ${String.format("%.1f", totalStorage)} GB"
            } catch (e: Exception) {
                // Fail gracefully
            }

            delay(5000) // Update every 5 seconds
        }
    }

    GlassPanel(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface.copy(alpha = 0.3f),
        blurRadius = 20.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FuturisticText(
                    text = "SYSTEM TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = colors.primary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // RAM Section
            StatRow(title = "MEMORY (RAM)", usedText = ramText, progress = ramUsage, color = colors.primary)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage Section
            StatRow(title = "STORAGE (SSD)", usedText = storageText, progress = storageUsage, color = colors.accent)
        }
    }
}

@Composable
private fun StatRow(title: String, usedText: String, progress: Float, color: Color) {
    val colors = FoxLauncherTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.5f))
            Text(usedText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}
