package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.positionChange
import com.example.foxos.ui.components.*
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    pomodoroViewModel: PomodoroViewModel,
    weatherViewModel: WeatherViewModel,
    voiceViewModel: VoiceAssistantViewModel,
    controlViewModel: ControlCenterViewModel? = null,
    onOpenDrawer: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLock: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenQuickNotes: () -> Unit,
    onOpenCustomization: () -> Unit = {},
    onOpenStudentHub: () -> Unit = {},
    pendingTaskCount: Int = 0,
    mediaTitle: String = "Not Playing",
    mediaArtist: String = "",
    pageCount: Int = 1,
    currentPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    showClockOnHome: Boolean = true,
    wallpaperId: String = "pastel",
    // Gesture callbacks
    onSwipeUp: (fingerCount: Int) -> Unit = {},
    onSwipeDownFromTop: (isLeftSide: Boolean) -> Unit = {},
    onSwipeDownFromCenter: () -> Unit = {},
    onTripleTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onOpenSidebar: () -> Unit = {},
    onCloseSidebar: () -> Unit = {},
    pinnedHomeApps: Set<String> = emptySet()
) {
    val colors = FoxLauncherTheme.colors
    var activeServiceCard by remember { mutableStateOf<String?>(null) }
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    val isStudyMode by viewModel.isStudyModeActive.collectAsState()
    var showSidebar by remember { mutableStateOf(false) }

    val suggestedApps by viewModel.suggestedApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val weather by weatherViewModel.weatherInfo.collectAsState()
    val assistantState by voiceViewModel.state.collectAsState()
    val recognizedText by voiceViewModel.recognizedText.collectAsState()
    val predictedApps by viewModel.predictedApps.collectAsState()
    
    // Use pinned apps if available, otherwise fall back to suggested
    val displayApps = remember(pinnedHomeApps, allApps, suggestedApps) {
        if (pinnedHomeApps.isNotEmpty()) {
            allApps.filter { pinnedHomeApps.contains(it.packageName) }
        } else {
            suggestedApps
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    var scale by remember { mutableStateOf(1f) }
    val isEditing = scale < 0.95f

    // Pager state for multiple home pages
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pageCount }
    )

    // Notify parent when page changes
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    var dragStartX by remember { mutableStateOf(0f) }
    var dragStartY by remember { mutableStateOf(0f) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var tapCount by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.7f, 1f)
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    dragStartX = down.position.x
                    dragStartY = down.position.y
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var pointerCount = 1
                    var gestureConsumed = false
                    var isLongPress = false
                    val startTime = System.currentTimeMillis()
                    
                    // Check if touch is in left edge for sidebar
                    val isLeftEdge = dragStartX < size.width * 0.10f
                    val isRightEdge = dragStartX > size.width * 0.90f
                    
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size > pointerCount) {
                            pointerCount = event.changes.size
                        }
                        
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                totalDragX += change.positionChange().x
                                totalDragY += change.positionChange().y
                                
                                val isVerticalSwipe = kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX) * 1.5f
                                val isHorizontalSwipe = kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) * 1.5f
                                val totalMovement = kotlin.math.abs(totalDragX) + kotlin.math.abs(totalDragY)
                                
                                // Consume vertical swipes
                                if (totalMovement > 50f && isVerticalSwipe) {
                                    gestureConsumed = true
                                    change.consume()
                                }
                                // Consume horizontal swipes from edges for sidebar
                                if (totalMovement > 50f && isHorizontalSwipe && (isLeftEdge || isRightEdge)) {
                                    gestureConsumed = true
                                    change.consume()
                                }
                                
                                // Detect long press (held for 500ms without much movement)
                                if (!isLongPress && System.currentTimeMillis() - startTime > 500 && totalMovement < 20f) {
                                    isLongPress = true
                                    onLongPress()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    
                    val gestureDuration = System.currentTimeMillis() - startTime
                    val totalMovement = kotlin.math.abs(totalDragX) + kotlin.math.abs(totalDragY)
                    
                    // Handle tap / triple-tap (short duration, minimal movement)
                    if (gestureDuration < 300 && totalMovement < 20f && !isLongPress) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 400) {
                            tapCount++
                            if (tapCount >= 3) {
                                // Triple tap - lock screen
                                onTripleTap()
                                tapCount = 0
                                lastTapTime = 0L
                            } else {
                                lastTapTime = currentTime
                            }
                        } else {
                            tapCount = 1
                            lastTapTime = currentTime
                        }
                        return@awaitEachGesture
                    }
                    
                    if (!gestureConsumed || isLongPress) return@awaitEachGesture
                    
                    val isHorizontal = kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY)
                    
                    if (isHorizontal && kotlin.math.abs(totalDragX) > 100f) {
                        // Horizontal swipe for sidebar
                        if (totalDragX > 0 && isLeftEdge) {
                            // Swipe right from left edge - open sidebar
                            onOpenSidebar()
                        } else if (totalDragX < 0 && isRightEdge) {
                            // Swipe left from right edge - close sidebar
                            onCloseSidebar()
                        }
                    } else if (!isHorizontal && kotlin.math.abs(totalDragY) > 80f) {
                        if (totalDragY < 0) {
                            // Swipe up - open drawer
                            onSwipeUp(pointerCount)
                        } else {
                            // Swipe down
                            if (dragStartY < size.height * 0.15f) {
                                onSwipeDownFromTop(dragStartX < size.width / 2f)
                            } else {
                                onSwipeDownFromCenter()
                            }
                        }
                    }
                }
            }
    ) {
        HarmonyBackground(wallpaperId = wallpaperId)

        // Multi-page home screen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            HomePageContent(
                page = page,
                totalPages = pageCount,
                viewModel = viewModel,
                colors = colors,
                scale = scale,
                currentTime = currentTime,
                timeFormat = timeFormat,
                dateFormat = dateFormat,
                displayApps = displayApps,
                weather = weather,
                predictedApps = predictedApps,
                isStudyMode = isStudyMode,
                showClockOnHome = showClockOnHome,
                voiceViewModel = voiceViewModel,
                mediaTitle = mediaTitle,
                mediaArtist = mediaArtist,
                onOpenDrawer = onOpenDrawer,
                onOpenTasks = onOpenTasks,
                onOpenFocus = onOpenFocus,
                onOpenSettings = onOpenSettings,
                onShowSidebar = { showSidebar = true },
                onShowServiceCard = { activeServiceCard = it }
            )
        }

        // Page indicator
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colors.primary
                                else colors.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        if (isEditing) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(32.dp)) {
                GlassCard {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { scale = 1f }) {
                            Icon(Icons.Default.Check, "Done", tint = colors.onSurface)
                            Text("Done", color = colors.onSurface, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                            scale = 1f
                            onOpenCustomization()
                        }) {
                            Icon(Icons.Default.Wallpaper, "Wallpaper", tint = colors.onSurface)
                            Text("Wallpaper", color = colors.onSurface, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                            scale = 1f
                            onOpenCustomization()
                        }) {
                            Icon(Icons.Default.Widgets, "Widgets", tint = colors.onSurface)
                            Text("Widgets", color = colors.onSurface, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                            scale = 1f
                            onOpenCustomization()
                        }) {
                            Icon(Icons.Default.GridOn, "Grid", tint = colors.onSurface)
                            Text("Grid", color = colors.onSurface, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Assistant Overlay
        if (assistantState !is AssistantState.Idle) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))) {
                AlienAICoreHUD(
                    state = assistantState,
                    recognizedText = recognizedText,
                    onDismiss = { voiceViewModel.resetState() },
                    onCancelTimer = { voiceViewModel.cancelTimer() }
                )
            }
        }

        // Service Card Overlay
        if (activeServiceCard != null) {
            Dialog(
                onDismissRequest = { activeServiceCard = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { activeServiceCard = null }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GlassPanel(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(250.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            ),
                        shape = com.example.foxos.ui.theme.HarmonyShapes.large,
                        color = if (colors.isLight) Color.White.copy(alpha = 0.8f) else colors.surface.copy(alpha = 0.9f),
                        blurRadius = 40.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                            Text(
                                text = "$activeServiceCard Service",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    colors.onSurface.copy(alpha = 0.05f),
                                    com.example.foxos.ui.theme.HarmonyShapes.medium
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Interactive $activeServiceCard Widget Content", color = colors.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // Sidebar
        Sidebar(
            isVisible = showSidebar,
            onDismiss = { showSidebar = false },
            onOpenDrawer = onOpenDrawer,
            onOpenTasks = onOpenTasks,
            onOpenFocus = onOpenFocus,
            onOpenSettings = onOpenSettings,
            onOpenAssistant = { voiceViewModel.startListening() },
            onOpenQuickNotes = onOpenQuickNotes,
            onOpenStudentHub = onOpenStudentHub,
            pendingTaskCount = pendingTaskCount,
            controlViewModel = controlViewModel
        )
    }
}

@Composable
private fun HomePageContent(
    page: Int,
    totalPages: Int,
    viewModel: LauncherViewModel,
    colors: com.example.foxos.ui.theme.FoxThemeColors,
    scale: Float,
    currentTime: Date,
    timeFormat: SimpleDateFormat,
    dateFormat: SimpleDateFormat,
    displayApps: List<com.example.foxos.model.AppInfo>,
    weather: com.example.foxos.viewmodel.WeatherInfo,
    predictedApps: Set<String>,
    isStudyMode: Boolean,
    showClockOnHome: Boolean,
    voiceViewModel: VoiceAssistantViewModel,
    mediaTitle: String = "Not Playing",
    mediaArtist: String = "",
    onOpenDrawer: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowSidebar: () -> Unit,
    onShowServiceCard: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = true
                shape = com.example.foxos.ui.theme.HarmonyShapes.large
            }
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // Menu button for sidebar - only on page 0
        if (page == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onShowSidebar,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = colors.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Only show clock, date, and weather widget on page 0
        if (page == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Clock - conditionally shown
                if (showClockOnHome) {
                    Column {
                        Text(
                            text = timeFormat.format(currentTime),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Light,
                                color = colors.onSurface.copy(alpha = 0.8f),
                                letterSpacing = (-1).sp
                            )
                        )
                        Text(
                            text = dateFormat.format(currentTime),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                StackableWidget(
                    modifier = Modifier.width(160.dp).height(100.dp),
                    widgets = listOf(
                        {
                            HarmonyWidget(modifier = Modifier.fillMaxSize(), title = "Weather") {
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 4.dp)) {
                                    Text(weather.temperature, color = colors.onSurface.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 32.sp)
                                    Text(weather.condition, color = colors.onSurface.copy(alpha = 0.6f), fontSize = 14.sp, maxLines = 1)
                                }
                            }
                        },
                        {
                            HarmonyWidget(modifier = Modifier.fillMaxSize(), title = "Music") {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    Icon(
                                        if (mediaTitle != "Not Playing") Icons.Default.MusicNote else Icons.Default.PlayArrow, 
                                        contentDescription = "Music", 
                                        tint = colors.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                Text(
                                    mediaTitle.take(18), 
                                    color = colors.onSurface.copy(alpha = if (mediaTitle != "Not Playing") 0.9f else 0.5f), 
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                if (mediaArtist.isNotEmpty()) {
                                    Text(
                                        mediaArtist.take(16), 
                                        color = colors.onSurface.copy(alpha = 0.6f), 
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                )
            )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Different content per page
        when (page) {
            0 -> {
                // Main page - Suggested apps and widgets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InteractiveLargeFolder(
                        modifier = Modifier.weight(1f),
                        title = "Apps",
                        apps = displayApps.take(9),
                        onAppClick = { app -> viewModel.launchApp(app.packageName) },
                        onExpandClick = { onOpenDrawer() }
                    )
                    SmartSuggestionsWidget(
                        modifier = Modifier.weight(1f),
                        onOpenFocus = onOpenFocus
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Popular Apps row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    displayApps.drop(4).take(4).forEach { app ->
                        val isPredicted = predictedApps.contains(app.packageName)
                        val iconBitmap = remember(app.packageName) {
                            app.icon?.let { it.toBitmap().asImageBitmap() }
                        }
                        HarmonyAppIcon(
                            icon = iconBitmap,
                            label = app.label,
                            onClick = { viewModel.launchApp(app.packageName) },
                            onSwipeUp = { onShowServiceCard(app.label) },
                            isPredicted = isPredicted
                        )
                    }
                    repeat((4 - displayApps.drop(4).take(4).size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
            1 -> {
                // Second page - More apps
                Text(
                    "Page ${page + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show next set of apps
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    displayApps.drop(8).take(4).forEach { app ->
                        val iconBitmap = remember(app.packageName) {
                            app.icon?.let { it.toBitmap().asImageBitmap() }
                        }
                        HarmonyAppIcon(
                            icon = iconBitmap,
                            label = app.label,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
            else -> {
                // Additional pages
                Text(
                    "Page ${page + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Customize this page",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // HarmonyOS-style Floating Dock
        HarmonyFloatingDock(modifier = Modifier.padding(bottom = 24.dp)) {
            HarmonyFloatingDockIcon(Icons.Default.Apps, "Apps", onOpenDrawer)
            HarmonyFloatingDockIcon(Icons.Default.Checklist, "Tasks", onOpenTasks)
            HarmonyFloatingDockIcon(
                icon = Icons.Default.Mic,
                label = "Assistant",
                onClick = { voiceViewModel.startListening() }
            )
            HarmonyFloatingDockIcon(Icons.Default.Settings, "Settings", onOpenSettings)
            HarmonyFloatingDockIcon(
                icon = if (isStudyMode) Icons.Default.Shield else Icons.Default.ShieldMoon,
                label = "Focus",
                onClick = onOpenFocus,
                isActive = isStudyMode
            )
        }
    }
}

@Composable
fun AlienDataDial(currentTime: Date) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EE, d MMM", Locale.getDefault())
    val infiniteTransition = rememberInfiniteTransition(label = "dial")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            rotate(rotation, center) {
                drawCircle(
                    color = Color(0xFF00FFE5),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 30f), 0f)
                    )
                )
            }

            rotate(-rotation * 1.5f, center) {
                drawCircle(
                    color = Color(0xFFFF00D4),
                    radius = radius * 0.8f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                    )
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeFormat.format(currentTime),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00FFE5),
                    letterSpacing = (-1).sp
                )
            )
            Text(
                text = dateFormat.format(currentTime).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
