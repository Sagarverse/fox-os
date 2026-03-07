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
import com.example.foxos.ui.components.GlassPanel
import com.example.foxos.ui.components.HarmonyBackground
import com.example.foxos.ui.components.HarmonyAppIcon
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.LauncherViewModel
import com.example.foxos.service.FoxNotificationService
import androidx.compose.animation.core.animateFloat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants

@Composable
fun AppDrawer(
    viewModel: LauncherViewModel,
    onCloseDrawer: () -> Unit,
    gridColumns: Int = 4,
    showLabels: Boolean = true,
    hapticEnabled: Boolean = true,
    iconShape: String = "rounded_square",
    onHideApp: ((String) -> Unit)? = null,
    onPinApp: ((String) -> Unit)? = null,
    wallpaperId: String = "pastel"
) {
    val apps by viewModel.filteredApps.collectAsState()
    val suggestedApps by viewModel.suggestedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pinnedHomeApps by viewModel.pinnedHomeApps.collectAsState()
    val notificationBadges by FoxNotificationService.notificationBadges.collectAsState()
    val colors = FoxLauncherTheme.colors
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Immersive scroll soundbed
    LaunchedEffect(listState.firstVisibleItemIndex, hapticEnabled) {
        if (listState.firstVisibleItemIndex > 0 && hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            // Use a subtle sound for scroll if desired, we'll use NAVIGATION_DOWN or simply CLICK
            view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
        }
    }

    // Professional state reset: Clear search whenever we enter the drawer
    LaunchedEffect(Unit) {
        viewModel.clearSearchQuery()
    }

    val groupedApps = remember(apps) {
        apps.groupBy { it.label.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    // Microsoft-style nested scroll: swipe DOWN to close when at top of list
    // This allows normal scrolling but closes drawer on over-scroll at top
    var accumulatedOverscroll by remember { mutableStateOf(0f) }
    var isClosing by remember { mutableStateOf(false) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isClosing) return available // Prevent further processing while closing
                
                // When at top of list and trying to scroll further down (swipe down gesture)
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 5
                
                if (isAtTop && available.y > 0) {
                    // User is swiping DOWN while at top - accumulate overscroll
                    accumulatedOverscroll += available.y
                    if (accumulatedOverscroll > 80f) {
                        // Threshold reached - close drawer
                        isClosing = true
                        onCloseDrawer()
                        return available
                    }
                    // Consume the scroll to prevent bouncing but allow accumulation
                    return available
                }
                
                // Reset overscroll accumulator when scrolling up or not at top
                if (available.y < 0 || !isAtTop) {
                    accumulatedOverscroll = 0f
                }
                
                return Offset.Zero
            }
            
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isClosing) return available
                
                // Also check for fling gestures when at top
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 5
                
                if (isAtTop && available.y > 500f) {
                    // Fast downward fling at top - close immediately
                    isClosing = true
                    onCloseDrawer()
                    return available
                }
                
                return Velocity.Zero
            }
            
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Reset on fling end
                accumulatedOverscroll = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HarmonyBackground(wallpaperId = wallpaperId)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Section with Glass Header
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.2f),
                blurRadius = 40.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Search Intelligence...", 
                                color = Color.White.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearchQuery() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    IconButton(onClick = onCloseDrawer) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
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
            // Suggested Apps Section
            if (searchQuery.isEmpty() && suggestedApps.isNotEmpty()) {
                item(key = "header_suggestions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                        )
                        HorizontalDivider(color = colors.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
                    }
                }
                
                item(key = "suggestions_row") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        suggestedApps.take(gridColumns).forEach { app ->
                            Box(modifier = Modifier.weight(1f)) {
                                val iconBitmap = androidx.compose.runtime.remember(app.packageName) {
                                    app.icon?.let { it.toBitmap().asImageBitmap() }
                                }
                                HarmonyAppIcon(
                                    icon = iconBitmap,
                                    label = app.label,
                                    onClick = { viewModel.launchApp(app.packageName) },
                                    showLabel = showLabels,
                                    iconShape = iconShape,
                                    packageName = app.packageName,
                                    onHideApp = onHideApp,
                                    onPinApp = onPinApp,
                                    badgeCount = notificationBadges[app.packageName] ?: 0,
                                    isPinned = pinnedHomeApps.contains(app.packageName)
                                )
                            }
                        }
                        repeat(gridColumns - minOf(gridColumns, suggestedApps.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

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

                val chunks = appsInGroup.chunked(gridColumns)
                items(chunks, key = { "${initial}_${it.first().packageName}" }) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        chunk.forEach { app ->
                            Box(modifier = Modifier.weight(1f)) {
                                val iconBitmap = androidx.compose.runtime.remember(app.packageName) {
                                    app.icon?.let { it.toBitmap().asImageBitmap() }
                                }
                                HarmonyAppIcon(
                                    icon = iconBitmap,
                                    label = app.label,
                                    onClick = { 
                                        viewModel.launchApp(app.packageName)
                                        // Auto-clear for next entry
                                        viewModel.clearSearchQuery()
                                    },
                                    showLabel = showLabels,
                                    iconShape = iconShape,
                                    packageName = app.packageName,
                                    onHideApp = onHideApp,
                                    onPinApp = onPinApp,
                                    badgeCount = notificationBadges[app.packageName] ?: 0,
                                    isPinned = pinnedHomeApps.contains(app.packageName)
                                )
                            }
                        }
                        repeat(gridColumns - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        }
    }
}
