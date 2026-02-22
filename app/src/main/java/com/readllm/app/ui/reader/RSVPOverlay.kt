package com.readllm.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * RSVP (Rapid Serial Visual Presentation) - Speed reading mode
 * Displays words one at a time at adjustable speed (100-1000 WPM)
 * Inspired by Readest's RSVP feature
 */
@Composable
fun RSVPOverlay(
    text: String,
    isActive: Boolean,
    initialWPM: Int = 300,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return
    
    var wordsPerMinute by remember { mutableStateOf(initialWPM) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentWordIndex by remember { mutableStateOf(0) }
    var showControls by remember { mutableStateOf(true) }
    
    // Extract words from text
    val words = remember(text) {
        text.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
    
    // Auto-play effect
    LaunchedEffect(isPlaying, wordsPerMinute) {
        if (isPlaying && currentWordIndex < words.size) {
            while (isActive && isPlaying && currentWordIndex < words.size) {
                delay((60000 / wordsPerMinute).toLong())
                
                if (currentWordIndex < words.size - 1) {
                    currentWordIndex++
                } else {
                    isPlaying = false
                }
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { showControls = !showControls }
    ) {
        // Main word display
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Progress indicator
            Text(
                text = "${currentWordIndex + 1} / ${words.size}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Current word - large and centered
            if (currentWordIndex < words.size) {
                Text(
                    text = words[currentWordIndex],
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
            
            // Linear progress
            LinearProgressIndicator(
                progress = currentWordIndex.toFloat() / words.size.coerceAtLeast(1),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Speed control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speed: $wordsPerMinute WPM", style = MaterialTheme.typography.bodyMedium)
                        Row {
                            IconButton(
                                onClick = { wordsPerMinute = (wordsPerMinute - 50).coerceAtLeast(100) }
                            ) {
                                Icon(Icons.Default.Remove, "Slower")
                            }
                            IconButton(
                                onClick = { wordsPerMinute = (wordsPerMinute + 50).coerceAtMost(1000) }
                            ) {
                                Icon(Icons.Default.Add, "Faster")
                            }
                        }
                    }
                    
                    Slider(
                        value = wordsPerMinute.toFloat(),
                        onValueChange = { wordsPerMinute = it.toInt() },
                        valueRange = 100f..1000f,
                        steps = 17,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentWordIndex = 0 }) {
                            Icon(Icons.Default.SkipPrevious, "Restart")
                        }
                        
                        IconButton(
                            onClick = { 
                                currentWordIndex = (currentWordIndex - 10).coerceAtLeast(0)
                            }
                        ) {
                            Icon(Icons.Default.FastRewind, "Back 10")
                        }
                        
                        FilledIconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { 
                                currentWordIndex = (currentWordIndex + 10).coerceAtMost(words.size - 1)
                            }
                        ) {
                            Icon(Icons.Default.FastForward, "Forward 10")
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Exit")
                        }
                    }
                }
            }
        }
    }
}
