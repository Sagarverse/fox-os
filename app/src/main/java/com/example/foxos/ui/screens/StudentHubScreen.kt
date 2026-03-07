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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.data.Assignment
import com.example.foxos.data.Exam
import com.example.foxos.ui.components.FuturisticText
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.viewmodel.StudentHubViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.example.foxos.ui.components.SystemStatsWidget

@Composable
fun StudentHubScreen(
    viewModel: StudentHubViewModel,
    onBack: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val exams by viewModel.exams.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
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
                        text = "ACADEMIC CORE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
            
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Real-time Telemetry
                SystemStatsWidget()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Futuristic Tab Row
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    borderWidth = 1.dp,
                    borderColor = colors.onSurface.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabItem(
                            text = "EXAMS",
                            isSelected = selectedTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        TabItem(
                            text = "MISSIONS",
                            isSelected = selectedTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Crossfade(targetState = selectedTab, label = "tabContent") { tab ->
                    when (tab) {
                        0 -> ExamList(exams = exams, viewModel = viewModel)
                        1 -> AssignmentList(assignments = assignments, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.4f)
            )
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(20.dp)
                    .height(2.dp)
                    .background(colors.primary, CircleShape)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun ExamList(exams: List<Exam>, viewModel: StudentHubViewModel) {
    if (exams.isEmpty()) {
        EmptyState(message = "NO UPCOMING ASSESSMENTS", icon = Icons.Default.FactCheck)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(exams) { exam ->
                ExamItem(exam = exam, daysUntil = viewModel.getDaysUntil(exam.examDate))
            }
        }
    }
}

@Composable
private fun AssignmentList(assignments: List<Assignment>, viewModel: StudentHubViewModel) {
    if (assignments.isEmpty()) {
        EmptyState(message = "NO PENDING MISSIONS", icon = Icons.Default.AssignmentTurnedIn)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(assignments) { assignment ->
                AssignmentItem(
                    assignment = assignment,
                    isUrgent = viewModel.isUrgent(assignment.dueDate),
                    onComplete = { viewModel.markAssignmentComplete(assignment) }
                )
            }
        }
    }
}

@Composable
private fun ExamItem(exam: Exam, daysUntil: Int) {
    val colors = FoxLauncherTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface.copy(alpha = 0.3f),
        blurRadius = 25.dp,
        borderWidth = 1.dp,
        borderColor = colors.onSurface.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = colors.accent)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.subject.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = colors.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = dateFormat.format(Date(exam.examDate)),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (daysUntil == 0) "TODAY" else "$daysUntil T-MINUS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (daysUntil <= 2) Color(0xFFF44336) else colors.primary
                    )
                )
                Text("DAYS", style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun AssignmentItem(assignment: Assignment, isUrgent: Boolean, onComplete: () -> Unit) {
    val colors = FoxLauncherTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface.copy(alpha = 0.3f),
        blurRadius = 25.dp,
        borderWidth = 1.dp,
        borderColor = colors.onSurface.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onComplete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isUrgent) Color(0xFFF44336) else colors.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DEADLINE: ${dateFormat.format(Date(assignment.dueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUrgent) Color(0xFFF44336) else colors.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isUrgent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF44336).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("URGENT", color = Color(0xFFF44336), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: ImageVector) {
    val colors = FoxLauncherTheme.colors
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.onSurface.copy(alpha = 0.05f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message, 
                color = colors.onSurface.copy(alpha = 0.2f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
