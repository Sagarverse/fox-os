package com.example.foxos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foxos.ui.components.GlassCard
import com.example.foxos.viewmodel.UsageViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusDashboard(
    viewModel: UsageViewModel,
    onBack: () -> Unit
) {
    val usageStats by viewModel.usageStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Dashboard") },
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
            Text("Daily App Usage", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(usageStats) { stats ->
                    UsageItem(stats.packageName, stats.totalTimeInForeground)
                }
            }
        }
    }
}

@Composable
fun UsageItem(packageName: String, timeMillis: Long) {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
    
    GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(packageName.split(".").last(), style = MaterialTheme.typography.bodyLarge)
            Text("${hours}h ${minutes}m", style = MaterialTheme.typography.bodyMedium)
        }
    }
}