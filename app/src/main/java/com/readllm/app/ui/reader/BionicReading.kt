package com.readllm.app.ui.reader

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Bionic Reading / Highlighted Reading
 * Bolds the first half of each word to increase reading speed
 * Inspired by book-story's highlighted reading feature
 */
object BionicReading {
    
    /**
     * Apply bionic reading effect to text
     * Bolds the first half of each word
     */
    fun applyBionicReading(
        text: String,
        enabled: Boolean = true,
        fontWeight: FontWeight = FontWeight.Bold
    ): AnnotatedString {
        if (!enabled) {
            return AnnotatedString(text)
        }
        
        return buildAnnotatedString {
            // Split by whitespace while preserving delimiters
            val words = text.split(Regex("(\\s+)"))
            
            words.forEach { word ->
                if (word.isBlank()) {
                    append(word)
                } else {
                    // Check if word is alphanumeric
                    val alphaNumeric = word.filter { it.isLetterOrDigit() }
                    
                    if (alphaNumeric.isEmpty()) {
                        // Special characters only (punctuation, etc.)
                        append(word)
                    } else {
                        // Calculate how many characters to bold
                        val boldLength = calculateBoldLength(alphaNumeric.length)
                        
                        var alphaCount = 0
                        var boldApplied = false
                        
                        word.forEach { char ->
                            if (char.isLetterOrDigit()) {
                                alphaCount++
                                
                                if (alphaCount <= boldLength) {
                                    // Bold first half
                                    withStyle(SpanStyle(fontWeight = fontWeight)) {
                                        append(char)
                                    }
                                } else {
                                    // Regular second half
                                    append(char)
                                    boldApplied = true
                                }
                            } else {
                                // Non-alphanumeric (punctuation)
                                append(char)
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Calculate how many characters to bold based on word length
     * Uses a progressive formula for optimal reading
     */
    private fun calculateBoldLength(wordLength: Int): Int {
        return when {
            wordLength <= 1 -> 1
            wordLength == 2 -> 1
            wordLength == 3 -> 2
            wordLength <= 5 -> (wordLength / 2.0).toInt()
            wordLength <= 8 -> (wordLength / 2.0 + 0.5).toInt()
            else -> (wordLength * 0.6).toInt()
        }
    }
    
    /**
     * Apply bionic reading to AnnotatedString (preserves existing styling)
     */
    fun applyBionicReading(
        annotatedText: AnnotatedString,
        enabled: Boolean = true,
        fontWeight: FontWeight = FontWeight.Bold
    ): AnnotatedString {
        if (!enabled) {
            return annotatedText
        }
        
        return applyBionicReading(annotatedText.text, enabled, fontWeight)
    }
}
