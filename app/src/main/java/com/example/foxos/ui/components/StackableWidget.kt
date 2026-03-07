package com.example.foxos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foxos.ui.theme.FoxLauncherTheme
import com.example.foxos.ui.theme.HarmonyShapes
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * A stacked widget container that allows vertical swiping between different service cards.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackableWidget(
    modifier: Modifier = Modifier,
    widgets: List<@Composable () -> Unit>
) {
    if (widgets.isEmpty()) return

    val colors = FoxLauncherTheme.colors
    val pagerState = rememberPagerState(pageCount = { widgets.size })

    Box(
        modifier = modifier
            .clip(HarmonyShapes.large)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                widgets[page]()
            }
        }
        
        // Pager Indicators - Premium Glass Style
        if (widgets.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(widgets.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color = if (isSelected) colors.primary else Color.White.copy(alpha = 0.2f)
                        val size = if (isSelected) 8.dp else 5.dp
                        
                        Box(
                            modifier = Modifier
                                .size(size)
                                .clip(CircleShape)
                                .background(color)
                                .then(if (isSelected) Modifier.shimmer(2000) else Modifier)
                        )
                    }
                }
            }
        }
    }
}
