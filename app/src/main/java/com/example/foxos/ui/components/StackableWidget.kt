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
        
        // Pager Indicators
        if (widgets.size > 1) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(widgets.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == iteration) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}
