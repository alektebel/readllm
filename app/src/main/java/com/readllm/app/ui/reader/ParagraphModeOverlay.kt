package com.readllm.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paragraph Mode - Focus on one paragraph at a time
 * Dimmed surrounding paragraphs for better focus
 * Inspired by Readest's paragraph mode feature
 */
@Composable
fun ParagraphModeOverlay(
    text: AnnotatedString,
    isActive: Boolean,
    fontSize: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return
    
    var currentParagraphIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    
    // Extract paragraphs from text
    val paragraphs = remember(text) {
        text.text.split("\n\n", "\n")
            .filter { it.isNotBlank() }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            paragraphs.forEachIndexed { index, paragraph ->
                val isCurrent = index == currentParagraphIndex
                val opacity = when {
                    isCurrent -> 1f
                    kotlin.math.abs(index - currentParagraphIndex) == 1 -> 0.4f
                    else -> 0.15f
                }
                
                Text(
                    text = paragraph,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.5f).sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = opacity),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !isCurrent) {
                            currentParagraphIndex = index
                        }
                )
            }
        }
        
        // Navigation controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paragraph ${currentParagraphIndex + 1} / ${paragraphs.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row {
                    IconButton(
                        onClick = { 
                            if (currentParagraphIndex > 0) {
                                currentParagraphIndex--
                            }
                        },
                        enabled = currentParagraphIndex > 0
                    ) {
                        Icon(Icons.Default.NavigateBefore, "Previous Paragraph")
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Exit Paragraph Mode")
                    }
                    
                    IconButton(
                        onClick = { 
                            if (currentParagraphIndex < paragraphs.size - 1) {
                                currentParagraphIndex++
                            }
                        },
                        enabled = currentParagraphIndex < paragraphs.size - 1
                    ) {
                        Icon(Icons.Default.NavigateNext, "Next Paragraph")
                    }
                }
            }
        }
    }
}
