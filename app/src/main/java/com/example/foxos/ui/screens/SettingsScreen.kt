package com.example.foxos.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.foxos.model.CustomGesture
import com.example.foxos.model.GestureAction
import com.example.foxos.ui.components.GestureOverlay
import com.example.foxos.ui.theme.Theme
import com.example.foxos.viewmodel.GestureViewModel
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.viewmodel.QuickShortcutViewModel
import com.example.foxos.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gestureViewModel: GestureViewModel,
    quickShortcutViewModel: QuickShortcutViewModel,
    launcherViewModel: LauncherViewModel,
    themeViewModel: ThemeViewModel,
    onOpenLockSettings: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val gestures by gestureViewModel.allGestures.collectAsState()
    val currentTheme by themeViewModel.theme.collectAsState()
    var showAddGestureDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FoxOS Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGestureDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Gesture")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeSelector(
                    selectedTheme = currentTheme,
                    onThemeSelected = { themeViewModel.setTheme(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("System", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Default Launcher Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Set as Default Launcher", style = MaterialTheme.typography.titleMedium)
                            Text("Replace your current home screen", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { openDefaultLauncherSettings(context) }) {
                            Text("SET")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lock Screen Shortcuts Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lock Screen Shortcuts", style = MaterialTheme.typography.titleMedium)
                            Text("Customize swipeable apps", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = onOpenLockSettings) {
                            Text("EDIT")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("Custom Gestures", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(gestures) { gesture ->
                GestureListItem(gesture, onDelete = { gestureViewModel.deleteGesture(gesture) })
            }
        }

        if (showAddGestureDialog) {
            AddGestureDialog(
                onDismiss = { showAddGestureDialog = false },
                onSave = { name, action, points ->
                    gestureViewModel.saveGesture(name, action, points)
                    showAddGestureDialog = false
                }
            )
        }
    }
}

@Composable
fun ThemeSelector(selectedTheme: Theme, onThemeSelected: (Theme) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeOption(Theme.ORANGE_BLACK, "Orange", selectedTheme == Theme.ORANGE_BLACK, onThemeSelected)
        ThemeOption(Theme.CYBERPUNK, "Cyber", selectedTheme == Theme.CYBERPUNK, onThemeSelected)
    }
}

@Composable
fun ThemeOption(theme: Theme, label: String, isSelected: Boolean, onClick: (Theme) -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = { onClick(theme) },
        label = { Text(label) }
    )
}

private fun openDefaultLauncherSettings(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun GestureListItem(gesture: CustomGesture, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(gesture.name, style = MaterialTheme.typography.bodyLarge)
                Text(gesture.action, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AddGestureDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, List<Offset>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(GestureAction.START_POMODORO.name) }
    var recordedPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Custom Gesture") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Gesture Name (e.g., 'S')") })
                Spacer(modifier = Modifier.height(8.dp))
                Text("Draw your gesture below:")
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    GestureOverlay(onGestureComplete = { recordedPoints = it })
                }
                if (recordedPoints.isNotEmpty()) {
                    Text("Gesture recorded!", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && recordedPoints.isNotEmpty()) onSave(name, selectedAction, recordedPoints) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}