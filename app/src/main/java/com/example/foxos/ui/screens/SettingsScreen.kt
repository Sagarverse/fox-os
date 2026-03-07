package com.example.foxos.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foxos.model.CustomGesture
import com.example.foxos.model.GestureAction
import com.example.foxos.ui.components.FuturisticText
import com.example.foxos.ui.components.GestureOverlay
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.Theme
import com.example.foxos.ui.theme.FoxHarmonyBlue
import com.example.foxos.utils.GenerativeAIEngine
import com.example.foxos.viewmodel.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.IntOffset

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
    var showHiddenAppsDialog by remember { mutableStateOf(false) }

    val lockedApps by launcherViewModel.lockedApps.collectAsState()
    val allApps by launcherViewModel.allApps.collectAsState()
    
    val geminiApiKey by settingsViewModel.geminiApiKey.collectAsState()
    val gridColumns by settingsViewModel.gridColumns.collectAsState()
    val showLabels by settingsViewModel.showLabels.collectAsState()
    val doubleTapToLock by settingsViewModel.doubleTapToLock.collectAsState()
    val hapticFeedback by settingsViewModel.hapticFeedback.collectAsState()
    val showClockOnHome by settingsViewModel.showClockOnHome.collectAsState()
    val assistantName by settingsViewModel.assistantName.collectAsState()
    val geminiModel by settingsViewModel.geminiModel.collectAsState()
    val apiVerificationResult by settingsViewModel.apiVerificationResult.collectAsState()
    val availableModels by settingsViewModel.availableGeminiModels.collectAsState()
    val hiddenAppPackages by settingsViewModel.hiddenApps.collectAsState()
    val isMinimalisticMode by settingsViewModel.isMinimalisticMode.collectAsState()

    LaunchedEffect(geminiApiKey) {
        if (geminiApiKey.isNotBlank()) {
            GenerativeAIEngine.setApiKey(geminiApiKey, geminiModel)
        }
    }

    val colors = FoxLauncherTheme.colors

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        modifier = Modifier.clip(CircleShape).background(colors.onSurface.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FuturisticText(
                        text = "SYSTEM CONFIG",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // AI Section
                item { SettingsSectionHeader(Icons.Default.AutoAwesome, "NEURAL INTERFACE") }
                item {
                    SettingsItem(
                        title = "Gemini AI Core",
                        subtitle = if (geminiApiKey.isBlank()) "API Key Required" else "Connected via $geminiModel",
                        onClick = { showApiKeyDialog = true }
                    )
                }
                item {
                    SettingsTextFieldItem(
                        icon = Icons.Default.SmartToy,
                        title = "Assistant Identity",
                        value = assistantName,
                        onValueChange = { settingsViewModel.updateAssistantName(it) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Visuals
                item { SettingsSectionHeader(Icons.Default.Palette, "VISUAL ARCHITECTURE") }
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Theme", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        ThemeSelector(currentTheme) { themeViewModel.setTheme(it) }
                    }
                }
                item {
                    SettingsSliderItem(
                        title = "Neural Grid",
                        value = gridColumns.toFloat(),
                        onValueChange = { settingsViewModel.updateGridColumns(it.toInt()) },
                        valueRange = 3f..6f,
                        steps = 3,
                        valueLabel = "$gridColumns Columns"
                    )
                }
                item {
                    SettingsSwitchItem(
                        title = "Minimalist Mode",
                        subtitle = "Clean interface, no labels",
                        checked = isMinimalisticMode,
                        onCheckedChange = { settingsViewModel.setMinimalisticMode(it) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Security
                item { SettingsSectionHeader(Icons.Default.Security, "SECURITY & VAULT") }
                item {
                    SettingsItem(
                        title = "App Vault",
                        subtitle = "Biometric lock for apps",
                        onClick = { showAppVaultDialog = true }
                    )
                }
                item {
                    SettingsItem(
                        title = "Hidden Apps",
                        subtitle = "Manage ${hiddenAppPackages.size} hidden entities",
                        onClick = { showHiddenAppsDialog = true }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Gestures
                item { SettingsSectionHeader(Icons.Default.Gesture, "KINETIC GESTURES") }
                items(gestures) { gesture ->
                    GestureListItem(gesture) { gestureViewModel.deleteGesture(gesture) }
                }
                item {
                    TextButton(
                        onClick = { showAddGestureDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RECORD NEW GESTURE")
                    }
                }
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
                onToggleLock = { packageName, isLocked -> launcherViewModel.toggleAppLock(packageName, isLocked) },
                onDismiss = { showAppVaultDialog = false }
            )
        }

        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentKey = geminiApiKey,
                currentModel = geminiModel,
                availableModels = availableModels,
                verificationResult = apiVerificationResult,
                onSave = { key, model ->
                    settingsViewModel.updateGeminiModel(model)
                    settingsViewModel.updateGeminiApiKey(key)
                    showApiKeyDialog = false
                },
                onVerify = { key, model -> settingsViewModel.verifyApiKey(key, model) },
                onModelChange = { settingsViewModel.resetVerification() },
                onDismiss = { showApiKeyDialog = false }
            )
        }

        if (showHiddenAppsDialog) {
            HiddenAppsDialog(
                hiddenPackages = hiddenAppPackages,
                allApps = allApps,
                onUnhide = { settingsViewModel.unhideApp(it) },
                onDismiss = { showHiddenAppsDialog = false }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    val colors = FoxLauncherTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, color = colors.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    val colors = FoxLauncherTheme.colors
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsSliderItem(title: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int, valueLabel: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
fun SettingsTextFieldItem(icon: ImageVector, title: String, value: String, onValueChange: (String) -> Unit) {
    val colors = FoxLauncherTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(title) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    currentModel: String,
    availableModels: List<GenerativeAIEngine.ModelMetadata>,
    verificationResult: Result<String>?,
    onSave: (String, String) -> Unit,
    onVerify: (String, String) -> Unit,
    onModelChange: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    var selectedModel by remember { mutableStateOf(currentModel) }
    val clipboardManager = LocalClipboardManager.current
    val colors = FoxLauncherTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 600.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                color = if (colors.isLight) Color.White.copy(alpha = 0.9f) else Color(0xFF1A1A1A).copy(alpha = 0.85f),
                blurRadius = 40.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = FoxHarmonyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "GEMINI CONFIGURATION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { 
                            apiKey = it
                            onModelChange()
                        },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) apiKey = text
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = FoxHarmonyBlue)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FoxHarmonyBlue,
                            focusedLabelColor = FoxHarmonyBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = { onVerify(apiKey, selectedModel) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FoxHarmonyBlue.copy(alpha = 0.15f), contentColor = FoxHarmonyBlue)
                    ) {
                        Text("TEST CONNECTION", fontWeight = FontWeight.Bold)
                    }
                    
                    verificationResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (result.isSuccess) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (result.isSuccess) "Connection Successful" else "Verification Failed: ${result.exceptionOrNull()?.message ?: "Unknown Error"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "SELECT MODEL",
                        style = MaterialTheme.typography.labelMedium.copy(color = colors.onSurface.copy(alpha = 0.6f)),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(availableModels) { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { 
                                        selectedModel = model.name 
                                        onModelChange()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedModel == model.name,
                                    onClick = { selectedModel = model.name },
                                    colors = RadioButtonDefaults.colors(selectedColor = FoxHarmonyBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(model.name, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                    Text("Supports: Text, Image", style = MaterialTheme.typography.bodySmall.copy(color = colors.onSurface.copy(alpha = 0.5f)))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Button(
                            onClick = { onSave(apiKey, selectedModel) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FoxHarmonyBlue)
                        ) {
                            Text("APPLY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelector(selectedTheme: Theme, onThemeSelected: (Theme) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Theme.entries.forEach { theme ->
            item { ThemeOption(theme, theme.name, selectedTheme == theme, onThemeSelected) }
        }
    }
}

@Composable
fun ThemeOption(theme: Theme, label: String, isSelected: Boolean, onClick: (Theme) -> Unit) {
    FilterChip(selected = isSelected, onClick = { onClick(theme) }, label = { Text(label) })
}

@Composable
fun GestureListItem(gesture: CustomGesture, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(gesture.name, style = MaterialTheme.typography.bodyLarge)
                Text(gesture.action, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
        }
    }
}

@Composable
fun AddGestureDialog(onDismiss: () -> Unit, onSave: (String, String, List<Offset>) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Gesture") }, confirmButton = { Button(onClick = { if (name.isNotBlank()) onSave(name, "START_POMODORO", emptyList()) }) { Text("Save") } })
}

@Composable
fun AppVaultDialog(apps: List<com.example.foxos.model.AppInfo>, lockedPackageNames: Set<String>, onToggleLock: (String, Boolean) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("App Vault") }, text = {
        LazyColumn(modifier = Modifier.height(400.dp)) {
            items(apps) { app ->
                val isLocked = lockedPackageNames.contains(app.packageName)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(app.label)
                    Switch(checked = isLocked, onCheckedChange = { onToggleLock(app.packageName, it) })
                }
            }
        }
    }, confirmButton = { Button(onClick = onDismiss) { Text("Done") } })
}

@Composable
fun HiddenAppsDialog(hiddenPackages: Set<String>, allApps: List<com.example.foxos.model.AppInfo>, onUnhide: (String) -> Unit, onDismiss: () -> Unit) {
    val hiddenApps = allApps.filter { it.packageName in hiddenPackages }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Hidden Apps") }, text = {
        LazyColumn(modifier = Modifier.height(400.dp)) {
            items(hiddenApps) { app ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(app.label)
                    TextButton(onClick = { onUnhide(app.packageName) }) { Text("Unhide") }
                }
            }
        }
    }, confirmButton = { Button(onClick = onDismiss) { Text("Done") } })
}

fun openDefaultLauncherSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
}