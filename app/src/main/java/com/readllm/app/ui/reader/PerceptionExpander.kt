package com.readllm.app.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Perception Expander
 * Two vertical lines on left/right sides to guide eye movement
 * Helps expand peripheral vision and improve reading speed
 * Inspired by book-story's perception expander feature
 */
@Composable
fun PerceptionExpander(
    enabled: Boolean,
    horizontalPadding: Dp = 80.dp,
    lineThickness: Dp = 2.dp,
    lineColor: Color = Color(0xFF4CAF50).copy(alpha = 0.4f),
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val paddingPx = with(density) { horizontalPadding.toPx() }
        val thicknessPx = with(density) { lineThickness.toPx() }
        
        val leftX = paddingPx
        val rightX = size.width - paddingPx
        
        // Draw left vertical line
        drawLine(
            color = lineColor,
            start = Offset(leftX, 0f),
            end = Offset(leftX, size.height),
            strokeWidth = thicknessPx
        )
        
        // Draw right vertical line
        drawLine(
            color = lineColor,
            start = Offset(rightX, 0f),
            end = Offset(rightX, size.height),
            strokeWidth = thicknessPx
        )
    }
}
