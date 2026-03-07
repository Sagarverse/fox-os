package com.example.foxos.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.gestures.*
import com.example.foxos.ui.components.*
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import com.example.foxos.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
    onOpenAssistant: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenStudentHub: () -> Unit = {},
    onOpenCustomization: () -> Unit = {},
    pendingTaskCount: Int = 0,
    mediaTitle: String = "Not Playing",
    mediaArtist: String = "",
    pageCount: Int = 1,
    currentPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    showClockOnHome: Boolean = true,
    wallpaperId: String = "pastel",
    onSwipeUp: (fingerCount: Int) -> Unit = {},
    onSwipeDownFromTop: () -> Unit = {},
    onSwipeDownFromCenter: () -> Unit = {},
    onTripleTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onOpenSidebar: () -> Unit = {},
    onCloseSidebar: () -> Unit = {},
    onRemoveApp: (String) -> Unit = {},
    pinnedHomeApps: Set<String> = emptySet(),
    isMinimalistic: Boolean = false
) {
    // Lift all state declarations to the very top
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

    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    var lastTapTime by remember { mutableStateOf(0L) }
    var tapCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isEditing) {
                if (!isEditing) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 1f)
                    }
                }
            }
            .pointerInput(lastTapTime, tapCount) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startPos = down.position
                    val startTime = System.currentTimeMillis()
                    
                    var isLongPressTriggered = false
                    var hasSwiped = false
                    
                    do {
                        val event = awaitPointerEvent()
                        val fingerCount = event.changes.size
                        val currentTimeNow = System.currentTimeMillis()
                        val elapsed = currentTimeNow - startTime
                        
                        // Detect swipe
                        if (!hasSwiped && !isLongPressTriggered) {
                            val averageX = event.changes.map { it.position.x }.average().toFloat()
                            val averageY = event.changes.map { it.position.y }.average().toFloat()
                            val dragAmountX = averageX - startPos.x
                            val dragAmountY = averageY - startPos.y
                            
                            // Only trigger vertical swipes if drag is primarily vertical and exceeds threshold
                            if (abs(dragAmountY) > 80f && abs(dragAmountY) > abs(dragAmountX) * 2) { 
                                hasSwiped = true
                                if (dragAmountY < 0) {
                                    onSwipeUp(fingerCount)
                                } else {
                                    if (startPos.y < size.height / 4) {
                                        onSwipeDownFromTop()
                                    } else {
                                        onSwipeDownFromCenter()
                                    }
                                }
                            }
                        }

                        if (!isLongPressTriggered && !hasSwiped && elapsed > 500) {
                            val totalMove = event.changes.map { (it.position - startPos).getDistance() }.maxOrNull() ?: 0f
                            if (totalMove < 20f) {
                                isLongPressTriggered = true
                                onLongPress()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    
                    val totalDuration = System.currentTimeMillis() - startTime
                    if (totalDuration < 300 && !isLongPressTriggered) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 400) {
                            tapCount++
                            if (tapCount >= 3) {
                                onTripleTap()
                                tapCount = 0
                                lastTapTime = 0L
                            } else {
                                lastTapTime = now
                            }
                        } else {
                            tapCount = 1
                            lastTapTime = now
                        }
                    }
                }
            }
    ) {
        HarmonyBackground(wallpaperId = wallpaperId)

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
                    onShowServiceCard = { activeServiceCard = it },
                    onRemoveApp = onRemoveApp,
                    isMinimalistic = isMinimalistic
                )
            }

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

            if (assistantState !is AssistantState.Idle) {
                val rmsLevel by voiceViewModel.rmsLevel.collectAsState()
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))) {
                    AlienAICoreHUD(
                        state = assistantState,
                        recognizedText = recognizedText,
                        onDismiss = { voiceViewModel.resetState() },
                        onReset = { voiceViewModel.resetAndListen() },
                        onTextSubmit = { voiceViewModel.submitTextCommand(it) },
                        rmsLevel = rmsLevel,
                        isMinimalistic = isMinimalistic,
                        onCancelTimer = { voiceViewModel.cancelTimer() }
                    )
                }
            }

            if (activeServiceCard != null) {
                Dialog(
                    onDismissRequest = { activeServiceCard = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
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
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {}
                                ),
                            shape = HarmonyShapes.large,
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
                                        HarmonyShapes.medium
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

            Sidebar(
                isVisible = showSidebar,
                onDismiss = { showSidebar = false },
                onOpenDrawer = onOpenDrawer,
                onOpenTasks = onOpenTasks,
                onOpenFocus = onOpenFocus,
                onOpenSettings = onOpenSettings,
                onOpenAssistant = { voiceViewModel.startListening() },
                onOpenQuickNotes = onOpenQuickNotes,
                onOpenNotifications = onOpenNotifications,
                onOpenStudentHub = onOpenStudentHub,
                pendingTaskCount = pendingTaskCount,
                controlViewModel = controlViewModel
            )

            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    HarmonyFloatingDock {
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
        }
    }

@Composable
fun HomePageContent(
    page: Int,
    totalPages: Int,
    viewModel: LauncherViewModel,
    colors: com.example.foxos.ui.theme.FoxThemeColors,
    scale: Float,
    currentTime: Date,
    timeFormat: SimpleDateFormat,
    dateFormat: SimpleDateFormat,
    displayApps: List<com.example.foxos.model.AppInfo>,
    weather: WeatherInfo,
    predictedApps: Set<String>,
    isStudyMode: Boolean,
    showClockOnHome: Boolean,
    voiceViewModel: VoiceAssistantViewModel,
    mediaTitle: String,
    mediaArtist: String,
    onOpenDrawer: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowSidebar: () -> Unit,
    onShowServiceCard: (String) -> Unit,
    onRemoveApp: (String) -> Unit,
    isMinimalistic: Boolean
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    clip = true
                    shape = HarmonyShapes.large
                }
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

        if (page == 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
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

        Spacer(modifier = Modifier.height(24.dp))

        when (page) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    InteractiveLargeFolder(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = "Apps",
                        apps = displayApps.take(9),
                        onAppClick = { app -> viewModel.launchApp(app.packageName) },
                        onExpandClick = { onOpenDrawer() }
                    )
                    SmartSuggestionsWidget(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onOpenFocus = onOpenFocus
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
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
                            onRemoveApp = onRemoveApp,
                            packageName = app.packageName,
                            isPredicted = isPredicted,
                            isMinimalistic = isMinimalistic
                        )
                    }
                }
            }
            1 -> {
                Text("Page ${page + 1}", style = MaterialTheme.typography.titleMedium, color = colors.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                ) {
                    displayApps.drop(8).take(4).forEach { app ->
                        val iconBitmap = remember(app.packageName) {
                            app.icon?.let { it.toBitmap().asImageBitmap() }
                        }
                        HarmonyAppIcon(
                            icon = iconBitmap,
                            label = app.label,
                            onClick = { viewModel.launchApp(app.packageName) },
                            onRemoveApp = onRemoveApp,
                            packageName = app.packageName,
                            isMinimalistic = isMinimalistic
                        )
                    }
                }
            }
            else -> {
                Text("Page ${page + 1}", style = MaterialTheme.typography.titleMedium, color = colors.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Customize this page", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
