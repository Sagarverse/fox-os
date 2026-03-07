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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.ArrowForward
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
import com.example.foxos.ui.components.PremiumAppSelector

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
    WallpaperOption("nebula", listOf(Color(0xFF23074D), Color(0xFFCC5333), Color(0xFFB91D73)), "Nebula"),
    WallpaperOption("lava", listOf(Color(0xFF4B0082), Color(0xFFFF4500), Color(0xFFFF0000)), "Lava"),
    WallpaperOption("ghost", listOf(Color(0xFF1F1C2C), Color(0xFF928DAB), Color(0xFF44A08D)), "Ghost Shell"),
    WallpaperOption("aurora", listOf(Color(0xFF00C9FF), Color(0xFF92FE9D), Color(0xFF00D9FF)), "Aurora")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationSheet(
    isVisible: Boolean,
    currentWallpaper: String,
    currentWallpaperStyle: String,
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
    onWallpaperStyleSelected: (String) -> Unit,
    onThemeSelected: (Theme) -> Unit,
    onGridColumnsChanged: (Int) -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: () -> Unit,
    onCustomWallpaperPicked: (Uri) -> Unit,
    onIconShapeChanged: (String) -> Unit,
    onSidebarAppsChanged: (Set<String>) -> Unit = {},
    onHomeScreenAppsChanged: (Set<String>) -> Unit = {},
    dockApps: Set<String> = emptySet(),
    onDockAppsChanged: (Set<String>) -> Unit = {}
) {
    val context = LocalContext.current
    var showSidebarAppSelector by remember { mutableStateOf(false) }
    var showHomeAppSelector by remember { mutableStateOf(false) }
    var showDockAppSelector by remember { mutableStateOf(false) }
    
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
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = true, onClick = {}),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = if (MaterialTheme.colorScheme.surface == Color.White) Color.White.copy(alpha = 0.85f) else Color(0xFF15191C).copy(alpha = 0.9f),
                blurRadius = 45.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header with Handle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Design Center",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Wallpapers Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.Wallpaper, title = "Aesthetic Backdrop")
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        WallpaperOptionCard(
                                            colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.4f)),
                                            name = "Gallery",
                                            isSelected = currentWallpaper == "custom",
                                            icon = Icons.Default.AddPhotoAlternate,
                                            onClick = { 
                                                onWallpaperSelected("custom")
                                                wallpaperPickerLauncher.launch("image/*") 
                                            }
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
                            }
                        }
                        
                        // Wallpaper Style Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.Style, title = "Motion Geometry")
                                Spacer(modifier = Modifier.height(16.dp))
                                 Row(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .horizontalScroll(rememberScrollState()),
                                     horizontalArrangement = Arrangement.spacedBy(16.dp)
                                 ) {
                                     WallpaperStyleCard(
                                         modifier = Modifier.width(160.dp),
                                         title = "Aurora",
                                         description = "Flowing liquid orbs",
                                         isSelected = currentWallpaperStyle == "aurora",
                                         onClick = { onWallpaperStyleSelected("aurora") }
                                     )
                                     WallpaperStyleCard(
                                         modifier = Modifier.width(160.dp),
                                         title = "Mesh",
                                         description = "Floating glass bubbles",
                                         isSelected = currentWallpaperStyle == "mesh",
                                         onClick = { onWallpaperStyleSelected("mesh") }
                                     )
                                     WallpaperStyleCard(
                                         modifier = Modifier.width(160.dp),
                                         title = "Bubbles",
                                         description = "Split glass motion",
                                         isSelected = currentWallpaperStyle == "bubbles",
                                         onClick = { onWallpaperStyleSelected("bubbles") }
                                     )
                                 }
                            }
                        }
                        
                        // Theme Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.Palette, title = "Visual Protocol")
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Theme.entries.forEach { theme ->
                                        item {
                                            ThemeOptionChip(
                                                name = theme.name.replace("_", " ").lowercase().capitalize(),
                                                color = when(theme) {
                                                    Theme.ORANGE_BLACK -> Color(0xFFFF8F00)
                                                    Theme.HARMONY_OS -> Color(0xFF007DFF)
                                                    Theme.CYBERPUNK -> Color(0xFF00FBFF)
                                                    Theme.MINIMALIST -> Color(0xFF1A1A1A)
                                                    Theme.AR_CAMERA -> Color(0xFF00C853)
                                                    else -> MaterialTheme.colorScheme.primary
                                                },
                                                isSelected = currentTheme == theme,
                                                onClick = { onThemeSelected(theme) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Icon Shapes Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.Category, title = "Entity Geometry")
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                            }
                        }
                        
                        // Grid & Pages Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.GridView, title = "Space Layout")
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Grid Matrix", fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { if (gridColumns > 3) onGridColumnsChanged(gridColumns - 1) }) {
                                                Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            Text("$gridColumns", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))
                                            IconButton(onClick = { if (gridColumns < 6) onGridColumnsChanged(gridColumns + 1) }) {
                                                Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Active Pages ($currentPage of $totalPages)", fontWeight = FontWeight.Bold)
                                        Row {
                                            IconButton(onClick = onRemovePage, enabled = totalPages > 1) {
                                                Icon(Icons.Default.RemoveCircleOutline, "Remove", tint = if (totalPages > 1) MaterialTheme.colorScheme.error else Color.Gray)
                                            }
                                            IconButton(onClick = onAddPage, enabled = totalPages < 7) {
                                                Icon(Icons.Default.AddCircleOutline, "Add", tint = if (totalPages < 7) MaterialTheme.colorScheme.primary else Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Modular Apps Section
                        item {
                            Column {
                                SectionHeader(icon = Icons.Default.Dashboard, title = "Modular Systems")
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ModularAppControl(
                                        modifier = Modifier.weight(1f),
                                        title = "Sidebar",
                                        count = sidebarApps.size,
                                        onClick = { showSidebarAppSelector = true }
                                    )
                                    ModularAppControl(
                                        modifier = Modifier.weight(1f),
                                        title = "Quick Apps",
                                        count = dockApps.size,
                                        onClick = { showDockAppSelector = true }
                                    )
                                    ModularAppControl(
                                        modifier = Modifier.weight(1f),
                                        title = "Home Apps",
                                        count = homeScreenApps.size,
                                        onClick = { showHomeAppSelector = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // App Selector Modal
    if (showSidebarAppSelector) {
        PremiumAppSelector(
            allApps = allApps,
            selectedApps = sidebarApps,
            title = "Sidebar Hub",
            onDismiss = { showSidebarAppSelector = false },
            onAppsSelected = { onSidebarAppsChanged(it) }
        )
    }
    
    if (showHomeAppSelector) {
        PremiumAppSelector(
            allApps = allApps,
            selectedApps = homeScreenApps,
            title = "Select Home Screen Apps",
            maxApps = 12,
            onDismiss = { showHomeAppSelector = false },
            onAppsSelected = { selected: Set<String> ->
                onHomeScreenAppsChanged(selected)
                showHomeAppSelector = false
            }
        )
    }
    
    if (showDockAppSelector) {
        PremiumAppSelector(
            allApps = allApps,
            selectedApps = dockApps,
            title = "Select Quick Apps",
            maxApps = 8,
            onDismiss = { showDockAppSelector = false },
            onAppsSelected = { selected ->
                onDockAppsChanged(selected)
                showDockAppSelector = false
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
private fun WallpaperStyleCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp)
            )
        }
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
