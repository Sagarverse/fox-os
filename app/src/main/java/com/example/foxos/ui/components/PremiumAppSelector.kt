package com.example.foxos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme

@Composable
fun PremiumAppSelector(
    allApps: List<AppInfo>,
    selectedApps: Set<String>,
    title: String,
    maxApps: Int = Int.MAX_VALUE,
    onDismiss: () -> Unit,
    onAppsSelected: (Set<String>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedApps) }
    var searchQuery by remember { mutableStateOf("") }
    val colors = FoxLauncherTheme.colors

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isEmpty()) allApps
        else allApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

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
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(32.dp),
                color = colors.surface.copy(alpha = 0.9f),
                blurRadius = 40.dp
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    
                    if (maxApps < Int.MAX_VALUE) {
                        Text(
                            text = "Select up to $maxApps apps (${currentSelection.size}/$maxApps)",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search apps...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.onSurface.copy(alpha = 0.05f),
                            unfocusedContainerColor = colors.onSurface.copy(alpha = 0.05f),
                            focusedIndicatorColor = colors.primary,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps) { app ->
                            val isSelected = currentSelection.contains(app.packageName)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        if (isSelected) {
                                            currentSelection = currentSelection - app.packageName
                                        } else if (currentSelection.size < maxApps) {
                                            currentSelection = currentSelection + app.packageName
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.onSurface.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = app.icon.toBitmap().asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.primary else colors.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { 
                            onAppsSelected(currentSelection)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Confirm Selection", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
