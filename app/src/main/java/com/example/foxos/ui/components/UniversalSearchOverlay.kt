package com.example.foxos.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.foxos.model.AppInfo
import com.example.foxos.ui.theme.FoxLauncherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchOverlay(
    isVisible: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onClose: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)) // Blur effect
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 64.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search apps, contacts, settings...", color = Color.White.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedBorderColor = FoxLauncherTheme.colors.primary,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = FoxLauncherTheme.colors.primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (searchQuery.isNotEmpty()) {
                        Text(
                            "Results",
                            color = FoxLauncherTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                            LazyColumn {
                                items(searchResults) { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                            .bounceClick {
                                                onAppClick(app)
                                                onClose()
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppIcon(
                                            app = app,
                                            onClick = {
                                                onAppClick(app)
                                                onClose()
                                            },
                                            showLabel = false
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = app.label,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                if (searchResults.isEmpty()) {
                                    item {
                                        Text(
                                            "No results found for '$searchQuery'",
                                            color = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
