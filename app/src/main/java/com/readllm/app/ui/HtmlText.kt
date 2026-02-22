package com.readllm.app.ui

import android.text.Html
import android.text.Spanned
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable that renders HTML content as styled text
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    fontSize: Float = 18f,
    lineHeight: Float = 1.5f,
    letterSpacing: Float = 0f,
    paragraphSpacing: Float = 16f,
    textIndent: Float = 0f,
    textColor: Color? = null,
    backgroundColor: Color? = null,
    onFontSizeChange: ((Float) -> Unit)? = null
) {
    val defaultColor = textColor ?: MaterialTheme.colorScheme.onBackground
    val annotatedString = remember(html, defaultColor) {
        htmlToAnnotatedString(html, defaultColor)
    }
    
    var currentFontSize by remember { mutableStateOf(fontSize) }
    
    var pointerModifier = modifier
    if (onFontSizeChange != null) {
        pointerModifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                val newSize = (currentFontSize * zoom).coerceIn(12f, 32f)
                if (newSize != currentFontSize) {
                    currentFontSize = newSize
                    onFontSizeChange(newSize)
                }
            }
        }
    }
    
    // Apply background color if specified
    val finalModifier = if (backgroundColor != null) {
        pointerModifier.background(backgroundColor)
    } else {
        pointerModifier
    }
    
    BasicText(
        text = annotatedString,
        modifier = finalModifier.padding(start = textIndent.dp),
        style = TextStyle(
            fontSize = currentFontSize.sp,
            lineHeight = (currentFontSize * lineHeight).sp,
            letterSpacing = letterSpacing.sp,
            color = defaultColor,
            fontFamily = FontFamily.Serif
        )
    )
}

/**
 * Converts HTML string to AnnotatedString with proper styling
 */
private fun htmlToAnnotatedString(html: String, defaultColor: Color): AnnotatedString {
    val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(html)
    }
    
    return buildAnnotatedString {
        parseSpanned(spanned, defaultColor, this)
    }
}

/**
 * Recursively parse Spanned text and apply styles
 */
private fun parseSpanned(
    spanned: Spanned,
    defaultColor: Color,
    builder: AnnotatedString.Builder
) {
    var currentIndex = 0
    
    while (currentIndex < spanned.length) {
        // Find next span change
        val nextSpanChange = spanned.nextSpanTransition(currentIndex, spanned.length, Any::class.java)
        val text = spanned.subSequence(currentIndex, nextSpanChange).toString()
        
        // Get all spans at current position
        val spans = spanned.getSpans(currentIndex, nextSpanChange, Any::class.java)
        
        // Apply styles based on spans
        val styles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        var isBold = false
        var isItalic = false
        var isUnderline = false
        
        spans.forEach { span ->
            when (span) {
                is android.text.style.StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> isBold = true
                        android.graphics.Typeface.ITALIC -> isItalic = true
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            isBold = true
                            isItalic = true
                        }
                    }
                }
                is android.text.style.UnderlineSpan -> isUnderline = true
            }
        }
        
        val spanStyle = SpanStyle(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (isUnderline) TextDecoration.Underline else null,
            color = defaultColor
        )
        
        builder.withStyle(spanStyle) {
            append(text)
        }
        
        currentIndex = nextSpanChange
    }
}
