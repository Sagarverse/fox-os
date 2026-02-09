package com.example.foxos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foxos.viewmodel.PomodoroViewModel

@Composable
fun PomodoroWidget(viewModel: PomodoroViewModel) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isBreak by viewModel.isBreak.collectAsState()

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreak) MaterialTheme.colorScheme.tertiaryContainer 
                            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBreak) "Break Time" else "Focus Session",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayMedium
            )
            Row {
                Button(onClick = { 
                    if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() 
                }) {
                    Text(if (isRunning) "Pause" else "Start")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { viewModel.resetTimer() }) {
                    Text("Reset")
                }
            }
        }
    }
}