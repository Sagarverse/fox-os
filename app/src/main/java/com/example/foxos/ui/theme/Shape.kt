package com.example.foxos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Harmony OS design philosophy: Soft rounded corners everywhere
val HarmonyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

// Specific component shapes
val FolderShape = RoundedCornerShape(32.dp)
val WidgetShape = RoundedCornerShape(24.dp)
val DockShape = RoundedCornerShape(40.dp)
