package com.example.foxos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foxos.ui.components.AppIcon
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.LauncherViewModel

@Composable
fun AppDrawer(
    viewModel: LauncherViewModel,
    onCloseDrawer: () -> Unit
) {
    val apps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val colors = FoxLauncherTheme.colors
    val listState = rememberLazyListState()

    // Professional state reset: Clear search whenever we enter the drawer
    LaunchedEffect(Unit) {
        viewModel.clearSearchQuery()
    }

    val groupedApps = remember(apps) {
        apps.groupBy { it.label.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    // High-performance nested scroll logic for swipe-down-to-close
    // This ensures scrolling is unaffected unless you're at the very top
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If we are scrolling down (available.y > 0) and at the top of the list
                if (available.y > 50f && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    onCloseDrawer()
                    return available
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .nestedScroll(nestedScrollConnection)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search Intelligence...", color = colors.onSurface.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.onSurface.copy(alpha = 0.2f),
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                )
            )
            IconButton(onClick = onCloseDrawer) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Grid optimized for 120Hz scrolling
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedApps.forEach { (initial, appsInGroup) ->
                item(key = "header_$initial") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = initial.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            ),
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                        )
                        HorizontalDivider(color = colors.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
                    }
                }

                val chunks = appsInGroup.chunked(4)
                items(chunks, key = { "${initial}_${it.first().packageName}" }) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        chunk.forEach { app ->
                            Box(modifier = Modifier.weight(1f)) {
                                AppIcon(
                                    app = app,
                                    onClick = { 
                                        viewModel.launchApp(app.packageName)
                                        // Auto-clear for next entry
                                        viewModel.clearSearchQuery()
                                    }
                                )
                            }
                        }
                        repeat(4 - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
