package com.example.foxos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.foxos.data.Assignment
import com.example.foxos.data.Exam
import com.example.foxos.ui.components.GlassCard
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.StudentHubViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHubPage(viewModel: StudentHubViewModel, onBack: () -> Unit = {}) {
    val colors = FoxLauncherTheme.colors
    val exams by viewModel.exams.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = colors.onSurface)
                    }
                    Column {
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exams Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "UPCOMING EXAMS", 
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface.copy(alpha = 0.4f))
                )
                IconButton(onClick = { showAddExamDialog = true }) {
                    Icon(Icons.Default.Add, "Add Exam", tint = colors.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (exams.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No upcoming exams. Tap + to add one.",
                        color = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(exams, key = { it.id }) { exam ->
                        ExamItem(
                            exam = exam,
                            daysUntil = viewModel.getDaysUntil(exam.examDate),
                            onDelete = { viewModel.deleteExam(exam) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Assignments Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ASSIGNMENTS", 
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface.copy(alpha = 0.4f))
                )
                IconButton(onClick = { showAddAssignmentDialog = true }) {
                    Icon(Icons.Default.Add, "Add Assignment", tint = colors.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (assignments.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No pending assignments. Tap + to add one.",
                        color = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assignments, key = { it.id }) { assignment ->
                        AssignmentItem(
                            assignment = assignment,
                            isUrgent = viewModel.isUrgent(assignment.dueDate),
                            onComplete = { viewModel.markAssignmentComplete(assignment) },
                            onDelete = { viewModel.deleteAssignment(assignment) }
                        )
                    }
                }
            }
        }
    }
    
    // Add Exam Dialog
    if (showAddExamDialog) {
        AddExamDialog(
            onDismiss = { showAddExamDialog = false },
            onAdd = { subject, date ->
                viewModel.addExam(subject, date)
                showAddExamDialog = false
            }
        )
    }
    
    // Add Assignment Dialog
    if (showAddAssignmentDialog) {
        AddAssignmentDialog(
            onDismiss = { showAddAssignmentDialog = false },
            onAdd = { title, date ->
                viewModel.addAssignment(title, date)
                showAddAssignmentDialog = false
            }
        )
    }
}

@Composable
fun ExamItem(exam: Exam, daysUntil: Int, onDelete: () -> Unit) {
    val colors = FoxLauncherTheme.colors
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Event, null, tint = colors.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exam.subject, fontWeight = FontWeight.Bold, color = colors.onSurface)
                Text(
                    when {
                        daysUntil < 0 -> "Passed"
                        daysUntil == 0 -> "Today!"
                        daysUntil == 1 -> "Tomorrow"
                        else -> "In $daysUntil days"
                    },
                    color = if (daysUntil <= 3) Color.Red else colors.primary,
                    fontSize = 12.sp
                )
                Text(
                    dateFormat.format(Date(exam.examDate)),
                    color = colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = colors.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AssignmentItem(
    assignment: Assignment,
    isUrgent: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = assignment.isCompleted,
                    onCheckedChange = { if (!assignment.isCompleted) onComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.onSurface.copy(alpha = 0.5f)
                    )
                )
                Column {
                    Text(
                        assignment.title, 
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface)
                    )
                    Text(
                        dateFormat.format(Date(assignment.dueDate)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isUrgent) Color.Red else colors.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isUrgent) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = colors.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(onDismiss: () -> Unit, onAdd: (String, Long) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Exam", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(8.dp))
                    Text(dateFormat.format(Date(selectedDate)))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (subject.isNotBlank()) onAdd(subject, selectedDate) },
                        enabled = subject.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentDialog(onDismiss: () -> Unit, onAdd: (String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000L) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Assignment", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(8.dp))
                    Text(dateFormat.format(Date(selectedDate)))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onAdd(title, selectedDate) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
