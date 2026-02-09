package com.example.foxos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.foxos.model.GestureAction
import com.example.foxos.ui.components.*
import com.example.foxos.ui.screens.*
import com.example.foxos.ui.theme.FoxThemeWrapper
import com.example.foxos.ui.theme.FoxOSTheme
import com.example.foxos.viewmodel.*

enum class Screen {
    Home, Drawer, Settings, Tasks, Focus, LockSettings, Lock, QuickNotes, Gesture, Black, SuperHub
}

class MainActivity : ComponentActivity() {
    private val launcherViewModel: LauncherViewModel by viewModels()
    private val pomodoroViewModel: PomodoroViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val usageViewModel: UsageViewModel by viewModels()
    private val gestureViewModel: GestureViewModel by viewModels()
    private val quickShortcutViewModel: QuickShortcutViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val controlViewModel: ControlCenterViewModel by viewModels()
    private val voiceViewModel: VoiceAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colors by themeViewModel.themeColors.collectAsState()
            val allApps by launcherViewModel.allApps.collectAsState()
            
            FoxOSTheme(colors = colors) {
                FoxThemeWrapper(colors = colors) {
                    var currentScreen by remember { mutableStateOf(Screen.Home) }
                    var isSidebarOpen by remember { mutableStateOf(false) }
                    var showFloatingNotes by remember { mutableStateOf(false) }
                    var floatingNotesText by remember { mutableStateOf("") }

                    // Horizontal draggable state for switching to Gesture page and Sidebar
                    val horizontalDraggableState = rememberDraggableState { delta ->
                        if (delta > 40 && currentScreen == Screen.Home) {
                            currentScreen = Screen.Gesture
                        } else if (delta < -40 && currentScreen == Screen.Gesture) {
                            currentScreen = Screen.Home
                        } else if (delta < -40 && currentScreen == Screen.Home && !isSidebarOpen) {
                            isSidebarOpen = true
                        } else if (delta > 40 && isSidebarOpen) {
                            isSidebarOpen = false
                        }
                    }

                    // Vertical draggable state for App Drawer and SuperHub
                    val verticalDraggableState = rememberDraggableState { delta ->
                        if (delta < -40 && currentScreen == Screen.Home) {
                            currentScreen = Screen.Drawer
                        } else if (delta > 40 && currentScreen == Screen.Home) {
                            currentScreen = Screen.SuperHub
                        }
                    }

                    BackHandler(enabled = currentScreen != Screen.Home || isSidebarOpen || showFloatingNotes) {
                        if (showFloatingNotes) showFloatingNotes = false
                        else if (isSidebarOpen) isSidebarOpen = false
                        else currentScreen = Screen.Home
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        NebulaBackground()

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .draggable(horizontalDraggableState, Orientation.Horizontal)
                                .draggable(verticalDraggableState, Orientation.Vertical)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { currentScreen = Screen.Black },
                                        onLongPress = { 
                                            if (currentScreen == Screen.Home) {
                                                showFloatingNotes = true
                                            }
                                        }
                                    )
                                },
                            color = Color.Transparent
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    when (targetState) {
                                        Screen.SuperHub -> slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                        Screen.Drawer -> slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                        Screen.Gesture -> slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                        else -> fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                                    }
                                },
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    Screen.Home -> HomeScreen(
                                        viewModel = launcherViewModel,
                                        pomodoroViewModel = pomodoroViewModel,
                                        weatherViewModel = weatherViewModel,
                                        voiceViewModel = voiceViewModel,
                                        onOpenDrawer = { currentScreen = Screen.Drawer },
                                        onOpenSettings = { currentScreen = Screen.Settings },
                                        onOpenTasks = { currentScreen = Screen.Tasks },
                                        onOpenFocus = { currentScreen = Screen.Focus },
                                        onOpenLock = { currentScreen = Screen.Lock },
                                        onOpenReference = { openReferenceMaterial() }
                                    )
                                    Screen.SuperHub -> SuperHubScreen(
                                        controlViewModel = controlViewModel,
                                        pomodoroViewModel = pomodoroViewModel,
                                        onClose = { currentScreen = Screen.Home }
                                    )
                                    Screen.Gesture -> GesturePage(
                                        onGestureComplete = { points ->
                                            val action = gestureViewModel.recognizeGesture(points)
                                            if (action != null) {
                                                handleGestureAction(action)
                                                currentScreen = Screen.Home
                                            }
                                        }
                                    )
                                    Screen.Drawer -> AppDrawer(
                                        viewModel = launcherViewModel,
                                        onCloseDrawer = { currentScreen = Screen.Home }
                                    )
                                    Screen.Settings -> SettingsScreen(
                                        gestureViewModel = gestureViewModel,
                                        quickShortcutViewModel = quickShortcutViewModel,
                                        launcherViewModel = launcherViewModel,
                                        themeViewModel = themeViewModel,
                                        onOpenLockSettings = { currentScreen = Screen.LockSettings },
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.Tasks -> TasksScreen(
                                        viewModel = taskViewModel,
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.Focus -> FocusDashboard(
                                        viewModel = usageViewModel,
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.Lock -> LockScreen(
                                        launcherViewModel = launcherViewModel,
                                        shortcutViewModel = quickShortcutViewModel,
                                        onUnlock = { currentScreen = Screen.Home }
                                    )
                                    Screen.QuickNotes -> QuickNotesScreen(onBack = { currentScreen = Screen.Home })
                                    Screen.Black -> Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { currentScreen = Screen.Home }) }
                                    )
                                    else -> {}
                                }
                            }
                        }

                        // Premium Edge Sidebar Overlay
                        AnimatedVisibility(
                            visible = isSidebarOpen,
                            enter = slideInHorizontally { it } + fadeIn(),
                            exit = slideOutHorizontally { it } + fadeOut()
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { isSidebarOpen = false } })
                                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                                    EdgeSidebar(
                                        apps = allApps.take(8),
                                        onAppClick = { 
                                            launcherViewModel.launchApp(it.packageName)
                                            isSidebarOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Floating Notes Window
                        if (showFloatingNotes) {
                            FloatingWindow(
                                title = "Quick Focus Notes",
                                onClose = { showFloatingNotes = false }
                            ) {
                                TextField(
                                    value = floatingNotesText,
                                    onValueChange = { floatingNotesText = it },
                                    modifier = Modifier.fillMaxSize(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = colors.primary,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = colors.onSurface,
                                        unfocusedTextColor = colors.onSurface
                                    ),
                                    placeholder = { Text("Type something...", color = colors.onSurface.copy(alpha = 0.5f)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openReferenceMaterial() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF Reader found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGestureAction(action: String) {
        when (action) {
            GestureAction.START_POMODORO.name -> {
                pomodoroViewModel.startTimer()
                Toast.makeText(this, "Pomodoro Started!", Toast.LENGTH_SHORT).show()
            }
            GestureAction.TOGGLE_STUDY_MODE.name -> {
                launcherViewModel.toggleStudyMode()
                Toast.makeText(this, "Study Mode Toggled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
