package com.example.foxos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.GlassCard
import com.example.foxos.ui.theme.FoxLauncherTheme

@Composable
fun StudentHubPage() {
    val colors = FoxLauncherTheme.colors
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "STUDENT HUB",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = colors.primary
                )
            )
            Text(
                "Upcoming Deadlines & Exams",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.onSurface.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Exam Countdown Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = colors.primary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Final Mathematics Exam", fontWeight = FontWeight.Bold, color = colors.onSurface)
                        Text("In 12 days", color = colors.primary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Urgent Assignments
            Text("URGENT TASKS", style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface.copy(alpha = 0.4f)))
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    AssignmentItem("Physics Lab Report", "Today, 11:59 PM", true)
                }
                item {
                    AssignmentItem("History Essay", "Tomorrow", false)
                }
                item {
                    AssignmentItem("Computer Science Project", "Friday", false)
                }
            }
        }
    }
}

@Composable
fun AssignmentItem(title: String, dueDate: String, isUrgent: Boolean) {
    val colors = FoxLauncherTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface))
                Text(dueDate, style = MaterialTheme.typography.labelSmall.copy(color = if(isUrgent) Color.Red else colors.onSurface.copy(alpha = 0.5f)))
            }
            if (isUrgent) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}
