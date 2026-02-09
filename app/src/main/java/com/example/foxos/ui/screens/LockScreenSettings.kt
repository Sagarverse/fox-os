package com.example.foxos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foxos.model.AppInfo
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.viewmodel.QuickShortcutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenSettings(
    shortcutViewModel: QuickShortcutViewModel,
    launcherViewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val shortcuts by shortcutViewModel.shortcuts.collectAsState()
    val allApps by launcherViewModel.allApps.collectAsState()
    var showAppSelection by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lock Screen Shortcuts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Choose up to 4 apps to show on your lock screen.")
            Spacer(modifier = Modifier.height(16.dp))

            // Display 4 slots for shortcuts
            (0..3).forEach { position ->
                val shortcut = shortcuts.find { it.position == position }
                val appInfo = shortcut?.let { s -> allApps.find { it.packageName == s.packageName } }

                ShortcutItem(
                    position = position,
                    appInfo = appInfo,
                    onClick = { showAppSelection = position },
                    onClear = { shortcutViewModel.clearShortcut(position) }
                )
            }
        }

        if (showAppSelection != null) {
            AppSelectionDialog(
                apps = allApps,
                onDismiss = { showAppSelection = null },
                onAppSelected = {
                    shortcutViewModel.setShortcut(showAppSelection!!, it.packageName)
                    showAppSelection = null
                }
            )
        }
    }
}

@Composable
fun ShortcutItem(
    position: Int,
    appInfo: AppInfo?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (appInfo != null) {
                Text(appInfo.label, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onClear, colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("Clear")
                }
            } else {
                Text("Slot ${position + 1}", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun AppSelectionDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppSelected: (AppInfo) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an App") },
        text = {
            LazyColumn {
                items(apps) { app ->
                    TextButton(onClick = { onAppSelected(app) }) {
                        Text(app.label)
                    }
                }
            }
        },
        confirmButton = {}
    )
}