package com.readllm.app.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reading Ruler - A visual horizontal line that follows reading position
 * Inspired by Readest's reading ruler feature
 */
@Composable
fun ReadingRuler(
    enabled: Boolean,
    rulerColor: Color = Color(0xFF4CAF50).copy(alpha = 0.3f),
    rulerHeight: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    
    var offsetY by remember { mutableStateOf(100f) }
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val rulerHeightPx = with(density) { rulerHeight.toPx() }
            
            // Draw reading ruler rectangle
            drawRect(
                color = rulerColor,
                topLeft = Offset(0f, offsetY - rulerHeightPx / 2),
                size = androidx.compose.ui.geometry.Size(size.width, rulerHeightPx)
            )
            
            // Draw top border line
            drawLine(
                color = rulerColor.copy(alpha = 0.8f),
                start = Offset(0f, offsetY - rulerHeightPx / 2),
                end = Offset(size.width, offsetY - rulerHeightPx / 2),
                strokeWidth = 2f
            )
            
            // Draw bottom border line
            drawLine(
                color = rulerColor.copy(alpha = 0.8f),
                start = Offset(0f, offsetY + rulerHeightPx / 2),
                end = Offset(size.width, offsetY + rulerHeightPx / 2),
                strokeWidth = 2f
            )
        }
    }
}
