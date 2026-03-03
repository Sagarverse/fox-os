package com.example.foxos.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foxos.model.CustomGesture
import com.example.foxos.model.GestureAction
import com.example.foxos.ui.components.GestureOverlay
import com.example.foxos.ui.theme.Theme
import com.example.foxos.viewmodel.GestureViewModel
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.viewmodel.QuickShortcutViewModel
import com.example.foxos.viewmodel.ThemeViewModel
import com.example.foxos.viewmodel.SettingsViewModel
import com.example.foxos.utils.GenerativeAIEngine

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
    val settingsViewModel: SettingsViewModel = viewModel()
    val gestures by gestureViewModel.allGestures.collectAsState()
    val currentTheme by themeViewModel.theme.collectAsState()
    var showAddGestureDialog by remember { mutableStateOf(false) }
    var showAppVaultDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val lockedApps by launcherViewModel.lockedApps.collectAsState()
    val allApps by launcherViewModel.allApps.collectAsState()
    
    // Settings state
    val geminiApiKey by settingsViewModel.geminiApiKey.collectAsState()
    val gridColumns by settingsViewModel.gridColumns.collectAsState()
    val showLabels by settingsViewModel.showLabels.collectAsState()
    val doubleTapToLock by settingsViewModel.doubleTapToLock.collectAsState()
    val hapticFeedback by settingsViewModel.hapticFeedback.collectAsState()
    val showClockOnHome by settingsViewModel.showClockOnHome.collectAsState()
    val assistantName by settingsViewModel.assistantName.collectAsState()

    // Set API key when loaded
    LaunchedEffect(geminiApiKey) {
        if (geminiApiKey.isNotBlank()) {
            GenerativeAIEngine.setApiKey(geminiApiKey)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // AI Assistant Section
            item {
                SettingsSectionHeader(icon = Icons.Default.SmartToy, title = "AI Assistant")
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        icon = Icons.Default.Key,
                        title = "Gemini API Key",
                        subtitle = if (geminiApiKey.isNotBlank()) "API key configured ✓" else "Tap to add your API key",
                        onClick = { showApiKeyDialog = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsTextFieldItem(
                        icon = Icons.Default.Person,
                        title = "Assistant Name",
                        value = assistantName,
                        onValueChange = { settingsViewModel.updateAssistantName(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Appearance Section
            item {
                SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance")
            }
            
            item {
                SettingsCard {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    ThemeSelector(
                        selectedTheme = currentTheme,
                        onThemeSelected = { themeViewModel.setTheme(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSliderItem(
                        icon = Icons.Default.GridView,
                        title = "Grid Columns",
                        value = gridColumns.toFloat(),
                        valueRange = 3f..6f,
                        steps = 2,
                        onValueChange = { settingsViewModel.updateGridColumns(it.toInt()) },
                        valueLabel = "$gridColumns columns"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.Label,
                        title = "Show App Labels",
                        subtitle = "Display names below app icons",
                        checked = showLabels,
                        onCheckedChange = { settingsViewModel.updateShowLabels(it) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.Schedule,
                        title = "Show Clock on Home",
                        subtitle = "Display time and date widget",
                        checked = showClockOnHome,
                        onCheckedChange = { settingsViewModel.updateShowClockOnHome(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Behavior Section
            item {
                SettingsSectionHeader(icon = Icons.Default.TouchApp, title = "Behavior")
            }
            
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        icon = Icons.Default.ScreenLockPortrait,
                        title = "Double Tap to Lock",
                        subtitle = "Double tap home screen to turn off display",
                        checked = doubleTapToLock,
                        onCheckedChange = { settingsViewModel.updateDoubleTapToLock(it) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on touch interactions",
                        checked = hapticFeedback,
                        onCheckedChange = { settingsViewModel.updateHapticFeedback(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Security Section
            item {
                SettingsSectionHeader(icon = Icons.Default.Security, title = "Security")
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        icon = Icons.Default.Lock,
                        title = "App Vault",
                        subtitle = "${lockedApps.size} apps secured with biometrics",
                        onClick = { showAppVaultDialog = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Apps,
                        title = "Lock Screen Shortcuts",
                        subtitle = "Customize swipeable apps",
                        onClick = onOpenLockSettings
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // System Section
            item {
                SettingsSectionHeader(icon = Icons.Default.PhoneAndroid, title = "System")
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        icon = Icons.Default.Home,
                        title = "Set as Default Launcher",
                        subtitle = "Replace your current home screen",
                        onClick = { openDefaultLauncherSettings(context) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Gestures Section
            item {
                SettingsSectionHeader(icon = Icons.Default.Gesture, title = "Custom Gestures")
            }
            
            item {
                SettingsCard {
                    if (gestures.isEmpty()) {
                        Text(
                            "No custom gestures configured",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            items(gestures) { gesture ->
                GestureListItem(gesture, onDelete = { gestureViewModel.deleteGesture(gesture) })
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showAddGestureDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Custom Gesture")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // About Section  
            item {
                SettingsSectionHeader(icon = Icons.Default.Info, title = "About")
            }
            
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FoxOS Launcher", style = MaterialTheme.typography.titleMedium)
                            Text("Version 1.0", style = MaterialTheme.typography.bodySmall)
                            Text("Inspired by HarmonyOS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("🦊", style = MaterialTheme.typography.displaySmall)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
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

        if (showAppVaultDialog) {
            AppVaultDialog(
                apps = allApps,
                lockedPackageNames = lockedApps,
                onToggleLock = { packageName, isLocked ->
                    launcherViewModel.toggleAppLock(packageName, isLocked)
                },
                onDismiss = { showAppVaultDialog = false }
            )
        }

        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentKey = geminiApiKey,
                onSave = { key ->
                    settingsViewModel.updateGeminiApiKey(key)
                    GenerativeAIEngine.setApiKey(key)
                    showApiKeyDialog = false
                },
                onDismiss = { showApiKeyDialog = false }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}

@Composable
fun SettingsTextFieldItem(
    icon: ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(title) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Key, contentDescription = null) },
        title = { Text("Gemini API Key") },
        text = {
            Column {
                Text(
                    "Enter your Gemini API key to enable AI-powered assistant responses.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Get your free API key at: ai.google.dev",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeSelector(selectedTheme: Theme, onThemeSelected: (Theme) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ThemeOption(Theme.DYNAMIC, "Dynamic You", selectedTheme == Theme.DYNAMIC, onThemeSelected) }
        item { ThemeOption(Theme.HARMONY_OS, "Harmony", selectedTheme == Theme.HARMONY_OS, onThemeSelected) }
        item { ThemeOption(Theme.MINIMALIST, "Minimalist", selectedTheme == Theme.MINIMALIST, onThemeSelected) }
        item { ThemeOption(Theme.ORANGE_BLACK, "Orange", selectedTheme == Theme.ORANGE_BLACK, onThemeSelected) }
        item { ThemeOption(Theme.CYBERPUNK, "Cyber", selectedTheme == Theme.CYBERPUNK, onThemeSelected) }
        item { ThemeOption(Theme.AR_CAMERA, "Translucent AR", selectedTheme == Theme.AR_CAMERA, onThemeSelected) }
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

@Composable
fun AppVaultDialog(
    apps: List<com.example.foxos.model.AppInfo>,
    lockedPackageNames: Set<String>,
    onToggleLock: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Biometric App Vault") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(apps) { app ->
                    val isLocked = lockedPackageNames.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = isLocked,
                            onCheckedChange = { locked ->
                                onToggleLock(app.packageName, locked)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}