package com.readllm.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Reading theme presets inspired by Readest
 * Each theme has light and dark variants
 */
data class ReadingTheme(
    val name: String,
    val backgroundColor: Color,
    val textColor: Color,
    val isDark: Boolean
)

object ReadingThemes {
    // Light themes
    val DefaultLight = ReadingTheme(
        name = "Default Light",
        backgroundColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF000000),
        isDark = false
    )
    
    val Sepia = ReadingTheme(
        name = "Sepia",
        backgroundColor = Color(0xFFF4ECD8),
        textColor = Color(0xFF5B4636),
        isDark = false
    )
    
    val Cream = ReadingTheme(
        name = "Cream",
        backgroundColor = Color(0xFFFFFEF0),
        textColor = Color(0xFF3A3A3A),
        isDark = false
    )
    
    val SoftBlue = ReadingTheme(
        name = "Soft Blue",
        backgroundColor = Color(0xFFE8F4F8),
        textColor = Color(0xFF1A3A4A),
        isDark = false
    )
    
    val Mint = ReadingTheme(
        name = "Mint",
        backgroundColor = Color(0xFFE8F5E9),
        textColor = Color(0xFF1B5E20),
        isDark = false
    )
    
    // Dark themes
    val DefaultDark = ReadingTheme(
        name = "Default Dark",
        backgroundColor = Color(0xFF121212),
        textColor = Color(0xFFE0E0E0),
        isDark = true
    )
    
    val Night = ReadingTheme(
        name = "Night",
        backgroundColor = Color(0xFF1A1A2E),
        textColor = Color(0xFFEEEEEE),
        isDark = true
    )
    
    val OLED = ReadingTheme(
        name = "OLED",
        backgroundColor = Color(0xFF000000),
        textColor = Color(0xFFFFFFFF),
        isDark = true
    )
    
    val DarkGray = ReadingTheme(
        name = "Dark Gray",
        backgroundColor = Color(0xFF2C2C2C),
        textColor = Color(0xFFD0D0D0),
        isDark = true
    )
    
    val Moonlight = ReadingTheme(
        name = "Moonlight",
        backgroundColor = Color(0xFF222831),
        textColor = Color(0xFFEEEEEE),
        isDark = true
    )
    
    val HighContrast = ReadingTheme(
        name = "High Contrast",
        backgroundColor = Color(0xFF000000),
        textColor = Color(0xFFFFFF00),
        isDark = true
    )
    
    // All available themes
    val allThemes = listOf(
        DefaultLight,
        Sepia,
        Cream,
        SoftBlue,
        Mint,
        DefaultDark,
        Night,
        OLED,
        DarkGray,
        Moonlight,
        HighContrast
    )
    
    fun getThemeByName(name: String): ReadingTheme {
        return allThemes.find { it.name == name } ?: DefaultLight
    }
}
