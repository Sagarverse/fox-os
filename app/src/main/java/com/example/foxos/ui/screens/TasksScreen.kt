package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.foxos.data.Task
import com.example.foxos.ui.components.FuturisticText
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.viewmodel.TaskViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    val colors = FoxLauncherTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            
            // Premium Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                FuturisticText(
                    text = "ASSIGNMENTS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Neural Input Row
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                color = colors.surface.copy(alpha = 0.2f),
                blurRadius = 40.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                        decorationBox = { innerTextField ->
                            if (newTaskTitle.isEmpty()) {
                                Text(
                                    "Initialize new mission...",
                                    color = colors.onSurface.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        },
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                viewModel.addTask(newTaskTitle)
                                newTaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (newTaskTitle.isNotBlank()) colors.primary else colors.onSurface.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Add", 
                            tint = if (newTaskTitle.isNotBlank()) Color.White else colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            var filterState by remember { mutableStateOf("All") }
            val filteredTasks = when(filterState) {
                "Pending" -> tasks.filter { !it.isCompleted }
                "Completed" -> tasks.filter { it.isCompleted }
                else -> tasks
            }

            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEach { filter ->
                    val isSelected = filterState == filter
                    GlassPanel(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { filterState = filter },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.1f),
                        borderWidth = if (isSelected) 1.dp else 0.dp,
                        borderColor = colors.primary.copy(alpha = 0.4f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.5f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Matrix
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize().padding(bottom = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = colors.onSurface.copy(alpha = 0.1f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No missions in this sector.",
                                color = colors.onSurface.copy(alpha = 0.2f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HarmonyShapes.medium)
            .clickable(onClick = onToggle),
        shape = HarmonyShapes.medium,
        color = colors.surface.copy(alpha = if (task.isCompleted) 0.2f else 0.4f),
        blurRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) colors.primary else colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (task.isCompleted) colors.onSurface.copy(alpha = 0.4f) else colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                )
                if (task.dueDate != null) {
                    Text(
                        text = "Synchronizing with timeline...",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary.copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.05f))
            ) {
                Icon(
                    Icons.Default.DeleteOutline, 
                    contentDescription = "Delete", 
                    tint = Color.Red.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}