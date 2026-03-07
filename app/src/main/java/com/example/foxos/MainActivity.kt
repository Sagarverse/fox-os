package com.example.foxos

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.example.foxos.model.GestureAction
import com.example.foxos.search.SearchResultType
import com.example.foxos.ui.components.*
import com.example.foxos.ui.screens.*
import com.example.foxos.ui.theme.FoxThemeWrapper
import com.example.foxos.ui.theme.FoxOSTheme
import com.example.foxos.viewmodel.*
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.foxos.worker.UsageWorker
import java.util.concurrent.TimeUnit

enum class Screen {
    Home, Drawer, Settings, Tasks, Focus, LockSettings, Lock, QuickNotes, Gesture, Black, QuickDock, StudentHub, Notifications
}

class MainActivity : FragmentActivity() {
    private val launcherViewModel: LauncherViewModel by viewModels()
    private val pomodoroViewModel: PomodoroViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val noteViewModel: NoteViewModel by viewModels()
    private val usageViewModel: UsageViewModel by viewModels()
    private val gestureViewModel: GestureViewModel by viewModels()
    private val quickShortcutViewModel: QuickShortcutViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val controlViewModel: ControlCenterViewModel by viewModels()
    private val voiceViewModel: VoiceAssistantViewModel by viewModels()
    private val contextViewModel: ContextViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mediaViewModel: MediaViewModel by viewModels()
    private val studentHubViewModel: StudentHubViewModel by viewModels()


    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleUsageWorker()
        requestRequiredPermissions()
        setContent {
            val colors by themeViewModel.themeColors.collectAsState()
            val allApps by launcherViewModel.allApps.collectAsState()

            LaunchedEffect(Unit) {
                launcherViewModel.launchEvents.collect { event ->
                    when (event) {
                        is LaunchEvent.DirectLaunch -> {}
                        is LaunchEvent.RequireBiometric -> {
                            showBiometricPrompt(
                                onSuccess = {
                                    launcherViewModel.launchAppDirectly(event.packageName)
                                },
                                onError = {
                                    Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
            
            LaunchedEffect(Unit) {
                weatherViewModel.fetchWeather(this@MainActivity)
            }
            
            FoxOSTheme(colors = colors) {
                FoxThemeWrapper(colors = colors) {
                    var currentScreen by remember { mutableStateOf(Screen.Home) }
                    var isSidebarOpen by remember { mutableStateOf(false) }
                    var showFloatingNotes by remember { mutableStateOf(false) }
                    
                    val floatingNotesText by noteViewModel.floatingNoteContent.collectAsState()
                    
                    var showUniversalSearch by remember { mutableStateOf(false) }
                    var showCustomizationSheet by remember { mutableStateOf(false) }
                    var currentHomePage by remember { mutableIntStateOf(0) }
                    
                    val searchQuery by launcherViewModel.searchQuery.collectAsState()
                    val searchResults by launcherViewModel.searchResults.collectAsState()
                    val isDesktopModeEnabled by controlViewModel.isDesktopModeEnabled.collectAsState()
                    
                    val doubleTapToLock by settingsViewModel.doubleTapToLock.collectAsState()
                    val gridColumns by settingsViewModel.gridColumns.collectAsState()
                    val showLabels by settingsViewModel.showLabels.collectAsState()
                    val hapticFeedback by settingsViewModel.hapticFeedback.collectAsState()
                    val showClockOnHome by settingsViewModel.showClockOnHome.collectAsState()
                    
                    val allTasks by taskViewModel.allTasks.collectAsState()
                    val pendingTaskCount = allTasks.count { !it.isCompleted }
                    
                    val mediaInfo by mediaViewModel.mediaInfo.collectAsState()
                    
                    val wallpaperId by settingsViewModel.wallpaperId.collectAsState()
                    val wallpaperStyle by settingsViewModel.wallpaperStyle.collectAsState()
                    val homePageCount by settingsViewModel.homePageCount.collectAsState()
                    val iconShape by settingsViewModel.iconShape.collectAsState()
                    val currentTheme by themeViewModel.theme.collectAsState()
                    val sidebarApps by settingsViewModel.sidebarApps.collectAsState()
                    val dockApps by settingsViewModel.dockApps.collectAsState()
                    val homeScreenApps by settingsViewModel.homeScreenApps.collectAsState()
                    val isMinimalisticMode by settingsViewModel.isMinimalisticMode.collectAsState()

                    BackHandler(enabled = currentScreen != Screen.Home || isSidebarOpen || showFloatingNotes || showUniversalSearch || showCustomizationSheet) {
                        when {
                            showCustomizationSheet -> showCustomizationSheet = false
                            showUniversalSearch -> {
                                showUniversalSearch = false
                                launcherViewModel.clearSearchQuery()
                            }
                            showFloatingNotes -> showFloatingNotes = false
                            isSidebarOpen -> isSidebarOpen = false
                            else -> currentScreen = Screen.Home
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        HarmonyBackground(wallpaperId = wallpaperId)

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    val springSpec = spring<Float>(stiffness = 300f, dampingRatio = 0.6f)
                                    scaleIn(initialScale = 0.5f, animationSpec = springSpec) + fadeIn(animationSpec = tween(200)) togetherWith scaleOut(targetScale = 0.5f, animationSpec = springSpec) + fadeOut(animationSpec = tween(200))
                                },
                                label = "FuturisticScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    Screen.Home -> {
                                        if (isDesktopModeEnabled) {
                                            DesktopHomeScreen(
                                                launcherViewModel = launcherViewModel,
                                                controlViewModel = controlViewModel,
                                                onOpenSuperHub = { /* Retired in favor of Neural Search */ },
                                                onOpenDrawer = { currentScreen = Screen.Drawer }
                                            )
                                        } else {
                                            HomeScreen(
                                                viewModel = launcherViewModel,
                                                pomodoroViewModel = pomodoroViewModel,
                                                weatherViewModel = weatherViewModel,
                                                voiceViewModel = voiceViewModel,
                                                controlViewModel = controlViewModel,
                                                onOpenDrawer = { currentScreen = Screen.Drawer },
                                                onOpenSettings = { currentScreen = Screen.Settings },
                                                onOpenTasks = { currentScreen = Screen.Tasks },
                                                onOpenFocus = { currentScreen = Screen.Focus },
                                                onOpenLock = { currentScreen = Screen.Lock },
                                                onOpenReference = { openReferenceMaterial() },
                                                onOpenQuickNotes = { currentScreen = Screen.QuickNotes },
                                                onOpenAssistant = { voiceViewModel.startListening() },
                                                onOpenNotifications = { currentScreen = Screen.Notifications },
                                                onOpenStudentHub = { currentScreen = Screen.StudentHub },
                                                onOpenCustomization = { showCustomizationSheet = true },
                                                pendingTaskCount = pendingTaskCount,
                                                mediaTitle = mediaInfo.title,
                                                mediaArtist = mediaInfo.artist,
                                                pageCount = homePageCount,
                                                currentPage = currentHomePage,
                                                onPageChanged = { currentHomePage = it },
                                                showClockOnHome = showClockOnHome,
                                                wallpaperId = wallpaperId,
                                                onSwipeUp = { fingerCount ->
                                                    currentScreen = if (fingerCount >= 2) Screen.QuickDock else Screen.Drawer
                                                },
                                                onSwipeDownFromTop = { 
                                                    showUniversalSearch = true
                                                },
                                                onSwipeDownFromCenter = { showUniversalSearch = true },
                                                onSwipeLeft = { 
                                                    if (homePageCount == 1) { 
                                                        isSidebarOpen = true 
                                                    } else {
                                                        if (currentHomePage < homePageCount - 1) currentHomePage++
                                                    }
                                                },
                                                onSwipeRight = { 
                                                    if (homePageCount == 1) {
                                                        currentScreen = Screen.Tasks
                                                    } else {
                                                        if (currentHomePage > 0) currentHomePage--
                                                    }
                                                },
                                                onTripleTap = { if (doubleTapToLock) lockDevice() },
                                                onLongPress = { showCustomizationSheet = true },
                                                onOpenSidebar = { isSidebarOpen = true },
                                                onCloseSidebar = { isSidebarOpen = false },
                                                onRemoveApp = { launcherViewModel.removeFromHome(it) },
                                                pinnedHomeApps = homeScreenApps,
                                                isMinimalistic = isMinimalisticMode
                                            )
                                        }
                                    }
                                    Screen.Drawer -> AppDrawer(
                                        viewModel = launcherViewModel,
                                        onCloseDrawer = { currentScreen = Screen.Home },
                                        gridColumns = gridColumns,
                                        showLabels = showLabels,
                                        hapticEnabled = hapticFeedback,
                                        iconShape = iconShape,
                                        onHideApp = { packageName -> launcherViewModel.hideApp(packageName) },
                                        onPinApp = { packageName -> launcherViewModel.pinToHome(packageName) },
                                        wallpaperId = wallpaperId
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
                                    Screen.LockSettings -> LockScreenSettings(
                                        shortcutViewModel = quickShortcutViewModel,
                                        launcherViewModel = launcherViewModel,
                                        onBack = { currentScreen = Screen.Settings }
                                    )
                                    Screen.Focus -> FocusDashboard(
                                        viewModel = usageViewModel,
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.Lock -> LockScreen(
                                        launcherViewModel = launcherViewModel,
                                        shortcutViewModel = quickShortcutViewModel,
                                        contextViewModel = contextViewModel,
                                        onUnlock = { currentScreen = Screen.Home }
                                    )
                                    Screen.QuickNotes -> QuickNotesScreen(
                                        viewModel = noteViewModel,
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.Black -> Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { currentScreen = Screen.Home }) }
                                    )
                                    Screen.QuickDock -> QuickDockDrawer(
                                        apps = if (dockApps.isNotEmpty()) {
                                            allApps.filter { dockApps.contains(it.packageName) }
                                        } else {
                                            allApps.take(8)
                                        },
                                        onAppClick = { app ->
                                            launcherViewModel.launchApp(app.packageName)
                                            currentScreen = Screen.Home
                                        },
                                        onClose = { currentScreen = Screen.Home }
                                    )
                                    Screen.Notifications -> NotificationsScreen(
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    Screen.StudentHub -> StudentHubScreen(
                                        viewModel = studentHubViewModel,
                                        onBack = { currentScreen = Screen.Home }
                                    )
                                    else -> {}

                                }
                            }
                        }


                        AnimatedVisibility(
                            visible = isSidebarOpen,
                            enter = slideInHorizontally { it } + fadeIn(),
                            exit = slideOutHorizontally { it } + fadeOut()
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { isSidebarOpen = false } })
                                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                                    val sidebarAppsList = if (sidebarApps.isNotEmpty()) {
                                        allApps.filter { sidebarApps.contains(it.packageName) }
                                    } else {
                                        allApps.take(8)
                                    }
                                    EdgeSidebar(
                                        apps = sidebarAppsList,
                                        onAppClick = { 
                                            launcherViewModel.launchApp(it.packageName)
                                            isSidebarOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        if (showFloatingNotes) {
                            FloatingWindow(
                                title = "Quick Focus Notes",
                                onClose = { showFloatingNotes = false }
                            ) {
                                TextField(
                                    value = floatingNotesText,
                                    onValueChange = { noteViewModel.updateFloatingNoteContent(it) },
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

                        NeuralPulseSearch(
                            isVisible = showUniversalSearch,
                            searchQuery = searchQuery,
                            onQueryChange = { launcherViewModel.setSearchQuery(it) },
                            searchResults = searchResults,
                            onResultClick = { result ->
                                when (result.type) {
                                    SearchResultType.APP -> result.packageName?.let { launcherViewModel.launchApp(it) }
                                    SearchResultType.SETTING, 
                                    SearchResultType.CONTACT, 
                                    SearchResultType.WHATSAPP, 
                                    SearchResultType.INSTAGRAM, 
                                    SearchResultType.WEB -> result.intent?.let { startActivity(it) }
                                }
                                showUniversalSearch = false
                                launcherViewModel.clearSearchQuery()
                            },
                            onClose = {
                                showUniversalSearch = false
                                launcherViewModel.clearSearchQuery()
                            },
                            controlViewModel = controlViewModel
                        )

                        CustomizationSheet(
                            isVisible = showCustomizationSheet,
                            currentWallpaper = wallpaperId,
                            currentWallpaperStyle = wallpaperStyle,
                            currentTheme = currentTheme,
                            gridColumns = gridColumns,
                            totalPages = homePageCount,
                            currentPage = currentHomePage,
                            currentIconShape = iconShape,
                            allApps = allApps,
                            sidebarApps = sidebarApps,
                            homeScreenApps = homeScreenApps,
                            onDismiss = { showCustomizationSheet = false },
                            onWallpaperSelected = { settingsViewModel.updateWallpaperId(it) },
                            onWallpaperStyleSelected = { settingsViewModel.updateWallpaperStyle(it) },
                            onThemeSelected = { themeViewModel.setTheme(it) },
                            onGridColumnsChanged = { settingsViewModel.updateGridColumns(it) },
                            onAddPage = { settingsViewModel.updateHomePageCount(homePageCount + 1) },
                            onRemovePage = { settingsViewModel.updateHomePageCount(homePageCount - 1) },
                            onCustomWallpaperPicked = { uri ->
                                settingsViewModel.updateCustomWallpaperUri(uri.toString())
                            },
                            onIconShapeChanged = { settingsViewModel.updateIconShape(it) },
                            onSidebarAppsChanged = { settingsViewModel.updateSidebarApps(it) },
                            onHomeScreenAppsChanged = { settingsViewModel.updateHomeScreenApps(it) },
                            dockApps = dockApps,
                            onDockAppsChanged = { settingsViewModel.updateDockApps(it) }
                        )
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
        } catch (_: Exception) {
            Toast.makeText(this, "No PDF Reader found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Vault")
            .setSubtitle("Authenticate to unlock")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun scheduleUsageWorker() {
        val usageRequest = PeriodicWorkRequestBuilder<UsageWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "fox_usage_learning",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            usageRequest
        )
    }

    private fun lockDevice() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable device admin to use double-tap to lock")
            }
            startActivity(intent)
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(
                    applicationContext, 
                    "Some features may be limited without permissions", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
