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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.FuturisticText
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.data.Note
import com.example.foxos.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    val currentNoteText by viewModel.currentNoteText.collectAsState()
    val currentNote by viewModel.currentNote.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(Unit) {
        viewModel.createNewNote()
    }

    val colors = FoxLauncherTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        // We'll assume a background is already provided by the parent or we add one
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            
            // Premium Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isEditing) isEditing = false else onBack()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                FuturisticText(
                    text = if (isEditing) "QUANTUM NOTE" else "QUICK NOTES",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isEditing) {
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentNote()
                            isEditing = false
                        },
                        enabled = currentNoteText.isNotBlank(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = if (currentNoteText.isNotBlank()) 0.2f else 0.05f))
                    ) {
                        Icon(
                            Icons.Default.Save, 
                            contentDescription = "Save", 
                            tint = if (currentNoteText.isNotBlank()) colors.primary else colors.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                // Immersive Editor
                GlassPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = HarmonyShapes.large,
                    color = colors.surface.copy(alpha = 0.3f),
                    blurRadius = 30.dp
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = currentNoteText,
                        onValueChange = { viewModel.updateNoteText(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.onSurface,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                        decorationBox = { innerTextField ->
                            if (currentNoteText.isEmpty()) {
                                Text(
                                    "Capture your thoughts in the matrix...",
                                    color = colors.onSurface.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            } else {
                // Notes List
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colors.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Neural cache empty",
                                color = colors.onSurface.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    viewModel.loadNote(note)
                                    isEditing = true
                                },
                                onDelete = { noteToDelete = note }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button - Glass Style
        if (!isEditing) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.createNewNote()
                        isEditing = true
                    },
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Note")
                }
            }
        }
    }

    // Delete confirmation dialog
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FoxLauncherTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = HarmonyShapes.medium,
        color = colors.surface.copy(alpha = 0.4f),
        blurRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.content.take(120),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colors.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormat.format(Date(note.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.05f))
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}