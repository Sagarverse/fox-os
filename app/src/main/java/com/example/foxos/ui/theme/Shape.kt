package com.example.foxos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Harmony OS design philosophy: Soft rounded corners everywhere
val HarmonyShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(44.dp)
)

// Specific component shapes
val FolderShape = RoundedCornerShape(40.dp)
val WidgetShape = RoundedCornerShape(32.dp)
val DockShape = RoundedCornerShape(48.dp)
