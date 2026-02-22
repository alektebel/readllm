package com.readllm.app.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color preset for reader background and text colors
 * Inspired by Book's Story app
 */
@Immutable
data class ColorPreset(
    val id: Int,
    val name: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    companion object {
        // Light presets
        val defaultLight = ColorPreset(
            id = 0,
            name = "Default",
            backgroundColor = Color(0xFFFFFFFF),
            textColor = Color(0xFF000000)
        )
        
        val sepia = ColorPreset(
            id = 1,
            name = "Sepia",
            backgroundColor = Color(0xFFF4ECD8),
            textColor = Color(0xFF5B4636)
        )
        
        val cream = ColorPreset(
            id = 2,
            name = "Cream",
            backgroundColor = Color(0xFFFFFEF0),
            textColor = Color(0xFF3A3A3A)
        )
        
        val softBlue = ColorPreset(
            id = 3,
            name = "Soft Blue",
            backgroundColor = Color(0xFFE8F4F8),
            textColor = Color(0xFF1A3A4A)
        )
        
        val mint = ColorPreset(
            id = 4,
            name = "Mint",
            backgroundColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF1B5E20)
        )
        
        val lavender = ColorPreset(
            id = 5,
            name = "Lavender",
            backgroundColor = Color(0xFFF3E5F5),
            textColor = Color(0xFF4A148C)
        )
        
        // Dark presets
        val defaultDark = ColorPreset(
            id = 6,
            name = "Dark",
            backgroundColor = Color(0xFF1A1A1A),
            textColor = Color(0xFFE0E0E0)
        )
        
        val night = ColorPreset(
            id = 7,
            name = "Night",
            backgroundColor = Color(0xFF1A1A2E),
            textColor = Color(0xFFEEEEEE)
        )
        
        val oled = ColorPreset(
            id = 8,
            name = "OLED",
            backgroundColor = Color(0xFF000000),
            textColor = Color(0xFFFFFFFF)
        )
        
        val darkGray = ColorPreset(
            id = 9,
            name = "Dark Gray",
            backgroundColor = Color(0xFF2C2C2C),
            textColor = Color(0xFFD0D0D0)
        )
        
        val moonlight = ColorPreset(
            id = 10,
            name = "Moonlight",
            backgroundColor = Color(0xFF222831),
            textColor = Color(0xFFEEEEEE)
        )
        
        val highContrast = ColorPreset(
            id = 11,
            name = "High Contrast",
            backgroundColor = Color(0xFF000000),
            textColor = Color(0xFFFFFF00)
        )
        
        // All available presets
        val allPresets = listOf(
            defaultLight,
            sepia,
            cream,
            softBlue,
            mint,
            lavender,
            defaultDark,
            night,
            oled,
            darkGray,
            moonlight,
            highContrast
        )
        
        val lightPresets = allPresets.filter { it.id < 6 }
        val darkPresets = allPresets.filter { it.id >= 6 }
        
        fun getPresetById(id: Int): ColorPreset {
            return allPresets.find { it.id == id } ?: defaultLight
        }
    }
}
