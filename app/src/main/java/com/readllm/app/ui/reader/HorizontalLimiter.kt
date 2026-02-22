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
 * Horizontal Limiter / Reading Focus Zone
 * Dims text outside a configurable reading zone to help maintain focus
 * Inspired by book-story's horizontal limiter feature
 */
@Composable
fun HorizontalLimiter(
    enabled: Boolean,
    zoneHeight: Dp = 100.dp,
    verticalOffset: Dp = 0.dp,
    showRulerLines: Boolean = true,
    rulerThickness: Dp = 2.dp,
    dimmingOpacity: Float = 0.7f,
    dimmingColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val centerY = size.height / 2 + with(density) { verticalOffset.toPx() }
        val zoneHeightPx = with(density) { zoneHeight.toPx() }
        val halfZone = zoneHeightPx / 2
        
        val topY = centerY - halfZone
        val bottomY = centerY + halfZone
        
        // Draw top dimming area
        if (topY > 0) {
            drawRect(
                color = dimmingColor.copy(alpha = dimmingOpacity),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, topY)
            )
        }
        
        // Draw bottom dimming area
        if (bottomY < size.height) {
            drawRect(
                color = dimmingColor.copy(alpha = dimmingOpacity),
                topLeft = Offset(0f, bottomY),
                size = androidx.compose.ui.geometry.Size(size.width, size.height - bottomY)
            )
        }
        
        // Draw ruler lines if enabled
        if (showRulerLines) {
            val rulerColor = Color.White.copy(alpha = 0.5f)
            val rulerThicknessPx = with(density) { rulerThickness.toPx() }
            
            // Top ruler line
            drawLine(
                color = rulerColor,
                start = Offset(0f, topY),
                end = Offset(size.width, topY),
                strokeWidth = rulerThicknessPx
            )
            
            // Bottom ruler line
            drawLine(
                color = rulerColor,
                start = Offset(0f, bottomY),
                end = Offset(size.width, bottomY),
                strokeWidth = rulerThicknessPx
            )
        }
    }
}
