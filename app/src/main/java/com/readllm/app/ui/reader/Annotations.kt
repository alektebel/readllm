package com.readllm.app.ui.reader

import androidx.compose.ui.graphics.Color

/**
 * Text highlight colors for annotations
 * Inspired by Readest's highlighting system
 */
object HighlightColors {
    val Yellow = Color(0xFFFFEB3B).copy(alpha = 0.4f)
    val Green = Color(0xFF4CAF50).copy(alpha = 0.4f)
    val Blue = Color(0xFF2196F3).copy(alpha = 0.4f)
    val Pink = Color(0xFFE91E63).copy(alpha = 0.4f)
    val Orange = Color(0xFFFF9800).copy(alpha = 0.4f)
    val Purple = Color(0xFF9C27B0).copy(alpha = 0.4f)
    
    val allColors = listOf(Yellow, Green, Blue, Pink, Orange, Purple)
    val colorNames = listOf("Yellow", "Green", "Blue", "Pink", "Orange", "Purple")
}

/**
 * Represents a text highlight/annotation
 */
data class TextHighlight(
    val id: String,
    val bookId: Long,
    val chapterNumber: Int,
    val startIndex: Int,
    val endIndex: Int,
    val text: String,
    val color: Color,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a bookmark
 */
data class Bookmark(
    val id: String,
    val bookId: Long,
    val chapterNumber: Int,
    val position: Int,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)
