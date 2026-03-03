package com.example.foxos.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.graphics.Shape
import com.example.foxos.ui.theme.*

data class WallpaperOption(
    val id: String,
    val colors: List<Color>,
    val name: String
)

data class IconShapeOption(
    val id: String,
    val name: String,
    val shape: Shape
)

val iconShapeOptions = listOf(
    IconShapeOption("circle", "Circle", CircleShape),
    IconShapeOption("rounded_square", "Rounded", RoundedCornerShape(16.dp)),
    IconShapeOption("squircle", "Squircle", RoundedCornerShape(28.dp)),
    IconShapeOption("square", "Square", RoundedCornerShape(4.dp)),
    IconShapeOption("teardrop", "Teardrop", RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 50.dp, bottomEnd = 8.dp)),
    IconShapeOption("hexagon", "Hexagon", CutCornerShape(8.dp))
)

val defaultWallpapers = listOf(
    WallpaperOption("pastel", listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5), Color(0xFFFFFDE7)), "Pastel"),
    WallpaperOption("sunset", listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFFFF8E53)), "Sunset"),
    WallpaperOption("ocean", listOf(Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF6B8DD6)), "Ocean"),
    WallpaperOption("forest", listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF2E7D32)), "Forest"),
    WallpaperOption("midnight", listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E), Color(0xFF16213E)), "Midnight"),
    WallpaperOption("cyberpunk", listOf(Color(0xFF0D0221), Color(0xFFFF00D4), Color(0xFF00FBFF)), "Cyberpunk"),
    WallpaperOption("rose", listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF48FB1)), "Rose"),
    WallpaperOption("aurora", listOf(Color(0xFF00C9FF), Color(0xFF92FE9D), Color(0xFF00D9FF)), "Aurora")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationSheet(
    isVisible: Boolean,
    currentWallpaper: String,
    currentTheme: Theme,
    gridColumns: Int,
    totalPages: Int,
    currentPage: Int,
    currentIconShape: String,
    allApps: List<com.example.foxos.model.AppInfo> = emptyList(),
    sidebarApps: Set<String> = emptySet(),
    homeScreenApps: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onWallpaperSelected: (String) -> Unit,
    onThemeSelected: (Theme) -> Unit,
    onGridColumnsChanged: (Int) -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: () -> Unit,
    onCustomWallpaperPicked: (Uri) -> Unit,
    onIconShapeChanged: (String) -> Unit,
    onSidebarAppsChanged: (Set<String>) -> Unit = {},
    onHomeScreenAppsChanged: (Set<String>) -> Unit = {}
) {
    val context = LocalContext.current
    var showAppSelector by remember { mutableStateOf(false) }
    var showHomeAppSelector by remember { mutableStateOf(false) }
    
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onCustomWallpaperPicked(it) }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Customize",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Wallpapers Section
                    SectionHeader(icon = Icons.Default.Wallpaper, title = "Wallpapers")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Custom wallpaper option
                        item {
                            WallpaperOptionCard(
                                colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.5f)),
                                name = "Gallery",
                                isSelected = currentWallpaper == "custom",
                                icon = Icons.Default.AddPhotoAlternate,
                                onClick = { wallpaperPickerLauncher.launch("image/*") }
                            )
                        }
                        
                        items(defaultWallpapers) { wallpaper ->
                            WallpaperOptionCard(
                                colors = wallpaper.colors,
                                name = wallpaper.name,
                                isSelected = currentWallpaper == wallpaper.id,
                                onClick = { onWallpaperSelected(wallpaper.id) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Theme Section
                    SectionHeader(icon = Icons.Default.Palette, title = "Themes")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ThemeOptionChip(
                                name = "Orange",
                                color = Color(0xFFFF8F00),
                                isSelected = currentTheme == Theme.ORANGE_BLACK,
                                onClick = { onThemeSelected(Theme.ORANGE_BLACK) }
                            )
                        }
                        item {
                            ThemeOptionChip(
                                name = "Harmony",
                                color = Color(0xFF007DFF),
                                isSelected = currentTheme == Theme.HARMONY_OS,
                                onClick = { onThemeSelected(Theme.HARMONY_OS) }
                            )
                        }
                        item {
                            ThemeOptionChip(
                                name = "Cyberpunk",
                                color = Color(0xFF00FBFF),
                                isSelected = currentTheme == Theme.CYBERPUNK,
                                onClick = { onThemeSelected(Theme.CYBERPUNK) }
                            )
                        }
                        item {
                            ThemeOptionChip(
                                name = "Minimal",
                                color = Color(0xFF1A1A1A),
                                isSelected = currentTheme == Theme.MINIMALIST,
                                onClick = { onThemeSelected(Theme.MINIMALIST) }
                            )
                        }
                        item {
                            ThemeOptionChip(
                                name = "AR Camera",
                                color = Color(0xFF00C853),
                                isSelected = currentTheme == Theme.AR_CAMERA,
                                onClick = { onThemeSelected(Theme.AR_CAMERA) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Icon Shapes Section
                    SectionHeader(icon = Icons.Default.Category, title = "Icon Shapes")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(iconShapeOptions) { shapeOption ->
                            IconShapeOptionCard(
                                shape = shapeOption.shape,
                                name = shapeOption.name,
                                isSelected = currentIconShape == shapeOption.id,
                                onClick = { onIconShapeChanged(shapeOption.id) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Grid & Pages Section
                    SectionHeader(icon = Icons.Default.GridView, title = "Layout")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grid Columns", color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (gridColumns > 3) onGridColumnsChanged(gridColumns - 1) }
                            ) {
                                Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                "$gridColumns",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (gridColumns < 6) onGridColumnsChanged(gridColumns + 1) }
                            ) {
                                Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Home Pages ($currentPage of $totalPages)", color = MaterialTheme.colorScheme.onSurface)
                        Row {
                            IconButton(
                                onClick = onRemovePage,
                                enabled = totalPages > 1
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, "Remove Page", 
                                    tint = if (totalPages > 1) MaterialTheme.colorScheme.error else Color.Gray)
                            }
                            IconButton(
                                onClick = onAddPage,
                                enabled = totalPages < 7
                            ) {
                                Icon(Icons.Default.AddCircleOutline, "Add Page", 
                                    tint = if (totalPages < 7) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Sidebar Apps Section
                    SectionHeader(icon = Icons.Default.Dashboard, title = "Sidebar Apps")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${sidebarApps.size} apps selected",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { showAppSelector = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Apps")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Home Screen Apps Section
                    SectionHeader(icon = Icons.Default.Home, title = "Home Screen Apps")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${homeScreenApps.size} apps pinned",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { showHomeAppSelector = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Apps")
                        }
                    }
                }
            }
        }
    }
    
    // App Selector Dialog
    if (showAppSelector) {
        AppSelectorDialog(
            allApps = allApps,
            selectedApps = sidebarApps,
            title = "Select Sidebar Apps",
            maxApps = 8,
            onDismiss = { showAppSelector = false },
            onAppsSelected = { selected ->
                onSidebarAppsChanged(selected)
                showAppSelector = false
            }
        )
    }
    
    // Home Screen Apps Selector Dialog
    if (showHomeAppSelector) {
        AppSelectorDialog(
            allApps = allApps,
            selectedApps = homeScreenApps,
            title = "Select Home Screen Apps",
            maxApps = 12,
            onDismiss = { showHomeAppSelector = false },
            onAppsSelected = { selected ->
                onHomeScreenAppsChanged(selected)
                showHomeAppSelector = false
            }
        )
    }
}

@Composable
private fun AppSelectorDialog(
    allApps: List<com.example.foxos.model.AppInfo>,
    selectedApps: Set<String>,
    title: String = "Select Apps",
    maxApps: Int = 8,
    onDismiss: () -> Unit,
    onAppsSelected: (Set<String>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedApps) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Selected: ${currentSelection.size}/$maxApps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(allApps.size) { index ->
                        val app = allApps[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSelection = if (currentSelection.contains(app.packageName)) {
                                        currentSelection - app.packageName
                                    } else if (currentSelection.size < maxApps) {
                                        currentSelection + app.packageName
                                    } else {
                                        currentSelection
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = currentSelection.contains(app.packageName),
                                onCheckedChange = { checked ->
                                    currentSelection = if (checked && currentSelection.size < maxApps) {
                                        currentSelection + app.packageName
                                    } else {
                                        currentSelection - app.packageName
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.label, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAppsSelected(currentSelection) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WallpaperOptionCard(
    colors: List<Color>,
    name: String,
    isSelected: Boolean,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(colors))
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ThemeOptionChip(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(name) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f)
        )
    )
}

@Composable
private fun IconShapeOptionCard(
    shape: Shape,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(shape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Apps,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            name,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
