package com.readllm.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A6A),           // Teal - calming, reading-friendly
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBEB),  // Light teal
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF4A6363),         // Muted teal-gray
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E8),
    onSecondaryContainer = Color(0xFF051F1F),
    tertiary = Color(0xFF4D6047),          // Sage green
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFE6C6),
    onTertiaryContainer = Color(0xFF0B1F09),
    background = Color(0xFFFAFDFD),        // Slightly cool white
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFAFDFD),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4948)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80D4D4),           // Light teal for dark mode
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFFB2EBEB),
    secondary = Color(0xFFB0CCCC),         // Soft teal-gray
    onSecondary = Color(0xFF1B3434),
    secondaryContainer = Color(0xFF324B4B),
    onSecondaryContainer = Color(0xFFCCE8E8),
    tertiary = Color(0xFFB3CAAB),          // Sage green
    onTertiary = Color(0xFF1F361B),
    tertiaryContainer = Color(0xFF354D31),
    onTertiaryContainer = Color(0xFFCFE6C6),
    background = Color(0xFF191C1C),        // True dark
    onBackground = Color(0xFFE0E3E3),
    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBFC9C8)
)

@Composable
fun ReadLLMTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
