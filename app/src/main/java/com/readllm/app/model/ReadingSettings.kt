package com.readllm.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReadingTheme {
    DAY,
    NIGHT,
    SEPIA,
    CUSTOM
}

enum class FontFamily {
    DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE,
    ROBOTO,
    OPEN_SANS,
    MERRIWEATHER,
    LORA
}

enum class PageTurnAnimation {
    NONE,
    SLIDE,
    FADE,
    CURL
}

enum class TextAlignment {
    LEFT,
    JUSTIFY,
    CENTER
}

@Entity(tableName = "reading_settings")
data class ReadingSettings(
    @PrimaryKey
    val id: Long = 1, // Single settings record
    
    // Theme settings
    val theme: ReadingTheme = ReadingTheme.DAY,
    val customBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    val customTextColor: Int = 0xFF000000.toInt(),
    val brightness: Float = 0.5f, // 0.0 to 1.0
    
    // Text settings
    val fontFamily: FontFamily = FontFamily.DEFAULT,
    val fontSize: Int = 18, // in sp
    val lineSpacing: Float = 1.5f, // multiplier
    val textAlignment: TextAlignment = TextAlignment.JUSTIFY,
    val boldText: Boolean = false,
    
    // Layout settings
    val marginHorizontal: Int = 16, // in dp
    val marginVertical: Int = 24, // in dp
    val paragraphSpacing: Int = 8, // in dp
    
    // Page settings
    val pageTurnAnimation: PageTurnAnimation = PageTurnAnimation.SLIDE,
    val keepScreenOn: Boolean = false,
    val screenOrientationLocked: Boolean = false,
    val volumeKeysNavigation: Boolean = true, // Use volume keys to turn pages
    
    // Reading behavior
    val autoBookmark: Boolean = true, // Auto-bookmark on exit
    val fullScreenMode: Boolean = false,
    val statusBarVisible: Boolean = true,
    val navigationBarVisible: Boolean = true
)
